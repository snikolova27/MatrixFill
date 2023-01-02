import Server.Server;

import java.io.IOException;

import static utils.Utils.SERVER_PORT;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(SERVER_PORT);

        server.start();
    }
}
