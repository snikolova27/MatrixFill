package Client;

import Server.Server;
import utils.Utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Scanner;

import static utils.Utils.*;

public class Client {
    //    private DataInputStream input = null;
//    private DataOutputStream output = null;
//    private Socket socket;
//    private final String address;
//    private final int port;
    private static final int BUFFER_SIZE = 200000;
    private static ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(LOCALHOST, SERVER_PORT));

            System.out.println("Client connected to server.");
            System.out.println(welcome());
            System.out.println(helpHandler());

            while (true) {
                System.out.println("> ");
                String input = scanner.nextLine();

                if (input.equals("over")) {
                    System.out.println("Disconnecting from server.");
                    break;
                }

                buffer.clear();
                buffer.put(input.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, StandardCharsets.UTF_8);

                System.out.println(reply);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
