package CoojaTopologyTester.strategies;

import CoojaTopologyTester.MutationStrategy;
import CoojaTopologyTester.Seed;
import java.util.Random;

public class CompleteDisplacementStrategy extends MutationStrategy {
    private static final Random random = new Random();

    @Override
    public Seed mutate(Seed seed) {
        return mutateTopology(seed);
    }

    private Seed mutateTopology(Seed seed) {
        double[][] newTopology = new double[seed.getTopology().length][3];
        for (int i = 0; i < seed.getTopology().length; i++) {
            newTopology[i][0] = (seed.getTopology())[i][0] + (random.nextDouble() - 0.5) * 10;
            newTopology[i][1] = (seed.getTopology())[i][1] + (random.nextDouble() - 0.5) * 10;
            newTopology[i][2] = (seed.getTopology())[i][2];
        }
        return new Seed(newTopology, seed.getEnergy());
    }
}
