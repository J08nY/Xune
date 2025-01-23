package sk.neuromancer.Xune.game;

import com.github.quantranuk.protobuf.nio.ProtoChannelFactory;
import com.github.quantranuk.protobuf.nio.ProtoServerSocketChannel;
import com.google.protobuf.Message;
import org.lwjgl.system.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.graphics.Window;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.network.Utils;
import sk.neuromancer.Xune.proto.MessageProto;

import java.io.IOException;
import java.net.SocketAddress;


@CommandLine.Command(name = "XuneServer", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025 server.")
public class Server implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to listen on.", defaultValue = "7531")
    private int port;

    private Window window;
    private boolean keepRunning = true;
    private int tickCount;
    private Level level;
    private ProtoServerSocketChannel serverChannel;

    public static final int TPS = 60;

    @Override
    public void run() {
        setup();

        if (!start()) {
            return;
        }

        long lastRender = System.nanoTime();
        double unprocessed = 0;
        double nsPerTick = 1000000000.0 / TPS;
        int ticks = 0;
        long lastSecond = System.currentTimeMillis();

        while (keepRunning) {
            long now = System.nanoTime();
            unprocessed += (now - lastRender) / nsPerTick;
            lastRender = now;
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
        window = new Window();
        SpriteSheet.initSheets();
        Entity.initClasses();

        ProtoChannelFactory.ServerBuilder builder = ProtoChannelFactory.newServer(port).setSerializer(Utils.getIdSerializer());
        serverChannel = builder.build();
        serverChannel.addMessageReceivedHandler(this::onMsgReceived);
        serverChannel.addConnectionHandler(this::onConnect);
        serverChannel.addDisconnectionHandler(this::onDisconnect);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            keepRunning = false;
        }));

        level = new Level(Level.LEVEL_1);
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
        window.quit();
        serverChannel.stop();
    }

    private void tick() {
        tickCount++;
        level.tick(tickCount);
        serverChannel.sendMessageToAll(MessageProto.State.newBuilder().setLevel(level.serializeTransient()).build());
    }

    private void onConnect(SocketAddress address) {
        LOGGER.info("Connection from: {}", address);
    }

    private void onDisconnect(SocketAddress address) {
        LOGGER.info("Disconnect from: {}", address);
    }

    private void onMsgReceived(SocketAddress address, Message message) {
        if (message instanceof MessageProto.Note note) {
            LOGGER.info("It is a note {}", note.getType());
            if (note.getType() == MessageProto.NoteType.CONNECT) {
                LOGGER.info("Client connected: {}", address);
                // TODO: Add to list of clients.
            } else if (note.getType() == MessageProto.NoteType.DISCONNECT) {
                LOGGER.info("Client disconnected: {}", address);
                // TODO: Remove from list of clients.
            }
        } else if (message instanceof MessageProto.Action action) {
            LOGGER.info("It is an action {}", action.getActionCase());
        } else if (message instanceof MessageProto.State state) {
            LOGGER.warn("Should not happen, client sent state.");
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Server()).execute(args);
        System.exit(exitCode);
    }
}
