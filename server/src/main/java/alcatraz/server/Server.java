package alcatraz.server;

import alcatraz.server.replication.ReplicationInterface;
import alcatraz.server.state.SharedState;
import alcatraz.shared.exceptions.*;
import alcatraz.shared.interfaces.ClientInterface;
import alcatraz.shared.interfaces.ServerInterface;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.utils.Player;

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
        if (!this.replication.isPrimary()) {
            // Forward the request to the primary server
            this.replication.getPrimaryServer().registerPlayer(clientName, client);
            return;
        }

        fairLock.lock();

        if (this.isPlayerRegistered(clientName)) {
            fairLock.unlock();
            throw new DuplicateNameException(clientName);
        }

        // Store the Player with ClientInterface
        this.state.players.put(clientName, new Player(client, clientName, "ip_address", "port"));
        System.out.println(clientName + " registered");

        this.replication.replicatePrimaryState();

        fairLock.unlock();
    }

    private boolean isPlayerRegistered(String clientName) {
        return this.state.players.containsKey(clientName);
    }

    @Override
    public LobbyKey createLobby(String ownerName) throws RemoteException {
        if (!this.replication.isPrimary()) {
            return this.replication.getPrimaryServer().createLobby(ownerName);
        }

        fairLock.lock();

        LobbyKey key = this.state.lobbyManager.createLobby(this.state.players.get(ownerName));
        System.out.println("Lobby " + key.lobbyId + " created by player " + ownerName);

        this.state.lobbyManager.addPlayerToLobby(key.lobbyId, this.state.players.get(ownerName));       //MM20241127: cannot throw

        this.replication.replicatePrimaryState();

        fairLock.unlock();

        return key;
    }

    @Override
    public void joinLobby(String clientName, Long lobbyId) throws RemoteException {
        if (!this.replication.isPrimary()) {
            this.replication.getPrimaryServer().joinLobby(clientName, lobbyId);
            return;
        }

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

        this.replication.replicatePrimaryState();

        fairLock.unlock();
    }

    @Override
    public void leaveLobby(String clientName) throws RemoteException {      //MM20241127: keep generic exception!
        if (!this.replication.isPrimary()) {
            this.replication.getPrimaryServer().leaveLobby(clientName);
            return;
        }

        this.fairLock.lock();

        this.state.lobbyManager.removePlayerFromLobby(clientName);

        this.replication.replicatePrimaryState();

        this.fairLock.unlock();
    }

    @Override
    public Map<Long, Lobby> getLobbies() {
        return this.state.lobbyManager.getAvailableLobbies();
    }

    @Override
    public ArrayList<Player> initializeGameStart(long lobbyId, String secret) throws RemoteException {
        if (!this.replication.isPrimary()) {
            return this.replication.getPrimaryServer().initializeGameStart(lobbyId, secret);
        }

        this.fairLock.lock();
        Lobby gameLobby = this.state.lobbyManager.getLobbyById(lobbyId);
        if (!gameLobby.checkSecret(secret)) {
            fairLock.unlock();
            throw new LobbyKeyIncorrect();
        }



        this.removeUnavailablePlayersFromLobby(gameLobby);

        if (!gameLobby.canBePlayed(secret)) {
            gameLobby.setAvailable();
            replication.replicatePrimaryState();
            fairLock.unlock();
            throw new NotEnoughPlayersException(lobbyId);
        }
        gameLobby.setUnavailable();
        this.replication.replicatePrimaryState();

        fairLock.unlock();

        return getListOfPlayers(gameLobby);
    }

    public static ArrayList<Player> getListOfPlayers(Lobby gameLobby) {
        Player owner = gameLobby.getPlayers().get(gameLobby.getOwner());

        // owner of the game should be the first player
        ArrayList<Player> players = new ArrayList<>(List.of(owner));

        // all the other players are added after the owner
        gameLobby.getPlayers().values().stream()
                .filter(player -> !player.getClientName().equals(gameLobby.getOwner()))
                .forEach(players::add);

        return players;
    }


    private void removeUnavailablePlayersFromLobby(Lobby gameLobby) {
        gameLobby.getPlayers().forEach((playerName, player) -> {
            try {
                ClientInterface client = player.getClient();
                if (client != null) {
                    System.out.println("Starting game for client: " + player.getClientName() + " in lobby " + gameLobby.getId());
//                    client.startGame(gameLobby);        //MM20241127: after today's discussion, this must be renamed to "isPresent()"
                } else {
                    gameLobby.removePlayer(playerName, true);
                    System.out.println("ClientInterface not found for player: " + player.getClientName());
                }
            } catch (LobbyLockedException e) { //MM20241128: do nothing when removePlayer fails?
//                TODO: Uncomment when the client implements the isPresent() method
//            } catch (RemoteException e) {
//                try {
//                    gameLobby.removePlayer(playerName, true);      //MM20241127: "true" prevents from exception
//                } catch (LobbyLockedException e1) { //MM20241128: do nothing when removePlayer fails?
//                }
            }
        });
    }

}