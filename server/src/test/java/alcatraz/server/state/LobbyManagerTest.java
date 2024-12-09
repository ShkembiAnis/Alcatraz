package alcatraz.server.state;

import alcatraz.shared.exceptions.LobbyFullException;
import alcatraz.shared.exceptions.TooManyLobbiesException;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.utils.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LobbyManagerTest {

    private LobbyManager lobbyManager;
    Player lobbyOwner = new Player(null, "owner", "ip0", "20");
    List<Player> otherPlayers;


    @BeforeEach
    void initializeLobbyManager() {
        lobbyManager = new LobbyManager();
        otherPlayers = new ArrayList<>(List.of(
                new Player(null, "1", "ip1", "10"),
                new Player(null, "2", "ip2", "20"),
                new Player(null, "3", "ip3", "30"),
                new Player(null, "4", "ip4", "40")
        ));
    }

    @AfterEach
    void removeLobbyManager() {
        lobbyManager = null;
    }

    private LobbyKey createLobby() {
        final LobbyKey lobbyKey = assertDoesNotThrow(() -> lobbyManager.createLobby(lobbyOwner));
        assertNotNull(lobbyKey);
        assertEquals(lobbyKey.lobbyId, 1);
        assertNotNull(lobbyKey.secret);

        Lobby createdLobby = lobbyManager.getLobbyById(lobbyKey.lobbyId);
        assertNotNull(createdLobby);

        Player ownerInTheCreatedLobby = assertDoesNotThrow(() -> createdLobby.getPlayer(lobbyOwner.getClientName()));
        assertNotNull(ownerInTheCreatedLobby);

        return lobbyKey;
    }

    @Test
    void testCreateLobby_create_a_lobby() {
        createLobby();
    }

    private void makeLobbyFull(Long lobbyId) {
        List<Player> randomPlayers = new ArrayList<>(List.of(
                new Player(null, "100", "ip1", "10"),
                new Player(null, "101", "ip2", "20"),
                new Player(null, "102", "ip3", "30")
        ));

        for (Player player : randomPlayers) {
            assertDoesNotThrow(() -> lobbyManager.addPlayerToLobby(lobbyId, player));
        }
    }

    @Test
    void testCreateLobby_cannot_add_more_players() {
        LobbyKey lobbyKey = createLobby();
        makeLobbyFull(lobbyKey.lobbyId);
        assertThrows(LobbyFullException.class, () -> lobbyManager.addPlayerToLobby(lobbyKey.lobbyId, otherPlayers.getFirst()));
    }

    @Test
    void testAddPlayerToLobby_joining_a_lobby_that_a_player_is_currently_in() {
        final LobbyKey lobbyKey = assertDoesNotThrow(() -> lobbyManager.createLobby(lobbyOwner));

        assertNotNull(lobbyKey);

        assertDoesNotThrow(() -> lobbyManager.addPlayerToLobby(lobbyKey.lobbyId, lobbyOwner));
    }

}
