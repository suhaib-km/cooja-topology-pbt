package CoojaTopologyTester;

import java.io.*;
import java.net.*;

public class CoojaTopologyTester {

    private static final int PORT = 8888;

    public static void main(String[] args) {
    }

    public static double testRadioDutyCycles(Seed seed) {
        Process cooja = startCoojaSimulation();
        if (cooja == null) {
            System.out.println("Cooja could not start");
            return -1; // or some other error value
        }

        Socket socket = waitForSocketConnection();
        if (socket != null) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                double[][] positions = seed.topology;

                StringBuilder addMoteCommand = new StringBuilder("ADD_MOTE root/sender,");
                addMoteCommand.append(positions.length).append(",");
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

                return analyzePowerStatistics(response);

            } catch (IOException e) {
                System.err.println("Error: Failed to perform radio duty cycle test.");
                e.printStackTrace();
                return -1; // or some other error value
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

        return -1; // or some other error value
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

    private static double analyzePowerStatistics(String response) {
        return Double.parseDouble(response.split(",")[0]);
    }
}
