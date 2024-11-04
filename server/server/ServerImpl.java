package server;

import java.util.HashMap;
import java.util.Map;

public class ServerImpl implements ServerServer {
    private final int serverId;
    private static final Map<Integer, ServerServer> serverGroup = new HashMap<>();
    private boolean isPrimary = false;

    public ServerImpl(int serverId) {
        this.serverId = serverId;
    }


    @Override
    public void electNewPrimary() {
        int highestId = serverGroup.keySet().stream().max(Integer::compareTo).orElse(-1);
        if (highestId == serverId) {
            isPrimary = true;
            System.out.println("Server " + serverId + " is now the primary.");
        } else {
            isPrimary = false;
            System.out.println("Server " + serverId + " is a backup.");
        }
    }

    @Override
    public void update() {
        if (isPrimary) {
            System.out.println("Primary Server " + serverId + " is updating state across all servers.");
        } else {
            System.out.println("Backup Server " + serverId + " is receiving updates.");
        }
    }

    @Override
    public void forwardToPrimary() {
        if (!isPrimary) {
            int primaryId = serverGroup.keySet().stream().max(Integer::compareTo).orElse(-1);
            System.out.println("Server " + serverId + " is forwarding request to Primary Server " + primaryId);
        } else {
            System.out.println("Server " + serverId + " is the primary and processes the request directly.");
        }
    }

    @Override
    public void joinServerGroup() {
        serverGroup.put(serverId, this);
        System.out.println("Server " + serverId + " has joined the server group.");
    }
}