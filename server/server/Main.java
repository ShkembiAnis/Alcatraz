package server;

import java.net.Inet4Address;
import java.net.InetAddress;
import spread.*;

/**
 * Server
 */
public class Main {

  public static void main(String[] args) {

    String spreadIPStr = System.getenv("SPREAD_IP");
    System.out.println(spreadIPStr);

    InetAddress spreadIP = null;
    try {
      spreadIP = InetAddress.getByName(spreadIPStr);

    } catch (Exception e) {
      System.out.println("IP not found");
      System.exit(1);
    }

    if (spreadIP == null) {
      System.exit(1);
    }

    SpreadConnection connection = new SpreadConnection();
    try {
      // connection.connect(inetAddress, 4803, "albert", false, false);
      connection.connect(spreadIP, 4803, spreadIPStr, true, true);
    } catch (Exception e) {
      System.out.println("Connection Error");
      System.exit(1);
    }

    SpreadGroup group = new SpreadGroup();
    try {
      group.join(connection, "alcatraz");
    } catch (Exception e) {
      System.exit(1);
    }

    SpreadMessage message = new SpreadMessage();
    message.setData(spreadIPStr.getBytes());
    message.addGroup("alcatraz");
    message.setReliable();

    try {
      connection.multicast(message);
      System.out.println("Message out!");
    } catch (Exception e) {
      System.exit(0);
    }

    SpreadMessage msg = null;
    try {
      while (true) {
        System.out.println("waiting...");
        msg = connection.receive();
        System.out
            .println(
                spreadIPStr + " got a new msg from " + msg.getSender() + " ,content: " + new String(msg.getData()));
        System.out.println("Is membership: " + msg.isMembership());
      }
    } catch (Exception e) {
      System.exit(1);
    }

    // if (msg.isRegular())
    // System.out
    // .println(spreadIPStr + " got a new msg from " + msg.getSender() + " ,content:
    // " + new String(msg.getData()));
    // else
    // System.out.println("New membership msg from " +
    // msg.getMembershipInfo().getGroup());

  }
}
