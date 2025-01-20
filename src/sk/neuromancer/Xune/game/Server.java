package sk.neuromancer.Xune.game;

import org.lwjgl.system.Library;
import picocli.CommandLine;
import sk.neuromancer.Xune.proto.MessageProto;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;


@CommandLine.Command(name = "XuneServer", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025 server.")
public class Server implements Runnable {

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to listen on.", defaultValue = "7531")
    private int port;

    @Override
    public void run() {
        listen();
    }

    private void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("New client connected");
                    handleClient(socket);
                } catch (IOException e) {
                    System.err.println("Client connection error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (InputStream input = socket.getInputStream()) {
            MessageProto.Message message = MessageProto.Message.parseFrom(input);
            System.out.println("Received message: " + message);
            // Handle the message as needed
        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        Library.initialize();
        int exitCode = new CommandLine(new Server()).execute(args);
        System.exit(exitCode);
    }
}
