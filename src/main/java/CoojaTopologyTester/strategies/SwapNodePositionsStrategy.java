package CoojaTopologyTester.strategies;

import java.util.Arrays;
import java.util.Random;
import CoojaTopologyTester.Seed;
import CoojaTopologyTester.MutationStrategy;

public class SwapNodePositionsStrategy extends MutationStrategy {
    @Override
    public Seed mutate(Seed seed) {
        double[][] newTopology = Arrays.copyOf(seed.getTopology(), seed.getTopology().length);
        Random rand = new Random();
        int index1 = rand.nextInt(newTopology.length);
        int index2 = rand.nextInt(newTopology.length);
        double[] temp = newTopology[index1];
        newTopology[index1] = newTopology[index2];
        newTopology[index2] = temp;
        return new Seed(newTopology, seed.getEnergy());
    }
}