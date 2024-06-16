package CoojaTopologyTester;

import java.io.*;
import java.util.*;

import CoojaTopologyTester.invariants.*;

public class ConfigLoader {
    public static String cscFilePath;
    public static List<String> moteTypes;
    public static List<Invariant> invariants = new ArrayList<>();

    public static void loadConfig(String configFile) {
        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);

            cscFilePath = prop.getProperty("cscFilePath");
            moteTypes = Arrays.asList(prop.getProperty("moteTypes").split(","));
            
            String propertyStr = prop.getProperty("properties");
            if (propertyStr != null) {
                for (String propDef : propertyStr.split(";")) {
                    invariants.add(parseInvariant(propDef));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Invariant parseInvariant(String propDef) {
        String[] parts = propDef.split(":");
        String type = parts[0];
        switch (type) {
            case "NodeCount":
                return new NodeCountInvariant(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            case "NodeRatio":
                return new NodeRatioInvariant(parts[1], parts[2], Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
            case "MaxCount":
                return new MaxCountInvariant(parts[1], Integer.parseInt(parts[2]));
            default:
                throw new IllegalArgumentException("Unknown property type: " + type);
        }
    }
}
