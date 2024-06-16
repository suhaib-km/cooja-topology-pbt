package CoojaTopologyTester.strategies;

import CoojaTopologyTester.Seed;
import CoojaTopologyTester.Node;
import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.MutationStrategy;
import CoojaTopologyTester.ConfigLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealingStrategy extends MutationStrategy {
    private final double initialTemperature;
    private final double coolingRate;
    private final Random rand = new Random();

    public SimulatedAnnealingStrategy(double initialTemperature, double coolingRate) {
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
    }

    @Override
    public Seed mutate(Seed seed, List<Invariant> invariants) {
        double temperature = initialTemperature;
        Seed currentSeed = seed;
        Seed bestSeed = seed;

        while (temperature > 1) {
            Seed newSeed = getNeighbour(currentSeed, temperature);

            if (checkInvariants(newSeed.getTopology(), invariants)) {
                double currentEnergy = currentSeed.getEnergy();
                double newEnergy = newSeed.getEnergy();

                if (acceptanceProbability(currentEnergy, newEnergy, temperature) > rand.nextDouble()) {
                    currentSeed = newSeed;
                }

                if (newEnergy < bestSeed.getEnergy()) {
                    bestSeed = newSeed;
                }
            }

            temperature *= 1 - coolingRate;
        }

        return bestSeed;
    }

    private Seed getNeighbour(Seed seed, double temperature) {
        Node[] nodes = seed.getTopology();
        List<int[]> edges = new ArrayList<>();  // Assuming you have a way to get edges from the seed
        int newSize = tinteger(nodes.length, temperature);

        int[] ops = getOpCount(newSize, nodes.length, temperature);
        int adds = ops[0];
        int dels = ops[1];

        Node[] newNodes = addNodes(nodes, adds, temperature);
        Node[] finalNodes = delNodes(newNodes, dels);

        int newEdgeSize = tinteger(edges.size(), temperature);
        int[] edgeOps = getOpCount(newEdgeSize, edges.size(), temperature);
        int edgeAdds = edgeOps[0];
        int edgeDels = edgeOps[1];

        List<int[]> newEdges = addEdges(finalNodes, edges, edgeAdds);
        List<int[]> finalEdges = delEdges(newEdges, edgeDels);

        return new Seed(finalNodes, seed.getEnergy())); // Adjust energy calculation as needed
    }

    private int tinteger(int base, double temperature) {
        int offset = (int) (0.5 * base * temperature) + 1;
        return rand.nextInt((base + offset) - (base - offset) + 1) + (base - offset);
    }

    private int[] getOpCount(int newSize, int oldSize, double temperature) {
        int adds = Math.max(0, tinteger(newSize - oldSize, temperature));
        int dels = Math.max(0, tinteger(oldSize - newSize, temperature));
        return new int[]{adds, dels};
    }

    private Node[] addNodes(Node[] nodes, int count, double temperature) {
        List<Node> newNodes = new ArrayList<>(List.of(nodes));
        for (int i = 0; i < count; i++) {
            double[] position = {rand.nextDouble(), rand.nextDouble(), rand.nextDouble()};
            newNodes.add(new Node(position, getRandomNodeType(ConfigLoader.moteTypes)));
        }
        return newNodes.toArray(new Node[0]);
    }

    private Node[] delNodes(Node[] nodes, int count) {
        List<Node> newNodes = new ArrayList<>(List.of(nodes));
        for (int i = 0; i < count && !newNodes.isEmpty(); i++) {
            newNodes.remove(rand.nextInt(newNodes.size()));
        }
        return newNodes.toArray(new Node[0]);
    }

    private List<int[]> addEdges(Node[] nodes, List<int[]> edges, int count) {
        List<int[]> newEdges = new ArrayList<>(edges);
        for (int i = 0; i < count; i++) {
            int n1 = rand.nextInt(nodes.length);
            int n2 = rand.nextInt(nodes.length);
            if (n1 != n2) {
                newEdges.add(new int[]{n1, n2});
            }
        }
        return newEdges;
    }

    private List<int[]> delEdges(List<int[]> edges, int count) {
        List<int[]> newEdges = new ArrayList<>(edges);
        for (int i = 0; i < count && !newEdges.isEmpty(); i++) {
            newEdges.remove(rand.nextInt(newEdges.size()));
        }
        return newEdges;
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
}
