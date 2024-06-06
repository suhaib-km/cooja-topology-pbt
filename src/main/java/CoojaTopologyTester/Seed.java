package CoojaTopologyTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Seed {
    double[][] topology;
    double energy;
    public static final int INITIAL_ENERGY = 100;
    public static final int MAX_SEEDS = 10;

    public Seed(double[][] topology, double energy) {
        this.topology = topology;
        this.energy = energy;
    }

    static void initializeSeeds(List<Seed> seeds, MutationStrategy strategy) {
        for (int i = 0; i < MAX_SEEDS; i++) {
            seeds.add(strategy.mutate(new Seed(new double[5][3], INITIAL_ENERGY)));
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

    static void updateSeedEnergy(Seed seed, double dutyCycle) {
        double targetDutyCycle = 0.5;
        double energyAdjustment = 1.0 / (1 + Math.abs(dutyCycle - targetDutyCycle));
        seed.energy += energyAdjustment;
    }

    public double[][] getTopology()
    {
        return topology;
    }

    public double getEnergy()
    {
        return energy;
    }
}
