package sk.neuromancer.Xune.game;

import com.github.quantranuk.protobuf.nio.ProtoChannelFactory;
import com.github.quantranuk.protobuf.nio.ProtoSocketChannel;
import com.google.protobuf.Message;
import org.lwjgl.system.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.game.players.Human;
import sk.neuromancer.Xune.graphics.HUD;
import sk.neuromancer.Xune.graphics.LevelView;
import sk.neuromancer.Xune.graphics.Window;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.network.Utils;
import sk.neuromancer.Xune.network.controllers.CombinedController;
import sk.neuromancer.Xune.network.controllers.Controller;
import sk.neuromancer.Xune.network.controllers.LocalController;
import sk.neuromancer.Xune.network.controllers.RemoteController;
import sk.neuromancer.Xune.proto.MessageProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

@CommandLine.Command(name = "XuneClient", mixinStandardHelpOptions = true, versionProvider = Version.class, description = "Xune 2025 client.")
public class Client implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    @CommandLine.Option(names = {"-H", "--host"}, description = "Host to connect to.", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to connect to.", defaultValue = "7531")
    private int port;

    @CommandLine.Option(names = {"-f", "--fullscreen"}, description = "Run in fullscreen mode.")
    private boolean fullscreen;

    private Window window;
    private Level level;
    private Human human;
    private LevelView view;
    private HUD hud;
    private long id;

    private boolean keepRunning = true;
    private int tickCount;
    private int serverTickCount;
    private double nsPerTick = 1000000000.0 / Config.TPS;
    private State state;
    private InputHandler input;
    private SoundManager sound;

    private ProtoSocketChannel clientChannel;
    private ConnectionState connectionState;
    private LinkedBlockingQueue<Message> messageQueue;

    @Override
    public void run() {
        setup();

        clientChannel.connect();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        while (true) {
            if (connectionState != ConnectionState.Connected) {
                if (connectionState == ConnectionState.Disconnected) {
                    clientChannel = prepareChannel();
                    clientChannel.connect();
                }
                LOGGER.warn("Not connected, retrying in 1s");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

        clientChannel.sendMessage(MessageProto.Connection.newBuilder().setRequest(MessageProto.ConnectionRequest.newBuilder().build()).build());

        long lastTick = System.nanoTime();
        double unprocessed = 0;
        double nsPerFrame = 1000000000.0 / Config.FPS;
        int ticks = 0;
        int msgs = 0;
        int renders = 0;
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
            try {
                Message msg = messageQueue.poll(Math.round(nsPerFrame), TimeUnit.NANOSECONDS);
                if (msg != null) {
                    msgs++;
                    handleMessage(msg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            render();
            renders++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSecond > 1000) {
                lastSecond = currentTime;
                LOGGER.info("{} ticks, {} messages, {} fps, state: {}", ticks, msgs, renders, state);
                ticks = 0;
                msgs = 0;
                renders = 0;
            }
        }

        clientChannel.disconnect();

        quit();
    }

    private ProtoSocketChannel prepareChannel() {
        ProtoChannelFactory.ClientBuilder clientBuilder = ProtoChannelFactory.newClient(host, port).setSerializer(Utils.getIdSerializer());
        ProtoSocketChannel channel = clientBuilder.build();
        channel.addConnectionHandler(this::onConnect);
        channel.addDisconnectionHandler(this::onDisconnect);
        channel.addMessageReceivedHandler(this::onMessageReceived);
        return channel;
    }

    private void setup() {
        Library.initialize();
        SpriteSheet.initSheets();
        Entity.initClasses();

        window = new Window(fullscreen, false);
        sound = new SoundManager();
        input = new InputHandler(window);
        state = State.Init;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            keepRunning = false;
        }));

        clientChannel = prepareChannel();
        connectionState = ConnectionState.Connecting;
        messageQueue = new LinkedBlockingQueue<>();
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        switch (state) {
            case Init, Lobby, Done:
                break;
            case InGame:
                view.render();
                hud.render();
                break;
        }

        glfwSwapBuffers(window.getHandle());
    }

    private void adjustTickRate(int serverTickCount) {
        int tickDifference = serverTickCount - tickCount;
        if (tickDifference > 0) {
            // Client is behind, speed up
            nsPerTick = 1000000000.0 / (Config.TPS + Math.min(tickDifference, 10));
        } else if (tickDifference < 0) {
            // Client is ahead, slow down
            nsPerTick = 1000000000.0 / (Config.TPS - Math.min(-tickDifference, 10));
        } else {
            // Client is in sync
            nsPerTick = 1000000000.0 / Config.TPS;
        }
        //System.out.println("Tick rate: " + (1000000000.0 / nsPerTick));
    }

    private void tick() {
        tickCount++;
        glfwPollEvents();

        switch (state) {
            case Init, Lobby:
                break;
            case InGame:
                level.tick(tickCount);
                view.tick(tickCount);
                hud.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                break;
            case Done:
                keepRunning = false;
                break;
        }

        if (glfwWindowShouldClose(window.getHandle())) {
            keepRunning = false;
        }
    }

    private void quit() {
        SpriteSheet.destroySheets();
        window.quit();
        sound.quit();
        input.quit();
        glfwTerminate();
    }

    private void handleMessage(Message message) {
        if (message instanceof MessageProto.Connection conn) {
            if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.PING) {
                MessageProto.Ping ping = conn.getPing();
                MessageProto.Connection.Builder builder = MessageProto.Connection.newBuilder();
                builder.setPong(MessageProto.Pong.newBuilder().setPreviousTimestamp(ping.getTimestamp()).setTimestamp(System.currentTimeMillis()).build());
                clientChannel.sendMessage(builder.build());
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.PONG) {
                MessageProto.Pong pong = conn.getPong();
                long rtt = System.currentTimeMillis() - pong.getPreviousTimestamp();
                LOGGER.info("RTT: {}ms", rtt);
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.RESPONSE) {
                this.id = conn.getResponse().getPlayerId();
                this.state = State.Lobby;
                LOGGER.info("Connected with id {}", id);
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.REQUEST) {
                LOGGER.warn("Should not happen, received connection request.");
            }

        } else if (message instanceof MessageProto.Event event) {
            if (event.getEventCase() == MessageProto.Event.EventCase.GAMESTART) {
                level = new Level(event.getGameStart().getLevel(), id);
                human = level.getHuman();
                hud = new HUD(input, window);
                view = new LevelView(input, window, hud);
                Controller local = new LocalController(level, human);
                Controller remote = new RemoteController(clientChannel);
                Controller combined = new CombinedController(local, remote);

                level.setView(view);
                human.setController(combined);
                human.setInput(input);
                human.setView(view);
                human.setHud(hud);
                hud.setView(view);
                hud.setLevel(level);
                view.setLevel(level, true);
                Tile spawn = level.spawnOf(human);
                view.centerOn(spawn.getLevelX(), spawn.getLevelY());

                window.show();
                this.state = State.InGame;
                SoundManager.play(SoundManager.TRACK_DUNESHIFTER, true, 0.5f);
                LOGGER.info("Game started");
            } else if (event.getEventCase() == MessageProto.Event.EventCase.GAMEEND) {
                this.state = State.Done;
                MessageProto.GameEnd gameEnd = event.getGameEnd();
                LOGGER.info("Game ended: you {}.", gameEnd.getWinnerId() == id ? "won" : "lost");
            }
        } else if (message instanceof MessageProto.State state) {
            level.deserializeTransient(state.getLevel());
            serverTickCount = state.getLevel().getTickCount();
            if (serverTickCount > tickCount) {
                LOGGER.warn("Received state from the future: {}", serverTickCount - tickCount);
            } else if (serverTickCount < tickCount) {
                LOGGER.warn("Received state from the past: {}", serverTickCount - tickCount);
            } else {
                //LOGGER.info("Received state OK");
            }
            adjustTickRate(serverTickCount);
        } else if (message instanceof MessageProto.Action action) {
            LOGGER.warn("Should not happen, received action.");
        }
    }

    private void onConnect(SocketAddress address) {
        LOGGER.info("Connected to {}", address);
        connectionState = ConnectionState.Connected;
    }

    private void onDisconnect(SocketAddress address) {
        LOGGER.info("Disconnected from {}", address);
        connectionState = ConnectionState.Disconnected;
    }

    private void onMessageReceived(SocketAddress socketAddress, Message message) {
        messageQueue.offer(message);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Client()).execute(args);
        System.exit(exitCode);
    }

    enum State {
        Init,
        Lobby,
        InGame,
        Done
    }

    enum ConnectionState {
        Disconnected,
        Connecting,
        Connected
    }
}
