package core;

import database.DatabaseManager;
import utils.ConfigLoader;
import utils.Logger;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int DEFAULT_PORT = 12345;
    private static int port;

    public static void main(String[] args) {
        // Cargar configuración
        ConfigLoader.load();
        port = ConfigLoader.getInt("server.port", DEFAULT_PORT);
        String dbUrl = ConfigLoader.get("db.url", "jdbc:sqlite:messenger.db");

        try {
            DatabaseManager.connect(dbUrl);
            DatabaseManager.initializeDatabase();
            Logger.log("Base de datos conectada: " + dbUrl);

            ServerSocket serverSocket = new ServerSocket(port);
            Logger.log("Servidor escuchando en el puerto " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ThreadPoolManager.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            Logger.log("Error fatal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.close();
            ThreadPoolManager.shutdown();
        }
    }
}