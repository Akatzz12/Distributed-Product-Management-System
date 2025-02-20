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

        category = args[0].toLowerCase();
        categoryMap.putIfAbsent(category, new HashMap<>());

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int assignedPort = serverSocket.getLocalPort();
            System.out.println(category + " Server running on port " + assignedPort);

            // Register with NameServer
            String registrationResponse = registerWithNameServer(category, assignedPort);
            if (registrationResponse.contains("ERROR")) {
                System.out.println(registrationResponse);
                return;
            }

            while (true) {
                Socket socket = serverSocket.accept();
                new DatabaseHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String registerWithNameServer(String category, int port) {
        try (Socket socket = new Socket("localhost", 6010);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("REGISTER " + category + " localhost:" + port);
            return in.readLine();
        } catch (IOException e) {
            return "ERROR: Unable to register with NameServer.";
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

                String[] parts = request.split(" ", 4);
                if (parts.length >= 3) {
                    String operation = parts[0].toUpperCase();
                    String receivedCategory = parts[1].toLowerCase();
                    String product = parts[2].toLowerCase();

                    categoryMap.putIfAbsent(receivedCategory, new HashMap<>());

                    switch (operation) {
                        case "PUT":
                            if (parts.length == 4) {
                                String details = parts[3];

                                // Check if the product already exists
                                if (categoryMap.get(receivedCategory).containsKey(product)) {
                                    out.println("ERROR: Product '" + product + "' already exists.");
                                } else if (categoryMap.get(receivedCategory).containsValue(details)) {
                                    out.println("ERROR: Product with same details already exists.");
                                } else {
                                    categoryMap.get(receivedCategory).put(product, details);
                                    out.println("INSERTED " + product);
                                }
                            } else {
                                out.println("ERROR: Missing product details");
                            }
                            break;

                        case "GET":
                            if (categoryMap.containsKey(receivedCategory) && categoryMap.get(receivedCategory).containsKey(product)) {
                                out.println(categoryMap.get(receivedCategory).get(product));
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
