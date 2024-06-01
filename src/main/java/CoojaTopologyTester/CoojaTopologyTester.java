package CoojaTopologyTester;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import java.io.*;
import java.net.*;
import java.util.List;

public class CoojaTopologyTester {

    private static final int PORT = 8888; // Choose an appropriate port number

    // Main method to start the simulation and execute property-based tests
    public static void main(String[] args) {
        // Run property-based tests
        runPropertyBasedTests();
    }

    @Property(tries = 100) // run 100 different topologies
    void testRandomTopologies(@ForAll("topologies") List<Position> positions) {
        // Start the Contiki-NG simulation in a separate thread
        Process cooja = startCoojaSimulation();
        if (cooja == null) {
            System.out.println("Cooja could not start");
            return;
        }

        // Wait for the socket connection
        Socket socket = waitForSocketConnection();

        // Communicate with the simulation control plugin
        if (socket != null) {
            try {
                // Send commands or data to the simulation control plugin
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);

                // Receive responses from the simulation control plugin
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                // Add motes to simulation at generated positions
                String moteType = "root/sender";
                int amountToAdd = positions.size();
                StringBuilder addMoteCommand = new StringBuilder("ADD_MOTE ").append(moteType).append(",");
                addMoteCommand.append(amountToAdd).append(",");
                for (Position pos : positions) {
                    addMoteCommand.append(pos.x).append(",").append(pos.y).append(",").append(pos.z).append(",");
                }
                addMoteCommand.deleteCharAt(addMoteCommand.length() - 1); // Remove the last comma
                writer.println(addMoteCommand.toString());
                String response = reader.readLine();
                System.out.println(response);

                // Step the simulation
                int steps = 10000;
                for (int i = 0; i < steps; i++) {
                    writer.println("STEP_SIMULATION");
                    response = reader.readLine();
                }

                // Request power statistics
                writer.println("GET_POWER");
                response = reader.readLine();
                System.out.println("Power Statistics: " + response);

            } catch (IOException e) {
                System.err.println("Error: Failed to communicate with Simulation Control Plugin.");
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Terminate Cooja simulation after ensuring data collection
        cooja.destroy();
        System.out.println("Cooja finished");
    }

    private static Socket waitForSocketConnection() {
        while (true) {
            try {
                return new Socket("localhost", PORT);
            } catch (IOException e) {
                // Wait for a short period before trying again
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Method to start the Cooja simulation
    private static Process startCoojaSimulation() {
        try {
            // Cooja process
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

    @Provide
    Arbitrary<List<Position>> topologies() {
        return Arbitraries.integers().between(1, 10).flatMap(size -> {
            Arbitrary<Double> coordinates = Arbitraries.doubles().between(0.0, 100.0);
            return coordinates.array(Position.class, size).list().ofMaxSize(1);
        });
    }

    // Helper method to run property-based tests
    private static void runPropertyBasedTests() {
        new CoojaTopologyTester().testSomeProperty();
    }

    // Method to perform property-based tests
    private void testSomeProperty() {
        // Implement property-based tests here
        // For example:
        System.out.println("Running property-based tests...");
    }

    // Position class to store mote coordinates
    static class Position {
        double x, y, z;

        Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
