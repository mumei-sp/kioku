package connection;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class TCPAsyncServer implements AutoCloseable {
    private final int port;
    private ServerSocket serverSocket;

    public TCPAsyncServer(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    public int getPort() {
        return this.port;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    @Override
    public void close() throws IOException {
        if(!Objects.isNull(serverSocket)) serverSocket.close();
    }
}
