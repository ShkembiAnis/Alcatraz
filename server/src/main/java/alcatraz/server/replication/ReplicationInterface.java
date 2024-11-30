package alcatraz.server.replication;

import alcatraz.server.state.SharedState;
import alcatraz.shared.interfaces.ServerInterface;

public interface ReplicationInterface {
    boolean isPrimary();
    void replicatePrimaryState();
    ServerInterface getPrimaryServer();
    SharedState getSharedState();
}
