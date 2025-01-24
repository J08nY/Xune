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
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.game.players.Remote;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.network.Utils;
import sk.neuromancer.Xune.proto.MessageProto;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;


@CommandLine.Command(name = "XuneServer", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025 server.")
public class Server implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to listen on.", defaultValue = "7531")
    private int port;

    private boolean keepRunning = true;
    private int tickCount;

    private Level level;
    private List<Client> clients;
    private List<Player> players;
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
                LOGGER.info("{} ticks", ticks);
                ticks = 0;
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
        state = State.Lobby;
        clients = Collections.synchronizedList(new LinkedList<>());
        players = Collections.synchronizedList(new LinkedList<>());
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
                state = State.InGame;
                serverChannel.sendMessageToAll(MessageProto.Event.newBuilder().setGameStart(MessageProto.GameStart.newBuilder().setLevel(level.serializeFull()).build()).build());
            }
        } else if (state == State.InGame) {
            if (level.isDone()) {
                LOGGER.info("Game over...");
                state = State.Done;
                Player winner = level.getWinner();
                serverChannel.sendMessageToAll(MessageProto.Event.newBuilder().setGameEnd(MessageProto.GameEnd.newBuilder().setWinnerId(winner.getId()).build()).build());
                return;
            }
            tickCount++;
            level.tick(tickCount);
            serverChannel.sendMessageToAll(MessageProto.State.newBuilder().setLevel(level.serializeTransient()).build());
        } else if (state == State.Done) {
            keepRunning = false;
        }
    }

    private void onConnect(SocketAddress address) {
        LOGGER.info("Connection from: {}", address);
    }

    private void onDisconnect(SocketAddress address) {
        LOGGER.info("Disconnect from: {}", address);
    }

    private void handleMessage(Msg msg) {
        SocketAddress address = msg.address();
        Message message = msg.message();
        if (message instanceof MessageProto.Connection conn) {
            if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.REQUEST) {
                LOGGER.info("Connection request from {}", address);
                Client client = new Client(address, clients.size());
                LOGGER.info("Client id: {}", client.id());
                clients.add(client);
                // TODO: This is nasty.
                Player player = new Remote(level, Flag.values()[client.id() % 3], 1000);
                LOGGER.info("Player flag: {}", player.getFlag());
                players.add(player);
                assert player.getId() == client.id;
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
            } else {
                LOGGER.warn("Should not happen, client sent response.");
            }
        } else if (message instanceof MessageProto.Action action) {
            LOGGER.info("It is an action {}", action.getActionCase());
            //TODO: Handle cases
        } else if (message instanceof MessageProto.State || message instanceof MessageProto.Event) {
            LOGGER.warn("Should not happen, client sent or event");
        }
    }

    private void onMsgReceived(SocketAddress address, Message message) {
        messageQueue.put(new Msg(address, message));
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Server()).execute(args);
        System.exit(exitCode);
    }

    record Client(SocketAddress address, int id) {

    }

    record Msg(SocketAddress address, Message message) {

    }

    enum State {
        Lobby,
        InGame,
        Done
    }
}
