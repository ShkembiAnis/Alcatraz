package server.server_impl;

public interface ServerServer {
    void electNewPrimary();
    void update();
    void forwardToPrimary();
    void joinServerGroup();
}
