package sk.neuromancer.Xune.game;

import com.github.quantranuk.protobuf.nio.ProtoChannelFactory;
import com.github.quantranuk.protobuf.nio.ProtoServerSocketChannel;
import com.google.protobuf.Message;
import org.lwjgl.system.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import sk.neuromancer.Xune.network.Utils;
import sk.neuromancer.Xune.proto.MessageProto;

import java.io.IOException;
import java.net.SocketAddress;


@CommandLine.Command(name = "XuneServer", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025 server.")
public class Server implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to listen on.", defaultValue = "7531")
    private int port;

    private boolean keepRunning = true;
    private ProtoServerSocketChannel serverChannel;

    public static final int TPS = 60;

    @Override
    public void run() {
        setup();

        try {
            serverChannel.start();
        } catch (IOException ignored) {
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

        serverChannel.stop();
    }

    private void setup() {
        Library.initialize();

        ProtoChannelFactory.ServerBuilder builder = ProtoChannelFactory.newServer(port).setSerializer(Utils.getIdSerializer());
        serverChannel = builder.build();
        serverChannel.addMessageReceivedHandler(this::onMsgReceived);
        serverChannel.addConnectionHandler(this::onConnect);
        serverChannel.addDisconnectionHandler(this::onDisconnect);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            keepRunning = false;
        }));
    }

    private void tick() {

    }

    private void onConnect(SocketAddress address) {
        LOGGER.info("Client connected: {}", address);
    }

    private void onDisconnect(SocketAddress address) {
        LOGGER.info("Client disconnected: {}", address);
    }

    private void onMsgReceived(SocketAddress address, Message message) {
        if (message instanceof MessageProto.Note note) {
            LOGGER.info("It is a note {}", note.getType());
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
