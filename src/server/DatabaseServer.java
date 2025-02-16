package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseServer {
    private static Map<String, Map<String, String>> categoryMap = new HashMap<>();
    private static String category;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java DatabaseServer <category>");
            return;
        }

        category = args[0].toLowerCase(); // Normalize category case
        categoryMap.putIfAbsent(category, new HashMap<>()); // Ensure category exists

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

                String request = in.readLine();
                System.out.println("Received: " + request);

                if (request == null || request.trim().isEmpty()) {
                    out.println("ERROR: Empty request");
                    return;
                }

                String[] parts = request.split(" ", 4); // Adjusted for correct splitting
                if (parts.length >= 3) {
                    String operation = parts[0].toUpperCase();
                    String receivedCategory = parts[1].toLowerCase();
                    String product = parts[2].toLowerCase();

                    // Ensure category exists
                    categoryMap.putIfAbsent(receivedCategory, new HashMap<>());

                    switch (operation) {
                        case "PUT":
                            if (parts.length == 4) {
                                categoryMap.get(receivedCategory).put(product, parts[3]); // Store product details
                                out.println("INSERTED " + product);
                            } else {
                                out.println("ERROR: Missing product details");
                            }
                            break;

                        case "GET":
                            if (categoryMap.containsKey(receivedCategory) && categoryMap.get(receivedCategory).containsKey(product)) {
                                out.println(categoryMap.get(receivedCategory).get(product)); // Return only product details
                            } else {
                                out.println("NOT_FOUND");
                            }
                            break;

                        case "DEL":
                            if (categoryMap.containsKey(receivedCategory) && categoryMap.get(receivedCategory).remove(product) != null) {
                                out.println("DELETED " + product);
                            } else {
                                out.println("NOT_FOUND");
                            }
                            break;

                        default:
                            out.println("ERROR: Invalid command");
                    }
                } else {
                    out.println("ERROR: Invalid request format");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
