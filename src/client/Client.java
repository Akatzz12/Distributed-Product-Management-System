package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String NAME_SERVER_HOST = "localhost"; 
    private static final int NAME_SERVER_PORT = 6010; 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter command (PUT <category> <product> <details> | GET <category> <product> | DEL <category> <product> | EXIT): ");
            String command = scanner.nextLine().trim();
            
            if (command.equalsIgnoreCase("EXIT")) {
                System.out.println("Exiting client...");
                break;
            }

            String[] parts = command.split(" ", 4); 

            if (parts.length < 3) {
                System.out.println("Invalid command format. Try again.");
                continue;
            }

            String operation = parts[0].toUpperCase();
            String category = parts[1];
            String product = parts[2];
            String details = (parts.length == 4) ? parts[3] : null; 

            // Validate commands before contacting NameServer
            if (operation.equals("PUT") && details == null) {
                System.out.println("Error: PUT command requires product details.");
                continue;
            }
            if ((operation.equals("GET") || operation.equals("DEL")) && details != null) {
                System.out.println("Error: GET and DEL commands should not have product details.");
                continue;
            }

            // Look up DatabaseServer from NameServer
            String serverAddress = lookupServer(category);
            if (serverAddress == null || serverAddress.equals("NOT_FOUND")) {
                System.out.println("Error: No server found for category " + category);
                continue;
            }

            // Extract host and port from the response
            String[] addressParts = serverAddress.split(":");
            if (addressParts.length != 2) {
                System.out.println("Invalid address format from NameServer.");
                continue;
            }

            String dbServerHost = addressParts[0];
            int dbServerPort;

            try {
                dbServerPort = Integer.parseInt(addressParts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid port number received from NameServer.");
                continue;
            }

            // Send request to the DatabaseServer
            String response = communicateWithDatabaseServer(dbServerHost, dbServerPort, command);
            System.out.println("Server Response: " + response);
        }
        scanner.close();
    }

    private static String lookupServer(String category) {
        try (Socket socket = new Socket(NAME_SERVER_HOST, NAME_SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("LOOKUP " + category);
            String response = in.readLine();

            if (response == null || response.trim().isEmpty()) {
                return "NOT_FOUND";
            }
            return response;
        } catch (IOException e) {
            System.out.println("Error connecting to NameServer: " + e.getMessage());
            return null;
        }
    }

    private static String communicateWithDatabaseServer(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(message);
            return in.readLine();
        } catch (IOException e) {
            return "Error communicating with DatabaseServer: " + e.getMessage();
        }
    }
}
