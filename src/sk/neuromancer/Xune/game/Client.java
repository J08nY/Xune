package sk.neuromancer.Xune.game;

import com.github.quantranuk.protobuf.nio.ProtoChannelFactory;
import com.github.quantranuk.protobuf.nio.ProtoSocketChannel;
import org.lwjgl.system.Library;
import picocli.CommandLine;
import sk.neuromancer.Xune.network.Utils;
import sk.neuromancer.Xune.proto.MessageProto;

import static java.lang.Thread.sleep;

@CommandLine.Command(name = "XuneClient", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025 client.")
public class Client implements Runnable {

    @CommandLine.Option(names = {"-h", "--host"}, description = "Host to connect to.", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to connect to.", defaultValue = "7531")
    private int port;

    private ProtoSocketChannel clientChannel;

    @Override
    public void run() {
        setup();

        clientChannel.connect();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        clientChannel.sendMessage(MessageProto.Note.newBuilder().setType(MessageProto.NoteType.CONNECT).build());
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        clientChannel.sendMessage(MessageProto.Note.newBuilder().setType(MessageProto.NoteType.DISCONNECT).build());
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        clientChannel.disconnect();
    }

    private void setup() {
        Library.initialize();

        ProtoChannelFactory.ClientBuilder clientBuilder = ProtoChannelFactory.newClient(host, port).setSerializer(Utils.getIdSerializer());
        clientChannel = clientBuilder.build();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Client()).execute(args);
        System.exit(exitCode);
    }
}
