package alcatraz.client;

import alcatraz.shared.exceptions.DuplicateNameException;
import alcatraz.shared.exceptions.TooManyLobbiesException;
import alcatraz.shared.interfaces.ClientInterface;
import alcatraz.shared.interfaces.ServerInterface;
import alcatraz.shared.rmi.RMI;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.utils.Player;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Map;

public class ServerWrapper {
    private ArrayList<RMI> rmis;

    public ServerWrapper(ArrayList<RMI> rmis) {
        this.rmis = rmis;
    }

    public <T> T execute(ServerOperation<T> operation) throws RemoteException {
        RemoteException lastException = null;
        for (RMI rmi : rmis) {

            ServerInterface server = null;
            try {
                server = (ServerInterface) LocateRegistry.getRegistry(rmi.ip, rmi.port).lookup("Alcatraz");
            } catch (NotBoundException|RemoteException e) {
                System.err.println("Server operation failed, trying next server...");
                continue;
            }

            return operation.execute(server);
        }
        throw new RemoteException("All servers failed.", lastException);
    }

    public void registerPlayer(String clientName) throws RemoteException {
        this.execute(server -> {
            ClientInterface client = new Client(server, clientName);
            server.registerPlayer(clientName, client);
            return null;
        });
    }



    public LobbyKey createLobby(String ownerName) throws RemoteException {
        return this.execute(server -> {
            LobbyKey lobbyKey = server.createLobby(ownerName);
            System.out.println("Lobby created with ID: " + lobbyKey.lobbyId);
            return lobbyKey;
        });
    }

    public void joinLobby(String clientName, Long lobbyId) throws RemoteException {
        this.execute(server -> {
            server.joinLobby(clientName, Long.valueOf(lobbyId));
            return null;
        });
    }

    public Map<Long, Lobby> getLobbies(String clientName) throws RemoteException {
        return this.execute(server -> server.getLobbies(clientName));
    }

    public void leaveLobby(String clientName) throws RemoteException {
        this.execute(server->{
            server.leaveLobby(clientName);
            return null;
        });
    }

    public ArrayList<Player> initializeGameStart(long lobbyId, String secret) throws RemoteException {
        return this.execute(server -> server.initializeGameStart(lobbyId, secret));
    }

    @FunctionalInterface
    public interface ServerOperation<T> {
        T execute(ServerInterface server) throws RemoteException;
    }
}
