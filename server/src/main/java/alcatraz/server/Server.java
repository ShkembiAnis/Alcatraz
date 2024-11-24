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
    private final ReentrantLock fairLock = new ReentrantLock();     //MM20241124: simple lock would be enough and may be changed

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
            if (this.state.players.containsKey(clientName)) {
                //MM20241121: implement a dedicated exception class
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

    @Override
    public LobbyKey createLobby(Player owner) throws RemoteException {
        if (this.replication.isPrimary()) {
            fairLock.lock();
            //MM20241121: This section needs a mutex
            LobbyKey key = this.state.lobbyManager.createLobby(owner);
            System.out.println("Lobby created with ID: " + key.id + " by Player: " + owner.getClientName());

            this.state.lobbyManager.addPlayerToLobby(key.id, owner);
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

    // TODO: refactor nested if statements
    @Override
    public void joinLobby(Player client, Long lobbyId) throws RemoteException {
        if (this.replication.isPrimary()) {
            fairLock.lock();
            if (this.state.lobbyManager.getLobbies().containsKey(lobbyId)) {

                this.state.lobbyManager.addPlayerToLobby(lobbyId, this.state.players.get(client.getClientName()));
                System.out.println("Player " + client.getClientName() + " joined lobby " + lobbyId);

                // Replication logic: Update backups with the new lobbies state
                this.replication.replicatePrimaryState();
                fairLock.unlock();
            }
            else {
                fairLock.lock();
                throw new RemoteException();        //MM20241121: find or implement appropriate exception!
            }
        } else {
            // Forward the request to the primary server
            //MM20241121: The server may not be available anymore when we return from the primary server
            //              -> Therefore the client must check if creation was successful when an exception was thrown!
            this.replication.getPrimaryServer().joinLobby(client, lobbyId);
        }
    }

    @Override
    public Map<Long, Lobby> getLobbies() throws RemoteException {
        return this.state.lobbyManager.getLobbies();
    }


    @Override
    public void initializeGameStart(long lobbyId) throws RemoteException {
        if (this.replication.isPrimary()) {
            //MM20241124: do we need a lock here?
            Lobby lobby = this.state.lobbyManager.getLobbyById(lobbyId);
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
                    throw new RuntimeException(e);
                }
            });
            // No need to replicate here if lobbies haven't changed
        } else {
            // Forward the request to the primary server
            this.replication.getPrimaryServer().initializeGameStart(lobbyId);
        }

    }


    // TODO: method not used
    //MM20241124: unnecessary. will remove in next cycle after further checks
    private Boolean checkIfUsernameExists(String playerName) {
        //MM20241124: do we need a lock here? I would assume as otherwise we get corrupted values
        for (String key : this.state.players.keySet()) {
            if (playerName.equals(key)) {
                return true;
            }
        }
        return false;
    }


    // TODO: method not used

    /**
     * @param lobbies
     * @param playerId
     * @return true if player is not in a lobby, else false
     */
    private boolean isPlayerInAnyLobby(Map<Long, List<String>> lobbies, String playerId) {
        //MM20241124: I think this requires a lock as otherwise corrupted values will be returned
        for (List<String> players : lobbies.values()) {
            if (players.contains(playerId)) {
                return true;
            }
        }
        return false;
    }
}