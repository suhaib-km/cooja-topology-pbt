package CoojaTopologyTester;

import java.util.List;
import java.util.Random;

public class Seed {
    Node[] topology;
    double energy;
    public static final int INITIAL_ENERGY = 100;
    public static final int MAX_SEEDS = 10;

    public Seed(Node[] topology, double energy) {
        this.topology = topology;
        this.energy = energy;
    }

    static void initializeSeeds(List<Seed> seeds, MutationStrategy strategy, List<Invariant> invariants) {
        for (int i = 0; i < MAX_SEEDS; i++) {
            Node[] nodes = new Node[10];
            for (int j = 0; j < nodes.length; j++)
            {
                nodes[j] = new Node(new double[3], "");
            }
            seeds.add(strategy.mutate(new Seed(nodes, INITIAL_ENERGY), invariants));
        }
    }

    static Seed selectSeed(List<Seed> seeds) {
        int totalEnergy = seeds.stream().mapToInt(seed -> (int) seed.energy).sum();
        int randomEnergy = new Random().nextInt(totalEnergy);
        int currentEnergy = 0;
        for (Seed seed : seeds) {
            currentEnergy += seed.energy;
            if (currentEnergy >= randomEnergy) {
                return seed;
            }
        }
        return seeds.get(seeds.size() - 1);
    }
   
    public static void normaliseEnergy(List<Seed> seeds) {
        double maxEnergy = seeds.stream()
                               .mapToDouble(Seed::getEnergy)
                               .max()
                               .orElse(1.0);

        seeds.forEach(seed -> seed.setEnergy(seed.getEnergy() / maxEnergy));
    }


    static void updateSeedEnergy(Seed seed, double dutyCycle) {
        double targetDutyCycle = 0.5;
        double energyAdjustment = 1.0 / (1 + Math.abs(dutyCycle - targetDutyCycle));
        seed.energy += energyAdjustment;
    }

    public Node[] getTopology()
    {
        return topology;
    }

    public double getEnergy()
    {
        return energy;
    }

    public void setEnergy(double newEnergy)
    {
        this.energy = newEnergy;
    }
}
