package CoojaTopologyTester;

import java.io.*;
import java.net.*;

public class CoojaTopologyTester {

    private static final int PORT = 8888; // Choose an appropriate port number

    // Main method to start the simulation and execute property-based tests
    public static void main(String[] args) {
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

                // Add a mote of type "root/sender" at position (1.0, 1.0, 0.0)
                String moteType = "root/sender";
                int amountToAdd = 2;
                double[][] positions = { {1.0, 1.0, 0.0}, {2.0, 2.0, 0.0} };
                StringBuilder addMoteCommand = new StringBuilder("ADD_MOTE ").append(moteType).append(",");
                addMoteCommand.append(amountToAdd).append(",");
                for (double[] position : positions) {
                    for (double coord : position) {
                        addMoteCommand.append(coord).append(",");
                    }
                }
                addMoteCommand.deleteCharAt(addMoteCommand.length() - 1); // Remove the last comma
                writer.println(addMoteCommand.toString());
                String response = reader.readLine();
                System.out.println(response);

                // Start the simulation
                writer.println("START_SIMULATION");
                response = reader.readLine();
                System.out.println(response);

                // Stop the simulation
                writer.println("STOP_SIMULATION");
                response = reader.readLine();
                System.out.println(response);

                // Step the simulation
                for (int i = 0; i < 1000; i++) {
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

        // Wait for some time and then stop Cooja simulation
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

    // Method to perform property-based tests
    private void testSomeProperty() {
        // Implement property-based tests here
        // For example:
        System.out.println("Running property-based tests...");
    }
}
