package alcatraz.server.rmi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RMI {
    public String ip;
    public int port;
    public static String remoteObjectName = "Alcatraz";

    public RMI(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static Map<Integer, RMI> getRMISettings(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(new File(filePath));
        } catch (IOException e) {
            System.out.println("File " + filePath + " with RMI settings not found!");
            System.exit(1);
        }

        Map<Integer, RMI> rmiServers = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            int id = Integer.parseInt(field.getKey());
            JsonNode node = field.getValue();
            String ip = node.get("ip").asText();
            int port = node.get("port").asInt();
            rmiServers.put(id, new RMI(ip, port));
        }

        return rmiServers;
    }
}
