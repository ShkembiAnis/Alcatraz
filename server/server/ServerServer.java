package server;

public interface ServerServer {
    void electNewPrimary();
    void update();
    void forwardToPrimary();
    void joinServerGroup();
}
