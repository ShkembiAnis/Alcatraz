
package alcatraz.shared.utils;

public class LockedLobby {
    public final long id;
    public final String secret;

    public LockedLobby(long lobbyId, String secret) {
        this.id = lobbyId;
        this.secret = secret;
    }
}