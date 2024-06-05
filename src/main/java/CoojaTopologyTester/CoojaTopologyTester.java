package CoojaTopologyTester;

import net.jqwik.api.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class CoojaTopologyTester {

    private static final int PORT = 8888; // Choose an appropriate port number

    public static void main(String[] args) {
        // Perform property-based tests
        testSomeProperty();
    }

    @Property
    public static void testSomeProperty() {
        System.out.println("Running property-based tests...");
        Arbitraries.integers().between(2, 10).sampleStream().limit(5).forEach(nodeCount -> {
            System.out.println("Testing with " + nodeCount + " nodes.");
            testRadioDutyCycles(nodeCount, new ArrayList<>());
        });
    }

    public static void testRadioDutyCycles(int nodeCount, List<double[][]> previousTopologies) {
        Process cooja = startCoojaSimulation();
        if (cooja == null) {
            System.out.println("Cooja could not start");
            return;
        }

        Socket socket = waitForSocketConnection();
        if (socket != null) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                double[][] positions;
                if (previousTopologies.isEmpty()) {
                    positions = generateRandomPositions(nodeCount);
                } else {
                    positions = generateSmartPositions(nodeCount, previousTopologies);
                }

                StringBuilder addMoteCommand = new StringBuilder("ADD_MOTE root/sender,");
                addMoteCommand.append(nodeCount).append(",");
                for (double[] position : positions) {
                    for (double coord : position) {
                        addMoteCommand.append(coord).append(",");
                    }
                }
                addMoteCommand.deleteCharAt(addMoteCommand.length() - 1);
                writer.println(addMoteCommand.toString());
                String response = reader.readLine();
                System.out.println(response);

                for (int i = 0; i < 10000; i++) {
                    writer.println("STEP_SIMULATION");
                    response = reader.readLine();
                }

                writer.println("GET_POWER");
                response = reader.readLine();
                System.out.println("Power Statistics: " + response);

                double[] powerStatistics = parsePowerStatistics(response);
                previousTopologies.add(positions);

                if (shouldRunAnotherTest(powerStatistics)) {
                    testRadioDutyCycles(nodeCount, previousTopologies);
                }

            } catch (IOException e) {
                System.err.println("Error: Failed to perform radio duty cycle test.");
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Cooja finished");
        cooja.destroy();
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

    private static Process startCoojaSimulation() {
        try {
            File file = new File("/home/suhaib/uni/fyp/contiki-ng/tools/cooja/");
            ProcessBuilder builder = new ProcessBuilder("/home/suhaib/uni/fyp/contiki-ng/tools/cooja/./gradlew", "run").directory(file).redirectErrorStream(true);
            Process process = builder.start();

            System.out.println("Cooja started");
            return process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double[][] generateRandomPositions(int nodeCount) {
        double[][] positions = new double[nodeCount][3];
        for (int i = 0; i < nodeCount; i++) {
            positions[i][0] = Math.random() * 100;
            positions[i][1] = Math.random() * 100;
            positions[i][2] = 0.0;
        }
        return positions;
    }

    private static double[][] generateSmartPositions(int nodeCount, List<double[][]> previousTopologies) {
        double[][] lastTopology = previousTopologies.get(previousTopologies.size() - 1);
        double[][] newTopology = new double[nodeCount][3];
        for (int i = 0; i < nodeCount; i++) {
            newTopology[i][0] = lastTopology[i][0] + (Math.random() - 0.5) * 10;
            newTopology[i][1] = lastTopology[i][1] + (Math.random() - 0.5) * 10;
            newTopology[i][2] = lastTopology[i][2];
        }
        return newTopology;
    }

    private static double[] parsePowerStatistics(String response) {
        String[] parts = response.split(",");
        double[] stats = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            stats[i] = Double.parseDouble(parts[i]);
        }
        return stats;
    }

    private static boolean shouldRunAnotherTest(double[] powerStatistics) {
        double averagePower = Arrays.stream(powerStatistics).average().orElse(0.0);
        return averagePower > 50.0;
    }
}
