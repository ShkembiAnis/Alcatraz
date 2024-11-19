package alcatraz.server.spread;

public class Spread {
    public String host;
    public int port;
    public spread.SpreadConnection connection;
    public String groupName;

    public Spread(String host, int port, String groupName) {
        this.host = host;
        this.port = port;
        this.connection = new spread.SpreadConnection();
        this.groupName = groupName;
    }

    public static int extractServerId(String memberName) {
        // TODO: Refactor nested ifs
        // The memberName is in the format "#ServerName_ServerId#Host"
        if (memberName.startsWith("#")) {
            int secondHashIndex = memberName.indexOf('#', 1);
            if (secondHashIndex > 1) {
                String privateGroupName = memberName.substring(1, secondHashIndex);
                String[] parts = privateGroupName.split("_");
                if (parts.length == 2) {
                    try {
                        return Integer.parseInt(parts[1]); // Return the server ID
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // If unable to extract ID, return a high value
        return Integer.MAX_VALUE;
    }

    public static String extractServerName(String memberName) {
        // TODO: Refactor nested ifs
        // The memberName is in the format "#ServerName_ServerId#Host"
        if (memberName.startsWith("#")) {
            int secondHashIndex = memberName.indexOf('#', 1);
            if (secondHashIndex > 1) {
                String privateGroupName = memberName.substring(1, secondHashIndex);
                String[] parts = privateGroupName.split("_");
                if (parts.length == 2) {
                    return parts[0]; // Return the server name
                }
            }
        }
        // If the format is unexpected, return the original memberName
        return memberName;
    }
}
