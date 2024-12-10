//package alcatraz.server;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import alcatraz.server.replication.Replication;
//import alcatraz.server.state.SharedState;
//import alcatraz.shared.interfaces.ClientInterface;
//import alcatraz.shared.utils.Lobby;
//import alcatraz.shared.utils.LobbyKey;
//import alcatraz.shared.utils.Player;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.rmi.RemoteException;
//import java.util.List;
//import java.util.Optional;
//
//class ServerTest {
//
//    private Replication mockReplication;
//    private Server server;
//
//
//    @BeforeEach
//    void getPrimaryServerObject() {
//        mockReplication = mock(Replication.class);
//        when(mockReplication.isPrimary()).thenReturn(true);
//        when(mockReplication.getSharedState()).thenReturn(new SharedState());
//        server = assertDoesNotThrow(() -> new Server(mockReplication));
//    }
//
//    private void registerPlayer(String clientName) {
//        assertDoesNotThrow(() -> server.registerPlayer(clientName, null));
//    }
//
//    private boolean isLobbyExisting(Long lobbyId, String clientName) {
//        return assertDoesNotThrow(server.getLobbies(clientName).containsKey(lobbyId));
//    }
//
//    private boolean isPlayerInLobby(Long lobbyId, String playerName) throws RemoteException {
//        Optional<Lobby> foundLobby = server.getLobbies(playerName).values().stream()
//                .filter(lobby -> lobby.id == lobbyId)
//                .findFirst();
//
//        assertTrue(foundLobby.isPresent());
//
//        Player ownerInTheCreatedLobby = assertDoesNotThrow(() -> foundLobby.get().getPlayer(playerName));
//        return ownerInTheCreatedLobby != null;
//    }
//
//    private LobbyKey createLobby(String ownerName) {
//        final LobbyKey lobbyKey = assertDoesNotThrow(() -> server.createLobby(ownerName));
//        assertNotNull(lobbyKey);
//        assertTrue(lobbyKey.lobbyId >= 1);
//        assertNotNull(lobbyKey.secret);
//
//        assertTrue(isPlayerInLobby(lobbyKey.lobbyId, ownerName));
//
//        return lobbyKey;
//    }
//
//    @Test
//    void testCreateLobby_create_a_lobby() {
//        Player lobbyOwner = new Player(null, "owner", "ip0", "20");
//        registerPlayer(lobbyOwner.getClientName());
//
//        createLobby(lobbyOwner.getClientName());
//    }
//
//    @Test
//    void testCreateLobby_create_new_lobby_being_in_another_one() {
//        Player lobbyOwner = new Player(null, "owner", "ip0", "20");
//        registerPlayer(lobbyOwner.getClientName());
//
//        // create first lobby
//        LobbyKey lobbyKey1 = createLobby(lobbyOwner.getClientName());
//        assertTrue(isPlayerInLobby(lobbyKey1.lobbyId, lobbyOwner.getClientName()));
//
//        // create second lobby
//        LobbyKey lobbyKey2 = createLobby(lobbyOwner.getClientName());
//        assertFalse(isLobbyExisting(lobbyKey1.lobbyId, lobbyOwner.getClientName()));
//        assertTrue(isPlayerInLobby(lobbyKey2.lobbyId, lobbyOwner.getClientName()));
//    }
//
//    @Test
//    void testAddPlayerToLobby_add_player_to_lobby() {
//        Player lobbyOwner = new Player(null, "owner", "ip0", "20");
//        registerPlayer(lobbyOwner.getClientName());
//
//        LobbyKey lobbyKey = createLobby(lobbyOwner.getClientName());
//
//        Player player = new Player(null, "player", "ip1", "10");
//        registerPlayer(player.getClientName());
//
//        assertDoesNotThrow(() -> server.joinLobby(player.getClientName(), lobbyKey.lobbyId));
//        assertTrue(isPlayerInLobby(lobbyKey.lobbyId, player.getClientName()));
//    }
//
//    @Test
//    void testAddPlayerToLobby_join_lobby_you_are_currently_in() {
//        Player lobbyOwner = new Player(null, "owner", "ip0", "20");
//        registerPlayer(lobbyOwner.getClientName());
//
//        LobbyKey lobbyKey = createLobby(lobbyOwner.getClientName());
//
//        assertDoesNotThrow(() -> server.joinLobby(lobbyOwner.getClientName(), lobbyKey.lobbyId));
//        assertTrue(isPlayerInLobby(lobbyKey.lobbyId, lobbyOwner.getClientName()));
//
//        Lobby lobby = server.getLobbies(lobbyOwner.getClientName()).get(lobbyKey.lobbyId);
//        assertEquals(lobby.getPlayers().size(), 1);
//    }
//
//    @Test
//    void testGetListOfPlayers() {
//        assertDoesNotThrow(() -> server = new Server(mockReplication));
//
//        Lobby gameLobby = new Lobby(1, "owner", "secret");
//        ClientInterface mockClient = mock(ClientInterface.class);
//
//        Player owner = new Player(mockClient, "owner","ip_adresse", "0");
//        Player notOwner1 = new Player(mockClient, "notOwner1","ip_adresse", "0");
//        Player notOwner2 = new Player(mockClient, "notOwner2","ip_adresse", "0");
//        Player notOwner3 = new Player(mockClient, "notOwner3","ip_adresse", "0");
//
//        assertDoesNotThrow(() -> gameLobby.addPlayer(notOwner1));
//        assertDoesNotThrow(() -> gameLobby.addPlayer(notOwner2));
//        assertDoesNotThrow(() -> gameLobby.addPlayer(owner));
//        assertDoesNotThrow(() -> gameLobby.addPlayer(notOwner3));
//
//        List<Player> playerList = Server.getListOfPlayers(gameLobby);
//
//        assertEquals(playerList.getFirst().getClientName(), "owner");
//        assertEquals(playerList.size(), 4);
//        assertTrue(playerList.contains(notOwner1));
//        assertTrue(playerList.contains(notOwner2));
//        assertTrue(playerList.contains(notOwner3));
//    }
//}