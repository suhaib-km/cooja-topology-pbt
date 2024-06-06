package CoojaTopologyTester.strategies;

import java.util.Arrays;
import java.util.Random;
import CoojaTopologyTester.Seed;
import CoojaTopologyTester.MutationStrategy;

public class SingleNodeDisplacementStrategy extends MutationStrategy {
    @Override
    public Seed mutate(Seed seed) {
        double[][] newTopology = Arrays.copyOf(seed.getTopology(), seed.getTopology().length);
        Random rand = new Random();
        int index = rand.nextInt(newTopology.length);
        newTopology[index][0] += rand.nextDouble() - 0.5; // x mutation
        newTopology[index][1] += rand.nextDouble() - 0.5; // y mutation
        return new Seed(newTopology, seed.getEnergy());
    }
}