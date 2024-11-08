package org.example;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            LobbyManager lobbyManager = new LobbyManager();
            Server server = new Server(lobbyManager);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Alcatraz", server);
            System.out.println("Server started. Waiting for clients...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
