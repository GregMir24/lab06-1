package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private CollectionManager manager;
    private DatabaseManager dbManager;
    private boolean running;
    private ServerSocket serverSocket;
    private ExecutorService clientHandlerPool;

    public Server() {
        this.dbManager = new DatabaseManager();
        this.manager = new CollectionManager(dbManager);
        this.clientHandlerPool = Executors.newCachedThreadPool();
        this.running = true;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Сервер завершается...");
            stop();
        }));
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(8787);
            System.out.println("Сервер запущен на порту 8787");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился");

                clientHandlerPool.submit(() -> {
                    try {
                        ClientHandler handler = new ClientHandler(clientSocket, manager, dbManager);
                        handler.handle();
                    } catch (Exception e) {
                        System.err.println("Ошибка: " + e.getMessage());
                        try { clientSocket.close(); } catch (IOException ex) {}
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            clientHandlerPool.shutdown();
        } catch (IOException e) {}
    }

    public static void main(String[] args) {
        new Server().start();
    }
}