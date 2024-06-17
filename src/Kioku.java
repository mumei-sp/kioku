import commands.CommandEvaluator;
import connection.RESPParser;
import connection.TCPAsyncServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Kioku {

    public Kioku(int port) {
        TCPAsyncServer tcpServer = new TCPAsyncServer(port);
        System.out.println("Server started. Listening on port " + port);

        __executeCommands(tcpServer.getServerSocket());
    }

    private void __executeCommands(ServerSocket serverSocket) {
        while (true) {
            try {
                Socket socket = serverSocket.accept(); // Wait for new client connection
                System.out.println("Connected to new client.");

                // Start a new thread to handle client communication
                new Thread(() -> {
                    try (InputStream inputStream = socket.getInputStream();
                         OutputStream outputStream = socket.getOutputStream();
                         PrintWriter writer = new PrintWriter(outputStream, true);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                        String inputLine;
                        while ((inputLine = reader.readLine()) != null) {
                            // Process client input
                            String[] inputArray = RESPParser.decode(reader, inputLine);
                            Object result = CommandEvaluator.evaluate(inputArray[0], inputArray);

                            // Send response back to client
                            writer.print(RESPParser.encode(result));
                            writer.flush();
                        }
                    } catch (IOException e) {
                        System.out.println("I/O error with client: " + e.getMessage());
                    } catch (Exception e){
                        System.out.println("Error occured " + e.getMessage());
                    } finally {
                        try {
                            socket.close(); // Close the client socket when done
                        } catch (IOException e) {
                            System.out.println("Error closing socket: " + e.getMessage());
                        }
                    }
                }).start(); // Start the thread to handle this client
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8440;
        new Kioku(port);
    }

}
