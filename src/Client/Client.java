package Client;

import Server.Server;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import static utils.Utils.LOCALHOST;
import static utils.Utils.SERVER_PORT;

public class Client extends Thread {
    private DataInputStream input = null;
    private DataOutputStream output = null;
    private Socket socket;
    private final String address;
    private final int port;


    public Client(String address, int port) {
        this.port = port;
        this.address = address;
        try {
            this.socket = new Socket(this.address, this.port);
            System.out.println("Connected");

            // чете от терминала
            this.input = new DataInputStream(System.in);

            // изпраща изхода към сървъра
            this.output = new DataOutputStream(socket.getOutputStream());
            // чете от сървъра


        } catch (UnknownHostException uh) {
            System.out.println("Server not found: " + uh.getMessage());
        } catch (IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }

        // за запазване на съобщението от входа
        String currentLine = "";

        // четем докато потребителят не напише over
        while (!currentLine.equals("over")) {
            try {
                currentLine = this.input.readLine();
                this.output.writeUTF(currentLine);
            } catch (IOException e) {
                Utils.handleException(e);
            }
        }


        // затваряне на клиента
        try {
            this.input.close();
            this.output.close();
            this.socket.close();
        } catch (IOException e) {
            Utils.handleException(e);
        }
    }


    public static void main(String[] args) {
        Client client = new Client(LOCALHOST, SERVER_PORT);
    }
}
