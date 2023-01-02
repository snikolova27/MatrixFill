package Server;

import matrix.ConcurrentMatrixFill;
import utils.Utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static utils.Utils.*;

public class Server {
    //    private Socket socket = null;
//    private ServerSocket server = null;
//    private DataInputStream input = null;
//    private DataOutputStream output = null;
    private int countOfThreads;

    private ByteBuffer buffer;
    private Selector selector;
    private final int port;
    private ConcurrentMatrixFill concurrentMatrixFill;
    private static final int BUFFER_SIZE = 200000;
    private boolean isWorking = false;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        // стартиране на сървъра и чакане на клиент да се свърже
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            this.selector = Selector.open();
            this.configureServerSocketChannel(serverSocketChannel, this.selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            this.isWorking = true;

            System.out.println("Server has started");

            while (this.isWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        System.out.println("Client has disconnected.");
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = this.getClientInput(clientChannel);

                            System.out.println(clientInput);
                            if (clientInput == null) {
                                continue;
                                // ако прочетем командата over - спираме сървъра
                            } else if (clientInput.equals("over")) {
                                stop();
                                // ако прочетем командата за въвеждане на нишки, изчакваме клиентът
                                // да въведе брой нишки, създаваме матрицата и я запълваме
                                // първо чрез нишките след това последователно
                            } else if (clientInput.equals("threads")) {
                                this.writeClientOutput(clientChannel, "Please enter count of threads.");
                                clientInput = this.getClientInput(clientChannel);
                                assert clientInput != null;
                                while(clientInput.equals("")){
                                    clientInput = this.getClientInput(clientChannel);
                                }
                                this.countOfThreads = Integer.parseInt(clientInput);
                                this.concurrentMatrixFill = new ConcurrentMatrixFill(this.countOfThreads);
                                this.writeClientOutput(clientChannel, "Matrix created with " + this.countOfThreads + " rows and " + MAX_COLS + " columns.");
                                handleMatrix(clientChannel);

                                // команда за показване на помощното меню
                            } else if (clientInput.equals("help")) {
                                this.writeClientOutput(clientChannel, helpHandler());
                            }

                        } else if (key.isAcceptable()) {
                            System.out.println("The server has received an accept request");
                            this.accept(this.selector, key);
                        }

                        keyIterator.remove();

                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void stop() {
        this.isWorking = false;
        if (this.selector.isOpen()) {
            this.selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(LOCALHOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }
    private void handleMatrix(SocketChannel clientChannel) throws InterruptedException, IOException {
        writeClientOutput(clientChannel,concurrentMatrixFill.execute());
    }
    private String getClientInput(SocketChannel clientChannel) throws IOException {
        this.buffer.clear();

        int readBytes = clientChannel.read(this.buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        this.buffer.flip();
        byte[] clientInputBytes = new byte[this.buffer.remaining()];
        this.buffer.get(clientInputBytes);

        return new String(clientInputBytes);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        this.buffer.clear();
        this.buffer.put(output.getBytes());
        this.buffer.flip();

        clientChannel.write(this.buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = serverSocketChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }
}
