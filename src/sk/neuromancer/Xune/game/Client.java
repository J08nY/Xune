package sk.neuromancer.Xune.game;

import com.github.quantranuk.protobuf.nio.ProtoChannelFactory;
import com.github.quantranuk.protobuf.nio.ProtoSocketChannel;
import com.google.protobuf.Message;
import org.lwjgl.system.Library;
import picocli.CommandLine;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.game.players.Human;
import sk.neuromancer.Xune.graphics.HUD;
import sk.neuromancer.Xune.graphics.LevelView;
import sk.neuromancer.Xune.graphics.Window;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.network.Utils;
import sk.neuromancer.Xune.network.controllers.RemoteController;
import sk.neuromancer.Xune.proto.MessageProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Config.TPS;

@CommandLine.Command(name = "XuneClient", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025 client.")
public class Client implements Runnable {

    @CommandLine.Option(names = {"-h", "--host"}, description = "Host to connect to.", defaultValue = "localhost")
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
    private State state;
    private InputHandler input;
    private SoundManager sound;

    private ProtoSocketChannel clientChannel;
    private LinkedBlockingQueue<Message> messageQueue;

    @Override
    public void run() {
        setup();

        clientChannel.connect();

        clientChannel.sendMessage(MessageProto.Connection.newBuilder().setRequest(MessageProto.ConnectionRequest.newBuilder().build()).build());

        double nsPerTick = 1000000000.0 / TPS;
        int ticks = 0;
        long lastSecond = System.currentTimeMillis();

        while (keepRunning) {
            try {
                Message msg = messageQueue.poll(Math.round(nsPerTick), TimeUnit.NANOSECONDS);
                if (msg != null) {
                    handleMessage(msg);
                }
                tick();
                ticks++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSecond > 1000) {
                lastSecond = currentTime;
                System.out.println(ticks + " ticks");
                ticks = 0;
            }
        }

        clientChannel.disconnect();

        quit();
    }

    private void setup() {
        Library.initialize();
        SpriteSheet.initSheets();
        Entity.initClasses();

        window = new Window(fullscreen, false);
        sound = new SoundManager();
        input = new InputHandler(window);
        state = State.Init;

        ProtoChannelFactory.ClientBuilder clientBuilder = ProtoChannelFactory.newClient(host, port).setSerializer(Utils.getIdSerializer());
        clientChannel = clientBuilder.build();
        clientChannel.addMessageReceivedHandler(this::onMessageReceived);

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

    private void tick() {
        tickCount++;
        glfwPollEvents();

        switch (state) {
            case Init, Lobby, Done:
                break;
            case InGame:
                level.tick(tickCount);
                view.tick(tickCount);
                hud.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                break;
        }

        if (glfwWindowShouldClose(window.getHandle())) {
            keepRunning = false;
        }

        render();
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
                System.out.println("RTT: " + rtt + "ms");
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.RESPONSE) {
                this.id = conn.getResponse().getPlayerId();
                this.state = State.Lobby;
            } else if (conn.getConnectionCase() == MessageProto.Connection.ConnectionCase.REQUEST) {
                System.out.println("Should not happen, received connection request.");
            }

        } else if (message instanceof MessageProto.Event event) {
            if (event.getEventCase() == MessageProto.Event.EventCase.GAMESTART) {
                level = new Level(event.getGameStart().getLevel(), id);
                human = level.getHuman();
                hud = new HUD(input, window);
                view = new LevelView(input, window, hud);

                level.setView(view);
                human.setController(new RemoteController(clientChannel));
                human.setInput(input);
                human.setView(view);
                human.setHud(hud);
                hud.setView(view);
                hud.setLevel(level);
                view.setLevel(level, true);

                window.show();
                this.state = State.InGame;
            } else if (event.getEventCase() == MessageProto.Event.EventCase.GAMEEND) {
                keepRunning = false;
            }
        } else if (message instanceof MessageProto.State state) {
            level.deserializeTransient(state.getLevel());
        } else if (message instanceof MessageProto.Action action) {
            System.out.println("Should not happen, received action.");
        }
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
}
