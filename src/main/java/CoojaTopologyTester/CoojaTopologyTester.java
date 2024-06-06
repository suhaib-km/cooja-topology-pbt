package CoojaTopologyTester;

import net.jqwik.api.*;
import java.io.*;
import java.net.*;
import java.util.*;

import CoojaTopologyTester.strategies.*;

public class CoojaTopologyTester {

    private static final int PORT = 8888;
    private static final List<Seed> seeds = new ArrayList<>();
    private MutationStrategy mutationStrategy;

    static {
        Seed.initializeSeeds(seeds, new RandomStrategy());
    }

    @Property(tries = 1000)
    public void testSomeProperty(@ForAll("generateSeeds") Seed seed) {
        System.out.println("Running property-based tests...");

        if (new Random().nextBoolean()) {
            mutationStrategy = new RandomStrategy(); // Can randomly select among the five RandomGeneration classes
        } else {
            mutationStrategy = new CompleteDisplacementStrategy(); // Can randomly select among the five ExistingTopologyMutation classes
        }

        seed = mutationStrategy.mutate(seed);
        testRadioDutyCycles(seed);
    }

    @Provide
    Arbitrary<Seed> generateSeeds() {
        return Arbitraries.of(seeds);
    }

    public void testRadioDutyCycles(Seed seed) {
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

                double dutyCycle = analyzePowerStatistics(response);
                Seed.updateSeedEnergy(seed, dutyCycle);

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

    private static double analyzePowerStatistics(String response) {
        return Double.parseDouble(response.split(",")[0]);
    }
}
