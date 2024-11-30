
package alcatraz.shared.utils;

import java.io.Serializable;

public class LobbyKey implements Serializable {
    public final long lobbyId;
    public final String secret;

    public LobbyKey(long lobbyId, String secret) {
        this.lobbyId = lobbyId;
        this.secret = secret;
    }
}