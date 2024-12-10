package alcatraz.server.replication;

import alcatraz.server.state.LobbyManager;
import alcatraz.shared.rmi.RMIManager;
import alcatraz.server.spread.Spread;
import alcatraz.server.state.SharedState;
import alcatraz.shared.interfaces.ServerInterface;
import alcatraz.shared.utils.Player;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Replication implements ReplicationInterface, AdvancedMessageListener {

    // TODO: name is not relevant I think, ID would be enough, should they not be final?
    public int serverId;
    private boolean isPrimary;

    private final SharedState sharedState;
    private final RMIManager rmiManager;
    private final Spread spread;

    public Replication(SharedState sharedState, RMIManager rmiManager, Spread spread, int serverId) {
        this.serverId = serverId;
        this.isPrimary = false;

        this.sharedState = sharedState;
        this.rmiManager = rmiManager;
        this.spread = spread;
    }


    private void setSharedState(LobbyManager lobbyManager, HashMap<String, Player> players) {
        this.sharedState.lobbyManager = lobbyManager;
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
        SpreadMessage msg = new SpreadMessage();
        msg.setReliable();
        msg.addGroup(spread.groupName);
        msg.setSelfDiscard(true);

        try {
            msg.setObject(this.sharedState);
        } catch (SpreadException e) {
            System.out.println("Setting a content of the message failed!");
            e.printStackTrace();
        }

        try {
            spread.connection.multicast(msg);
            // TODO: we use blocking algorithm, so we have to wait here until we get all the confirmations
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage message) {

        Object receivedObject = null;
        try {
            receivedObject = message.getObject();
        } catch (SpreadException e) {
            e.printStackTrace();
        }

        if (!(receivedObject instanceof SharedState sharedState)) {
            System.out.println("Incorrect type received by replica!");
            return;
        }

        this.setSharedState(sharedState.lobbyManager, sharedState.players);
        System.out.println(this.serverId + " updated state from primary server.");
    }

    @Override
    public void membershipMessageReceived(SpreadMessage message) {
        System.out.println("Membership change detected.");
        System.out.println("Current server ID: " + this.serverId);

        MembershipInfo info = message.getMembershipInfo();
        List<Integer> availableServerIds = getAvailableServers(info);

        if (info.isCausedByJoin()) {
            System.out.println("Server joined the group.");
            if (this.isPrimary) {
                replicatePrimaryState();
            }
            electNewPrimary(availableServerIds);
        }

        if (info.isCausedByLeave() || info.isCausedByDisconnect() || info.isCausedByNetwork()) {
            electNewPrimary(availableServerIds);
        }
    }

    public void joinServerGroup(int serverId) {
        String privateGroupName = "Server_" + serverId;

        InetAddress spreadDaemonAddress = null;
        try {
            spreadDaemonAddress = InetAddress.getByName(spread.host);
        } catch (UnknownHostException e) {
            System.out.println("Address of the server daemon not known!");
            e.printStackTrace();
            return;
        }

        try {
            spread.connection.connect(spreadDaemonAddress, spread.port, privateGroupName, false, true);
        } catch (SpreadException e) {
            System.out.printf("Connection to the spread daemon %s:%s failed!", spread.host, spread.port);
            e.printStackTrace();
            return;
        }

        spread.connection.add(this);
        SpreadGroup group = new SpreadGroup();
        try {
            group.join(spread.connection, spread.groupName);
        } catch (SpreadException e) {
            System.out.printf("Joining the spread group %s failed!", spread.groupName);
            return;
        }

        System.out.println(this.serverId + " joined the group.");
    }

    private List<Integer> getAvailableServers(MembershipInfo info) {
        SpreadGroup[] members = info.getMembers();

        System.out.println("Members in group:");
        List<Integer> availableServerIds = new ArrayList<>();
        for (SpreadGroup member : members) {
            String memberName = member.toString();
            int memberId = Spread.extractServerId(memberName);
            availableServerIds.add(memberId);
            System.out.println("- (ID: " + memberId + ")");
        }

        return availableServerIds;
    }

    private void electNewPrimary(List<Integer> availablePlayers) {
        if (availablePlayers.isEmpty()) {
            return;
        }

        // Determine the primary based on the lowest ID
        int lowestID = availablePlayers.stream().
                min(Comparator.naturalOrder()).
                orElseThrow(() -> new IllegalStateException("No available players found"));

        rmiManager.setPrimaryServer(lowestID);

        System.out.println("Elected primary: " + lowestID);
        if (this.serverId == lowestID) {
            this.isPrimary = true;
            System.out.println(this.serverId + " is now the primary server.");
        } else {
            this.isPrimary = false;
            System.out.println(this.serverId + " is a backup server.");
        }
    }
}
