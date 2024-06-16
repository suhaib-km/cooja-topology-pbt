package CoojaTopologyTester;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class CoojaTopologyTester {

    private static final int PORT = 8888;
    private static final String CONFIG_FILE = "config.properties";

    public static void main(String[] args) {
        ConfigLoader.loadConfig(CONFIG_FILE);
        if (ConfigLoader.cscFilePath == null || ConfigLoader.moteTypes == null) {
            System.err.println("CSC file path or mote types not specified in the configuration file.");
            return;
        }
        Seed seed = initializeSeed(); // Initialize seed with appropriate topology and energy
        double[] dutyCycles = testRadioDutyCycles(seed);
        double dutyCycle = Arrays.stream(dutyCycles).max().orElse(-1);
        System.out.println("Duty Cycle: " + dutyCycle);
    }

    public static Seed initializeSeed() {
        // Replace with actual initialization logic based on your requirements
        Node[] topology = new Node[10];
        Random rand = new Random();
        for (int i = 0; i < topology.length; i++) {
            double[] position = {rand.nextDouble() * 10, rand.nextDouble() * 10, 0}; // Example position initialization
            String type = ConfigLoader.moteTypes.get(rand.nextInt(ConfigLoader.moteTypes.size()));
            topology[i] = new Node(position, type);
        }
        return new Seed(topology, Seed.INITIAL_ENERGY);
    }

    public static double[] testRadioDutyCycles(Seed seed) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Process cooja = startCoojaSimulation();
        if (cooja == null) {
            System.out.println("Cooja could not start");
            return new double[]{-1};
        }

        Socket socket = waitForSocketConnection();
        System.out.println("Connected to socket");

        if (socket != null) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String response;

                for (String moteType : ConfigLoader.moteTypes) {
                    List<Node> nodesOfType = Arrays.stream(seed.getTopology())
                                                .filter(node -> node.getType().equals(moteType))
                                                .collect(Collectors.toList());

                    if (!nodesOfType.isEmpty()) {
                        StringBuilder addMoteCommand = new StringBuilder("ADD_MOTE%%");
                        addMoteCommand.append(moteType.trim()).append(",");
                        addMoteCommand.append(nodesOfType.size()).append(",");

                        for (Node node : nodesOfType) {
                            double[] position = node.getPosition();
                            for (double coord : position) {
                                addMoteCommand.append(coord).append(",");
                            }
                        }
                        addMoteCommand.deleteCharAt(addMoteCommand.length() - 1);

                        System.out.println("Adding Motes: " + addMoteCommand.toString());
                        writer.println(addMoteCommand.toString());
                        response = reader.readLine();
                        System.out.println("Adding motes response: " + response);
                    }
                }

                System.out.println("Running Simulation");
                writer.println("START_SIMULATION");
                response = reader.readLine();
                System.out.println("Starting sim: " + response);

                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Stopping Simulation");
                writer.println("STOP_SIMULATION");
                response = reader.readLine();
                System.out.println("Stopping step: " + response);

                System.out.println("Getting Power statistics");
                writer.println("GET_POWER");
                response = reader.readLine();
                System.out.println("Power Statistics: " + response);
                
                return analyzePowerStatistics(response);

            } catch (IOException e) {
                System.err.println("Error: Failed to perform radio duty cycle test.");
                e.printStackTrace();
                System.out.println("Cooja finished");
                cooja.destroy();
                return new double[]{-1};
            } finally {
                try {
                    socket.close();
                    System.out.println("Cooja finished");
                    cooja.destroy();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Failed to return result");
        return new double[]{-1};
    }

    private static Socket waitForSocketConnection() {
        while (true) {
            try {
                return new Socket("localhost", PORT);
            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static Process startCoojaSimulation() {
        try {
            File file = new File("/home/suhaib/uni/fyp/contiki-ng/tools/cooja/");
            ProcessBuilder builder = new ProcessBuilder("/home/suhaib/uni/fyp/contiki-ng/tools/cooja/gradlew", "run").directory(file).redirectErrorStream(true);
            Process process = builder.start();

            System.out.println("Cooja started");
            return process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double[] analyzePowerStatistics(String response) {
        if (response.isEmpty()){
            return null;
        }
        String[] stats = response.split(";");
        if (stats.length == 0) return null;
        double[] result = new double[stats.length];
        for (int i = 0; i < stats.length; i++) {
            result[i] = Double.parseDouble(stats[i]);
        }
        return result;
    }
}
