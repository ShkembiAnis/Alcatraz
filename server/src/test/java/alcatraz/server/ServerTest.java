package alcatraz.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import alcatraz.server.replication.Replication;
import alcatraz.shared.interfaces.ClientInterface;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ServerTest {

    private Replication mockReplication;
    private Server server;


    @BeforeEach
    void prepareTheObject() {
        mockReplication = mock(Replication.class);
    }

    @Test
    void test() {
        assertDoesNotThrow(() -> server = new Server(mockReplication));

        Lobby gameLobby = new Lobby(1, "owner", "secret");
        ClientInterface mockClient = mock(ClientInterface.class);

        Player owner = new Player(mockClient, "owner","ip_adresse", "0");
        Player notOwner1 = new Player(mockClient, "notOwner1","ip_adresse", "0");
        Player notOwner2 = new Player(mockClient, "notOwner2","ip_adresse", "0");
        Player notOwner3 = new Player(mockClient, "notOwner3","ip_adresse", "0");

        assertDoesNotThrow(() -> gameLobby.addPlayer(notOwner1));
        assertDoesNotThrow(() -> gameLobby.addPlayer(notOwner2));
        assertDoesNotThrow(() -> gameLobby.addPlayer(owner));
        assertDoesNotThrow(() -> gameLobby.addPlayer(notOwner3));

        List<Player> playerList = Server.getListOfPlayers(gameLobby);

        assertEquals(playerList.getFirst().getClientName(), "owner");
        assertEquals(playerList.size(), 4);
        assertTrue(playerList.contains(notOwner1));
        assertTrue(playerList.contains(notOwner2));
        assertTrue(playerList.contains(notOwner3));
    }
}