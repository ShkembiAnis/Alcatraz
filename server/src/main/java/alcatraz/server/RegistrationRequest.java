package alcatraz.server;

import alcatraz.shared.ClientInterface;

import java.io.Serializable;

public class RegistrationRequest implements Serializable {
    private final String methodName;
    private String clientName;
    private ClientInterface client;
    private Long lobbyId;

    public RegistrationRequest(String methodName, String clientName) {
        this.methodName = methodName;
        this.clientName = clientName;
    }

    public RegistrationRequest(String methodName, String clientName, ClientInterface client) {
        this.methodName = methodName;
        this.clientName = clientName;
        this.client = client;
    }

    public RegistrationRequest(String methodName, String clientName, Long lobbyId) {
        this.methodName = methodName;
        this.clientName = clientName;
        this.lobbyId = lobbyId;
    }

    public RegistrationRequest(String methodName, Long lobbyId) {
        this.methodName = methodName;
        this.lobbyId = lobbyId;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClientName() {
        return clientName;
    }

    public ClientInterface getClient() {
        return client;
    }

    public Long getLobbyId() {
        return lobbyId;
    }
}
