package alcatraz.server.replication;

import alcatraz.server.replication.dto.ReplicationDTO;
import alcatraz.server.rmi.RMIManager;
import alcatraz.server.spread.Spread;
import alcatraz.server.state.SharedState;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.Player;
import alcatraz.shared.interfaces.ServerInterface;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Replication implements ReplicationInterface, AdvancedMessageListener {

    // TODO: name is not relevant I think, ID would be enough
    public String serverName;
    public int serverId;
    private boolean isPrimary;

    private final SharedState sharedState;
    private final RMIManager rmiManager;
    private final Spread spread;

    public Replication(SharedState sharedState, RMIManager rmiManager, Spread spread, String serverName, int serverId) {
        this.serverName = serverName;
        this.serverId = serverId;
        this.isPrimary = false;

        this.sharedState = sharedState;
        this.rmiManager = rmiManager;
        this.spread = spread;
    }


    private void setSharedState(HashMap<Long, Lobby> lobbies, HashMap<String, Player> players) {
        this.sharedState.lobbyManager.setLobbies(lobbies);
        this.sharedState.players = players;
    }

    @Override
    public boolean isPrimary() {
        return this.isPrimary;
    }

    @Override
    public SharedState getSharedState() {
        return this.sharedState;
    }

    @Override
    public ServerInterface getPrimaryServer() {
        return this.rmiManager.getPrimaryServer();
    }

    @Override
    public void replicatePrimaryState() {
        ReplicationDTO replicationDTO = new ReplicationDTO(this.sharedState.lobbyManager.getAllLobbies(), this.sharedState.players);
        try {
            SpreadMessage msg = new SpreadMessage();
            msg.setReliable();
            msg.addGroup("ServerGroup");
            msg.setObject(replicationDTO);
            spread.connection.multicast(msg);

            // TODO: we use blocking algorithm, so we have to wait here until we get all the confirmations
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage message) {
        try {
            Object receivedObject = message.getObject();

            if (receivedObject instanceof ReplicationDTO replicationDTO) {
                // Received lobbies update from primary
                this.setSharedState(replicationDTO.getLobbies(), replicationDTO.getPlayers());
                System.out.println(this.serverName + " updated lobbies from primary server.");

            }
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void membershipMessageReceived(SpreadMessage message) {
        MembershipInfo info = message.getMembershipInfo();
        // TODO: what about other cases e.g. isCausedByNetwork ?
        if (info.isCausedByJoin() || info.isCausedByLeave() || info.isCausedByDisconnect()) {
            System.out.println("Membership change detected.");
            electNewPrimary(info);
        }
    }

    public void joinServerGroup(String serverName, int serverId) {
        try {
            // Include server ID in the private group name
            String privateGroupName = serverName + "_" + serverId;
            spread.connection.connect(InetAddress.getByName(spread.host), spread.port, privateGroupName, false, true);
            spread.connection.add(this);
            SpreadGroup group = new SpreadGroup();
            group.join(spread.connection, spread.groupName);
            System.out.println(this.serverName + " joined the group.");
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void electNewPrimary(MembershipInfo info) {
        SpreadGroup[] members = info.getMembers();
        int primaryId = this.serverId;
        String primaryName = this.serverName;
        System.out.println("Current server ID: " + this.serverId + ", Name: " + this.serverName);
        System.out.println("Members in group:");

        // TODO: this should be a function that fetches all available members from the message
        Map<String, Integer> memberIdMap = new HashMap<>();
        for (SpreadGroup member : members) {
            String memberName = member.toString();
            String actualMemberName = Spread.extractServerName(memberName);
            int memberId = Spread.extractServerId(memberName);
            memberIdMap.put(actualMemberName, memberId);
            System.out.println(" - " + actualMemberName + " (ID: " + memberId + ")");
        }

        // TODO: this should be another function that returns the id of the lowest
        // TODO: set new primary Stub/Skeleton
        // Determine the primary based on the lowest or highest ID
        // For example, using the lowest ID
        for (Map.Entry<String, Integer> entry : memberIdMap.entrySet()) {
            int memberId = entry.getValue();
            String memberName = entry.getKey();
            if (memberId < primaryId) {
                primaryId = memberId;
                rmiManager.setPrimaryServer(primaryId);
                // TODO: I do not see that the primaryName is used anywhere apart from the logging
                primaryName = memberName;
            }
        }

        System.out.println("Elected primary: " + primaryName + " (ID: " + primaryId + ")");
        if (this.serverId == primaryId) {
            this.isPrimary = true;
            System.out.println(this.serverName + " is now the primary server.");

            // As the new primary, update backup servers with the current lobbies
            replicatePrimaryState();

        } else {
            this.isPrimary = false;
            System.out.println(this.serverName + " is a backup server.");
        }
    }
}
