package core;



import database.DatabaseManager;

import utils.ConfigLoader;

import utils.Logger;

import utils.NetworkUtils;



import java.net.InetAddress;

import java.net.ServerSocket;

import java.net.Socket;



public class Server {

    private static final int DEFAULT_PORT = 12346;

    private static final String DEFAULT_BIND = "0.0.0.0";

    private static int port;



    public static void main(String[] args) {

        ConfigLoader.load();

        port = ConfigLoader.getInt("server.port", DEFAULT_PORT);

        String bindHost = ConfigLoader.get("server.bind", DEFAULT_BIND);

        String dbUrl = ConfigLoader.get("db.url", "jdbc:sqlite:messenger.db");



        try {

            DatabaseManager.connect(dbUrl);

            DatabaseManager.initializeDatabase();

            Logger.log("Base de datos conectada: " + dbUrl);



            InetAddress bindAddress = InetAddress.getByName(bindHost);

            ServerSocket serverSocket = new ServerSocket(port, 50, bindAddress);

            Logger.log("Servidor escuchando en " + bindHost + ":" + port);

            Logger.log("Clientes en la misma red pueden conectarse usando: " + NetworkUtils.formatLocalAccess(port));



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


