package sk.neuromancer.Xune.game;

import picocli.CommandLine;
import sk.neuromancer.Xune.proto.MessageProto;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

@CommandLine.Command(name = "XuneClient", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025 client.")
public class Client implements Runnable {

    @CommandLine.Option(names = {"-h", "--host"}, description = "Host to connect to.", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to connect to.", defaultValue = "7531")
    private int port;

    @Override
    public void run() {
        connectToServer();
    }

    private void connectToServer() {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to the server");

            // Create a protobuf message
            MessageProto.Message message = MessageProto.Message.newBuilder()
                    .build();

            // Send the message to the server
            try (OutputStream output = socket.getOutputStream()) {
                message.writeTo(output);
                System.out.println("Message sent to the server: " + message);
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Client()).execute(args);
        System.exit(exitCode);
    }
}
