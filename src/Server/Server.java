package Server;

import matrix.ConcurrentMatrixFill;
import utils.Utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

import static utils.Utils.SERVER_PORT;

public class Server {
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    private int countOfThreads;

    private ByteBuffer buffer;
    private Selector selector;
    private ConcurrentMatrixFill concurrentMatrixFill;

    public Server(int port) {
        // стартиране на сървъра и чакане на клиент да се свърже
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server has started.");
            System.out.println();


            System.out.println("Waiting for a client to connect...");
            System.out.println();

            Socket socket = server.accept();
            System.out.println("Client has been accepted.");
            System.out.println();

            // четем входа от клиентската страна
            this.input = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            // за изпращане на съобщения към клиента
            // зачистване след всяко изпратено съобщение
            this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            sendMessage(welcome());

            String currentLine = "";

            // четем съобщенията от клиента, докато той не ни изпрати съобщение "over"
            boolean willEnterThreadsNext = false;
            while (!currentLine.equals("over")) {
                try {
                    currentLine = this.input.readUTF();
                    System.out.println("> Incoming from client: " + currentLine);
                    System.out.println();
                    if (currentLine.equals("threads")) {
                        willEnterThreadsNext = true;
                    } else if (willEnterThreadsNext) {
                        this.countOfThreads = Integer.parseInt(currentLine);
                        sendMessage("Count of threads entered: " + this.countOfThreads);
                        this.concurrentMatrixFill = new ConcurrentMatrixFill(this.countOfThreads);
                        sendMessage("\n");
                        sendMessage("Matrix created with " + this.countOfThreads + " rows and " + Utils.MAX_COLS + " columns.");
                        sendMessage(handleMatrix());
                        willEnterThreadsNext = false;
                    } else if (currentLine.equals("help")) {
                        sendMessage(helpHandler());
                    }
                } catch (IOException | InterruptedException e) {
                    Utils.handleException(e);
                }
            }
            System.out.println("Closing connection.");
            sendMessage("Closing connection.");

            // затваряне на сървъра
            socket.close();
            this.input.close();

        } catch (IOException e) {
            Utils.handleException(e);
        }
    }

    private String handleMatrix() throws InterruptedException {
        return concurrentMatrixFill.execute();
    }

    private String helpHandler() {
        return """
                Available commands:
                 threads - after pressing enter on this command you must enter the number of threads to work with
                 help - get a list of the available commands
                 over - disconnect from the server""";
    }

    private String welcome() {
        return """
                Welcome to the Concurrent Matrix Fill Client-Server application. Its purpose is to fill a 2D matrix
                using multiple threads where each thread fills the matrix with a different number that is randomly generated.
                """;
    }

    private void sendMessage(String message) throws IOException {
        this.output.writeUTF("< Incoming message from server: ");
        this.output.writeUTF(message);
    }

    public static void main(String[] args) {
        Server server = new Server(SERVER_PORT);
    }
}
