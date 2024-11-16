package alcatraz.server;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;

public interface ReplicationInterface extends AdvancedMessageListener {
    void joinServerGroup();
    void electNewPrimary(MembershipInfo info);
    void update(Object state);
    void forwardToPrimary(Object request);
}
