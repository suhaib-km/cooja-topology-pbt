package CoojaTopologyTester.strategies;

import CoojaTopologyTester.Seed;
import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.MutationStrategy;
import CoojaTopologyTester.ConfigLoader;
import CoojaTopologyTester.Node;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

public class SingleNodeDisplacementStrategy extends MutationStrategy {

    @Override
    public Seed mutate(Seed seed, List<Invariant> invariants) {
        Random rand = new Random();
        Node[] newTopology;

        do {
            newTopology = Arrays.copyOf(seed.getTopology(), seed.getTopology().length);
            if (seed.getTopology().length <= 0)
            {
                return (new RandomStrategy()).mutate(seed, invariants);
            }
            int index = rand.nextInt(newTopology.length);
            double[] currentPosition = newTopology[index].getPosition();
            double[] newPosition = Arrays.copyOf(currentPosition, currentPosition.length);

            double displacementAmount = rand.nextDouble();
            for (int i = 0; i < newPosition.length; i++) {
                newPosition[i] += displacementAmount;
            }

            newTopology[index] = new Node(newPosition, newTopology[index].getType());
        } while (!checkInvariants(newTopology, invariants));

        return new Seed(newTopology, seed.getEnergy());
    }

    private boolean checkInvariants(Node[] topology, List<Invariant> invariants) {
        for (Invariant invariant : invariants) {
            if (!invariant.check(topology, ConfigLoader.moteTypes)) {
                return false;
            }
        }
        return true;
    }
}
