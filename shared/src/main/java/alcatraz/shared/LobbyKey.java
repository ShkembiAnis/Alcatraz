
package alcatraz.shared;

public class LobbyKey {
    public final long id;
    public final String secret;

    public LobbyKey(long lobbyId, String secret) {
        this.id = lobbyId;
        this.secret = secret;
    }
}