package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class DatabaseServer {
    private static HashMap<String, String> productMap = new HashMap<>();
    private static String category;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java DatabaseServer <category>");
            return;
        }

        category = args[0];

        try (ServerSocket serverSocket = new ServerSocket(0)) { // Assigns a free port dynamically
            int assignedPort = serverSocket.getLocalPort();
            System.out.println(category + " Server running on port " + assignedPort);

            // Register this database server in the NameServer
            registerWithNameServer(category, assignedPort);

            while (true) {
                Socket socket = serverSocket.accept();
                new DatabaseHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerWithNameServer(String category, int port) {
        try (Socket socket = new Socket("localhost", 6010); // NameServer is on port 6010
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("REGISTER " + category + " localhost:" + port);
            System.out.println("Registered " + category + " with NameServer.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class DatabaseHandler extends Thread {
        private Socket socket;

        public DatabaseHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String request = in.readLine(); // Example: PUT electronics phone "iPhone 15"
                System.out.println("Received: " + request);

                String[] parts = request.split(" ", 3);
                if (parts.length >= 2) {
                    String operation = parts[0];
                    String product = parts[1];

                    switch (operation.toUpperCase()) {
                        case "PUT":
                            if (parts.length == 3) {
                                productMap.put(product, parts[2]);
                                out.println("INSERTED " + product);
                            } else {
                                out.println("ERROR: Missing product details");
                            }
                            break;
                        case "GET":
                            out.println(productMap.getOrDefault(product, "NOT_FOUND"));
                            break;
                        case "DEL":
                            if (productMap.remove(product) != null) {
                                out.println("DELETED " + product);
                            } else {
                                out.println("NOT_FOUND");
                            }
                            break;
                        default:
                            out.println("ERROR: Invalid command");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
