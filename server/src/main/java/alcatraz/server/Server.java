package alcatraz.server;

import alcatraz.server.replication.ReplicationInterface;
import alcatraz.server.state.SharedState;
import alcatraz.shared.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Server extends UnicastRemoteObject implements ServerInterface {

    private final SharedState state;
    private final ReplicationInterface replication;
    private final ReentrantLock fairLock = new ReentrantLock();     //MM20241124: simple lock would be enough and may be changed -> check locks in combination with exceptions

    public Server(ReplicationInterface replication) throws RemoteException {
        super();
        this.replication = replication;
        this.state = replication.getSharedState();
    }

    //MM20241121: implement appropriate exception objects
    @Override
    public void registerPlayer(String clientName, ClientInterface client) throws RemoteException {
        if (this.replication.isPrimary()) {
            fairLock.lock();
            if (this.isPlayerRegistered(clientName)) {
                //MM20241121: implement a dedicated exception class
                fairLock.unlock();
                throw new RemoteException();
            }

            // Store the Player with ClientInterface
            //MM20241121: use a mutex at this stage!
            this.state.players.put(clientName, new Player(client, clientName, "ip_address", "port"));
            System.out.println(clientName + " registered");
            this.replication.replicatePrimaryState();
            fairLock.unlock();
            // TODO: there should be an early return instead of else
            //MM20241121: why?
        } else {
            // Forward the request to the primary server
            this.replication.getPrimaryServer().registerPlayer(clientName, client);
        }
    }

    private boolean isPlayerRegistered(String clientName) {
        return this.state.players.containsKey(clientName);
    }

    @Override
    public LobbyKey createLobby(Player owner) throws RemoteException {
        if (this.replication.isPrimary()) {
            fairLock.lock();
            //MM20241121: This section needs a mutex
            LobbyKey key = this.state.lobbyManager.createLobby(owner);
            System.out.println("Lobby " + key.lobbyId + " create by player " + owner.getClientName());

            this.state.lobbyManager.addPlayerToLobby(key.lobbyId, owner);
            this.replication.replicatePrimaryState();
            fairLock.unlock();
            return key;
        } else {
            // Forward the request to the primary server
            //MM20241121: The server may not be available anymore when we return from the primary server
            //              -> Therefore the client must check if creation was successful when an exception was thrown!
            return this.replication.getPrimaryServer().createLobby(owner);
        }
    }

    @Override
    public void joinLobby(Player client, Long lobbyId) throws RemoteException {
        if (!isPlayerRegistered(client.getClientName())) {          //MM20241124: this should by answered by any server (due to replication after each successful step)
            throw new RemoteException("Player " + client.getClientName() + " is not registered");
        }

        if (this.replication.isPrimary()) {
            fairLock.lock();
            this.state.lobbyManager.addPlayerToLobby(lobbyId, this.state.players.get(client.getClientName()));

            // Replication logic: Update backups with the new lobbies state
            this.replication.replicatePrimaryState();
            fairLock.unlock();
        } else {
            // Forward the request to the primary server
            //MM20241121: The server may not be available anymore when we return from the primary server
            //              -> Therefore the client must check if creation was successful when an exception was thrown!
            this.replication.getPrimaryServer().joinLobby(client, lobbyId);
        }
    }

    @Override
    public void leaveLobby(Player client) throws RemoteException {
        if (this.replication.isPrimary()) {
            this.fairLock.lock();
            this.state.lobbyManager.removePlayerFromLobby(client.getClientName());

            this.replication.replicatePrimaryState();
        } else {
            this.replication.getPrimaryServer().leaveLobby(client);
        }
    }

    @Override
    public Map<Long, Lobby> getLobbies() throws RemoteException {
        return this.state.lobbyManager.getAvailableLobbies();
    }

    @Override
    public void initializeGameStart(long lobbyId, String secret) throws RemoteException {
        if (this.replication.isPrimary()) {
            this.fairLock.lock();
            if (this.state.lobbyManager.isLobbyReadyForGameStart(lobbyId, secret)) {
                Lobby lobby = this.state.lobbyManager.getLobbyById(lobbyId);
                lobby.setUnavailable();
                lobby.getPlayers().forEach((key, value) -> {
                try {
                    ClientInterface client = value.getClient();
                    if (client != null) {
                        System.out.println("Starting game for client: " + value.getClientName() + " in lobby " + lobby.getId());
                        client.startGame(lobby);
                    } else {
                        System.out.println("ClientInterface not found for player: " + value.getClientName());
                    }
                } catch (RemoteException e) {
                    fairLock.unlock();
                    throw new RuntimeException(e);
                }
                });
            }
            fairLock.unlock();
            // No need to replicate here if lobbies haven't changed
        } else {
            // Forward the request to the primary server
            this.replication.getPrimaryServer().initializeGameStart(lobbyId, secret);
        }

    }


}