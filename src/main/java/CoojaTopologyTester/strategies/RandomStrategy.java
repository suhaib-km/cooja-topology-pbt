package CoojaTopologyTester.strategies;

import java.util.Random;
import CoojaTopologyTester.Seed;
import CoojaTopologyTester.MutationStrategy;

public class RandomStrategy extends MutationStrategy {
    private static final Random random = new Random();

    @Override
    public Seed mutate(Seed seed) {
        return new Seed(generateRandomPositions(seed.getTopology().length), Seed.INITIAL_ENERGY);
    }

    private double[][] generateRandomPositions(int nodeCount) {
        double[][] positions = new double[nodeCount][3];
        for (int i = 0; i < nodeCount; i++) {
            positions[i][0] = random.nextDouble() * 100;
            positions[i][1] = random.nextDouble() * 100;
            positions[i][2] = 0.0;
        }
        return positions;
    }
}