import server.DatabaseServer;
import server.NameServer;

public class Main {
    public static void main(String[] args) {
        // Start the NameServer
        new Thread(() -> {
            try {
                NameServer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Dynamically start DatabaseServers for product categories
        for (String category : args) {
            final String productCategory = category;
            new Thread(() -> {
                try {
                    DatabaseServer.main(new String[]{productCategory});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
