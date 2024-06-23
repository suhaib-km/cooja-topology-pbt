package CoojaTopologyTester.strategies;

import CoojaTopologyTester.Seed;
import CoojaTopologyTester.Node;
import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.MutationStrategy;
import CoojaTopologyTester.ConfigLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealingStrategy extends MutationStrategy {
    private static final int MAX_NODES = 20;
    private final double coolingRate;
    private double currentTemperature;
    private Seed currentSeed;
    private Seed bestSeed;
    private double bestEnergy;
    private final Random rand = new Random();

    public SimulatedAnnealingStrategy(double initialTemperature, double coolingRate) {
        this.coolingRate = coolingRate;
        this.currentTemperature = initialTemperature;
    }

    @Override
    public Seed mutate(Seed seed, List<Invariant> invariants) {
        if (currentSeed == null) {
            currentSeed = seed;
            bestSeed = seed;
            bestEnergy = seed.getEnergy();
        }

        Seed newSeed = getNeighbour(currentSeed, currentTemperature);

        if (checkInvariants(newSeed.getTopology(), invariants)) {
            double newEnergy = newSeed.getEnergy();

            if (acceptanceProbability(currentSeed.getEnergy(), newEnergy, currentTemperature) > rand.nextDouble()) {
                currentSeed = newSeed;
            }

            if (newEnergy < bestEnergy) {
                bestSeed = newSeed;
                bestEnergy = newEnergy;
            }
        }

        currentTemperature *= 1 - coolingRate;

        return currentSeed;
    }

    private Seed getNeighbour(Seed seed, double temperature) {
        Node[] nodes = seed.getTopology();
        int newSize = Math.min(MAX_NODES, tinteger(nodes.length, temperature));

        int[] ops = getOpCount(newSize, nodes.length, temperature);
        int adds = Math.min(ops[0], MAX_NODES - nodes.length); 
        int dels = Math.max(ops[1], 0); 

        Node[] newNodes = addNodes(nodes, adds);
        Node[] finalNodes = delNodes(newNodes, dels);

        return new Seed(finalNodes, seed.getEnergy());
    }

    private int tinteger(int base, double temperature) {
        int offset = (int) (0.5 * Math.max(base, 1) * temperature) + 1;
        int min = Math.max(1, base - offset);
        int max = Math.max(min + 1, base + offset); 
        return rand.nextInt(max - min) + min; 
    }

    private int[] getOpCount(int newSize, int oldSize, double temperature) {
        int adds = Math.max(0, tinteger(newSize - oldSize, temperature));
        int dels = Math.max(0, tinteger(oldSize - newSize, temperature));
        return new int[]{adds, dels};
    }

    private Node[] addNodes(Node[] nodes, int count) {
        List<Node> newNodes = new ArrayList<>(Arrays.asList(nodes));
        for (int i = 0; i < count; i++) {
            double[] position = {rand.nextDouble(), rand.nextDouble(), rand.nextDouble()};
            newNodes.add(new Node(position, getRandomNodeType(ConfigLoader.moteTypes)));
        }
        return newNodes.toArray(new Node[0]);
    }

    private Node[] delNodes(Node[] nodes, int count) {
        List<Node> newNodes = new ArrayList<>(Arrays.asList(nodes));
        for (int i = 0; i < count && newNodes.size() > 1; i++) { 
            newNodes.remove(rand.nextInt(newNodes.size())); 
        }
        return newNodes.toArray(new Node[0]);
    }

    private double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }

    private boolean checkInvariants(Node[] topology, List<Invariant> invariants) {
        for (Invariant invariant : invariants) {
            if (!invariant.check(topology, ConfigLoader.moteTypes)) {
                return false;
            }
        }
        return true;
    }

    private String getRandomNodeType(List<String> nodeTypes) {
        return nodeTypes.get(rand.nextInt(nodeTypes.size()));
    }

    public Seed getBestSeed() {
        return bestSeed;
    }
}
