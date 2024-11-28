
package alcatraz.shared;

import alcatraz.shared.Lobby;

public class LockedLobby {
    public final long id;
    public final String secret;

    public LockedLobby(long lobbyId, String secret) {
        this.id = lobbyId;
        this.secret = secret;
    }
}