package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class NameServer {
    private static final int PORT = 6010; // Port for NameServer
    private static HashMap<String, String> categoryMap = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("NameServer is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new NameServerHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void registerCategory(String category, String serverAddress) {
        categoryMap.put(category, serverAddress);
        System.out.println("Registered: " + category + " -> " + serverAddress);
    }

    public static String getServer(String category) {
        return categoryMap.get(category);
    }
}

class NameServerHandler extends Thread {
    private Socket socket;

    public NameServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request = in.readLine();
            System.out.println("Received: " + request);

            String[] parts = request.split(" ");
            if (parts.length >= 2) {
                String command = parts[0];
                String category = parts[1];

                if (command.equalsIgnoreCase("REGISTER") && parts.length == 3) {
                    String serverAddress = parts[2];
                    NameServer.registerCategory(category, serverAddress);
                    out.println("SUCCESS");
                } else if (command.equalsIgnoreCase("LOOKUP")) {
                    String serverAddress = NameServer.getServer(category);
                    out.println((serverAddress != null) ? serverAddress : "NOT_FOUND");
                } else {
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
