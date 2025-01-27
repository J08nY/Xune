package sk.neuromancer.Xune.game;

import com.github.quantranuk.protobuf.nio.ProtoChannelFactory;
import com.github.quantranuk.protobuf.nio.ProtoServerSocketChannel;
import com.google.protobuf.Message;
import org.lwjgl.system.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.PlayableEntity;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.game.players.Remote;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.network.Utils;
import sk.neuromancer.Xune.network.controllers.Controller;
import sk.neuromancer.Xune.network.controllers.LocalController;
import sk.neuromancer.Xune.proto.MessageProto;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;


@CommandLine.Command(name = "XuneServer", mixinStandardHelpOptions = true, version = "1.1.2", description = "Xune 2025 server.")
public class Server implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to listen on.", defaultValue = "7531")
    private int port;

    private boolean keepRunning = true;
    private int tickCount;
    private int updateCount;

    private Level level;
    private Map<Integer, Client> clients;
    private State state;

    private ProtoServerSocketChannel serverChannel;
    private LinkedTransferQueue<Msg> messageQueue;

    @Override
    public void run() {
        setup();

        if (!start()) {
            return;
        }

        long lastTick = System.nanoTime();
        double unprocessed = 0;
        double nsPerTick = 1000000000.0 / Config.TPS;
        int ticks = 0;
        long lastSecond = System.currentTimeMillis();

        while (keepRunning) {
            long now = System.nanoTime();
            unprocessed += (now - lastTick) / nsPerTick;
            lastTick = now;
            while (unprocessed >= 1) {
                ticks++;
                tick();
                unprocessed -= 1;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSecond > 1000) {
                lastSecond = currentTime;
                LOGGER.info("{} ticks, {} updates", ticks, updateCount);
                ticks = 0;
                updateCount = 0;
            }
        }

        stop();
    }

    private void setup() {
        Library.initialize();
        SpriteSheet.initSheets();
        Entity.initClasses();

        ProtoChannelFactory.ServerBuilder builder = ProtoChannelFactory.newServer(port).setSerializer(Utils.getIdSerializer());
        serverChannel = builder.build();
        serverChannel.addMessageReceivedHandler(this::onMsgReceived);
        serverChannel.addConnectionHandler(this::onConnect);
        serverChannel.addDisconnectionHandler(this::onDisconnect);

        messageQueue = new LinkedTransferQueue<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            keepRunning = false;
        }));

        level = new Level(Level.LEVEL_1);
        tickCount = 0;
        state = State.Lobby;
        clients = new ConcurrentHashMap<>();
    }

    private boolean start() {
        try {
            serverChannel.start();
        } catch (IOException e) {
            LOGGER.error("Failed to bind to port.", e);
            return false;
        }
        return true;
    }

    private void stop() {
        serverChannel.stop();
    }

    private void tick() {
        List<Msg> messages = new LinkedList<>();
        messageQueue.drainTo(messages);
        messages.forEach(this::handleMessage);

        if (state == State.Lobby) {
            if (level.isFull()) {
                LOGGER.info("Starting game...");
                for (Client client : clients.values()) {
                    client.setState(ClientState.InGame);
                }
                state = State.InGame;
                serverChannel.sendMessageToAll(MessageProto.Event.newBuilder().setGameStart(MessageProto.GameStart.newBuilder().setLevel(level.serializeFull()).build()).build());
            }
        } else if (state == State.InGame) {
            if (clients.isEmpty()) {
                LOGGER.info("No clients connected, restarting game.");
                state = State.Lobby;
                level = new Level(Level.LEVEL_1);
                tickCount = 0;
                return;
            }

            if (level.isDone()) {
                LOGGER.info("Game over...");
                state = State.Done;
                Player winner = level.getWinner();
                serverChannel.sendMessageToAll(MessageProto.Event.newBuilder().setGameEnd(MessageProto.GameEnd.newBuilder().setWinnerId(winner.getId()).build()).build());
                return;
            }

            tickCount++;
            level.tick(tickCount);
            if (!messages.isEmpty() || tickCount % Config.TICKS_PER_UPDATE == 0) {
                updateCount++;
                serverChannel.sendMessageToAll(MessageProto.State.newBuilder().setLevel(level.serializeTransient()).build());
            }

            if (tickCount % Config.TPS == 0) {
                serverChannel.sendMessageToAll(MessageProto.Connection.newBuilder().setPing(MessageProto.Ping.newBuilder().setTimestamp(System.currentTimeMillis()).build()).build());
            }
        } else if (state == State.Done) {
            LOGGER.info("Game done...");
            keepRunning = false;
        }
    }

    private void onConnect(SocketAddress address) {
        LOGGER.info("Connection from: {}", address);
        int id = clients.size();
        Client client = new Client(address, ClientState.Connected, id, null);
        clients.put(id, client);
    }

    private void onDisconnect(SocketAddress address) {
        LOGGER.info("Disconnect from: {}", address);
        clients.values().stream().filter(c -> c.getAddress().equals(address)).findFirst().ifPresent(client -> {
            clients.remove(client.getId());
            if (client.getPlayer() != null) {
                level.removePlayer(client.getPlayer());
            }
        });
    }

    private void handleMessage(Msg msg) {
        SocketAddress address = msg.address();
        Message message = msg.message();
        if (message instanceof MessageProto.Connection conn) {
            if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.REQUEST) {
                LOGGER.info("Connection request from {}", address);
                if (level.isFull() || state != State.Lobby) {
                    LOGGER.info("Game is full or not in lobby state.");
                    return;
                }
                Client client = clients.values().stream().filter(c -> c.getAddress().equals(address)).findFirst().orElse(null);
                if (client == null) {
                    LOGGER.error("Client not found");
                    return;
                }
                LOGGER.info("Client id: {}", client.getId());
                // TODO: This is nasty.
                Player player = new Remote(level, Flag.values()[client.getId() % 3], 1000, client.getId());
                player.setController(new LocalController(level, player));
                level.addPlayer(player);
                client.setState(ClientState.Lobby);
                LOGGER.info("Player flag: {}", player.getFlag());
                MessageProto.Connection response = MessageProto.Connection.newBuilder().setResponse(MessageProto.ConnectionResponse.newBuilder().setPlayerId(client.id).build()).build();
                serverChannel.sendMessage(address, response);
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.PING) {
                LOGGER.info("Ping from {}", address);
                MessageProto.Ping ping = conn.getPing();
                MessageProto.Pong pong = MessageProto.Pong.newBuilder().setPreviousTimestamp(ping.getTimestamp()).setTimestamp(System.currentTimeMillis()).build();
                serverChannel.sendMessage(address, MessageProto.Connection.newBuilder().setPong(pong).build());
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.PONG) {
                MessageProto.Pong pong = conn.getPong();
                long rtt = System.currentTimeMillis() - pong.getPreviousTimestamp();
                LOGGER.info("Pong from {} RTT: {} ms", address, rtt);
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.RESPONSE) {
                LOGGER.warn("Should not happen, client sent response.");
            } else {
                LOGGER.warn("Should not happen, client sent unknown connection mesasge.");
            }
        } else if (message instanceof MessageProto.Action action) {
            clients.values().stream().filter(c -> c.getAddress().equals(address)).findFirst().ifPresentOrElse(client -> {
                if (client.getState() != ClientState.InGame) {
                    LOGGER.warn("Client not in game state");
                    return;
                }
                Player player = client.getPlayer();
                Controller controller = player.getController();
                if (action.getActionCase() == MessageProto.Action.ActionCase.ENTITYPRODUCE) {
                    MessageProto.EntityProduceAction produce = action.getEntityProduce();
                    Building producer = (Building) level.getEntity(produce.getProducerId());
                    controller.produceUnit(PlayableEntity.fromEntityClass(produce.getKlass()).asSubclass(Unit.class), producer);
                } else if (action.getActionCase() == MessageProto.Action.ActionCase.BUILDINGPRODUCE) {
                    MessageProto.BuildingProduceAction produce = action.getBuildingProduce();
                    controller.produceBuilding(PlayableEntity.fromEntityClass(produce.getKlass()).asSubclass(Building.class));
                } else if (action.getActionCase() == MessageProto.Action.ActionCase.BUILDINGPLACE) {
                    MessageProto.BuildingPlaceAction place = action.getBuildingPlace();
                    Building building = player.getBuildResult(place.getPosition().getX(), place.getPosition().getY());
                    if (building == null) {
                        LOGGER.error("Failed to create building.");
                        return;
                    }
                    controller.placeBuilding(building);
                } else if (action.getActionCase() == MessageProto.Action.ActionCase.SENDCOMMAND) {
                    MessageProto.SendCommandAction send = action.getSendCommand();
                    Unit unit = (Unit) level.getEntity(send.getEntityId());
                    if (unit == null) {
                        LOGGER.error("Unit not found.");
                        return;
                    }
                    controller.sendCommand(unit, Command.deserialize(send.getCommand(), level));
                } else if (action.getActionCase() == MessageProto.Action.ActionCase.PUSHCOMMAND) {
                    MessageProto.PushCommandAction push = action.getPushCommand();
                    Unit unit = (Unit) level.getEntity(push.getEntityId());
                    if (unit == null) {
                        LOGGER.error("Unit not found.");
                        return;
                    }
                    controller.pushCommand(unit, Command.deserialize(push.getCommand(), level));
                } else {
                    LOGGER.warn("Should not happen, client sent unknown action.");
                }
            }, () -> LOGGER.warn("Client not found"));
        } else if (message instanceof MessageProto.State || message instanceof MessageProto.Event) {
            LOGGER.warn("Should not happen, client sent state or event.");
        }
    }

    private void onMsgReceived(SocketAddress address, Message message) {
        messageQueue.put(new Msg(address, message));
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Server()).execute(args);
        System.exit(exitCode);
    }

    class Client {
        private SocketAddress address;
        private ClientState state;
        private int id;
        private Player player;

        public Client(SocketAddress address, ClientState state, int id, Player player) {
            this.address = address;
            this.state = state;
            this.id = id;
            this.player = player;
        }

        public SocketAddress getAddress() {
            return address;
        }

        public void setAddress(SocketAddress address) {
            this.address = address;
        }

        public ClientState getState() {
            return state;
        }

        public void setState(ClientState state) {
            this.state = state;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }
    }

    record Msg(SocketAddress address, Message message) {

    }

    enum State {
        Lobby,
        InGame,
        Done
    }

    enum ClientState {
        Connected,
        Lobby,
        InGame
    }
}
