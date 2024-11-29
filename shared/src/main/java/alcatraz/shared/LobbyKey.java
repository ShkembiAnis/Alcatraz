
package alcatraz.shared;

public class LobbyKey {
    public final long lobbyId;
    public final String secret;

    public LobbyKey(long lobbyId, String secret) {
        this.lobbyId = lobbyId;
        this.secret = secret;
    }
}