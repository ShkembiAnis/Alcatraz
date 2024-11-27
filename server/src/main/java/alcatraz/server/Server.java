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

    @Override
    public void registerPlayer(String clientName, ClientInterface client) throws RemoteException {
        if (this.replication.isPrimary()) {
            fairLock.lock();
            if (this.isPlayerRegistered(clientName)) {
                fairLock.unlock();
                throw new DuplicateNameException(clientName);
            }

            // Store the Player with ClientInterface
            //MM20241121: use a mutex at this stage!
            this.state.players.put(clientName, new Player(client, clientName, "ip_address", "port"));
            System.out.println(clientName + " registered");
            this.replication.replicatePrimaryState();
            fairLock.unlock();
        } else {
            // Forward the request to the primary server
            this.replication.getPrimaryServer().registerPlayer(clientName, client);
        }
    }

    private boolean isPlayerRegistered(String clientName) {
        return this.state.players.containsKey(clientName);
    }

    @Override
    public LobbyKey createLobby(String ownerName) throws RemoteException {
        if (this.replication.isPrimary()) {
            fairLock.lock();
            //MM20241121: This section needs a mutex
            LobbyKey key = this.state.lobbyManager.createLobby(this.state.players.get(ownerName));
            System.out.println("Lobby " + key.lobbyId + " create by player " + ownerName);

            this.state.lobbyManager.addPlayerToLobby(key.lobbyId, this.state.players.get(ownerName));       //MM20241127: cannot throw
            this.replication.replicatePrimaryState();
            fairLock.unlock();
            return key;
        } else {
            return this.replication.getPrimaryServer().createLobby(ownerName);
        }
    }

    @Override
    public void joinLobby(String clientName, Long lobbyId) throws RemoteException {

        if (this.replication.isPrimary()) {
            fairLock.lock();

            if (!isPlayerRegistered(clientName)) {
                fairLock.unlock();
                throw new PlayerNotRegisteredException(clientName);
            }

            try {
                this.state.lobbyManager.addPlayerToLobby(lobbyId, this.state.players.get(clientName));
            } catch (RemoteException e) {
                this.fairLock.unlock();
                throw e;
            }
            // Replication logic: Update backups with the new lobbies state
            this.replication.replicatePrimaryState();
            fairLock.unlock();
        } else {
            this.replication.getPrimaryServer().joinLobby(clientName, lobbyId);
        }
    }

    @Override
    public void leaveLobby(String clientName) throws RemoteException {      //MM20241127: keep generic exception!
        if (this.replication.isPrimary()) {
            this.fairLock.lock();
            this.state.lobbyManager.removePlayerFromLobby(clientName);

            this.replication.replicatePrimaryState();
            this.fairLock.unlock();
        } else {
            this.replication.getPrimaryServer().leaveLobby(clientName);
        }
    }

    @Override
    public Map<Long, Lobby> getLobbies() {
        return this.state.lobbyManager.getAvailableLobbies();
    }

    @Override
    public void initializeGameStart(long lobbyId, String secret) throws RemoteException {
        if (this.replication.isPrimary()) {
            this.fairLock.lock();
            if (this.state.lobbyManager.getLobbyById(lobbyId).canBePlayed(secret)) {
                this.state.lobbyManager.getLobbyById(lobbyId).setUnavailable();
                this.state.lobbyManager.getLobbyById(lobbyId).getPlayers().forEach((key, value) -> {
                try {
                    ClientInterface client = value.getClient();
                    if (client != null) {
                        System.out.println("Starting game for client: " + value.getClientName() + " in lobby " + this.state.lobbyManager.getLobbyById(lobbyId).getId());
                        client.startGame(this.state.lobbyManager.getLobbyById(lobbyId));        //MM20241127: after today's discussion, this must be renamed to "isPresent()"
                    } else {
                        System.out.println("ClientInterface not found for player: " + value.getClientName());
                    }
                } catch (RemoteException e) {
                    this.state.lobbyManager.getLobbyById(lobbyId).removePlayer(key, true);      //MM20241127: "true" prevents from exception
                }
                });

                if (!this.state.lobbyManager.getLobbyById(lobbyId).canBePlayed(secret)) {
                    this.state.lobbyManager.getLobbyById(lobbyId).setAvailable();
                    replication.replicatePrimaryState();
                    fairLock.unlock();
                    throw new NotEnoughPlayersException(lobbyId);
                }
            }
            fairLock.unlock();
            // No need to replicate here if lobbies haven't changed
        } else {
            // Forward the request to the primary server
            this.replication.getPrimaryServer().initializeGameStart(lobbyId, secret);
        }

    }

}