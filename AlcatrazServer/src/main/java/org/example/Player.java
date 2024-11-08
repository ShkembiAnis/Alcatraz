//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example;

import java.io.Serializable;

public class Player implements Serializable {
    private ClientInterface client;
    private String clientName;
    private String ip_address;
    private String port;

    public Player(ClientInterface client, String clientName, String ip_address, String port) {
        this.client = client;
        this.clientName = clientName;
        this.ip_address = ip_address;
        this.port = port;
    }

    public String getClientName() {
        return this.clientName;
    }

    public String getIp_address() {
        return this.ip_address;
    }

    public String getPort() {
        return this.port;
    }
}
