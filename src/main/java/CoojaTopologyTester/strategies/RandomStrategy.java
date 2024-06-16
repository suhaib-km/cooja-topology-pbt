package CoojaTopologyTester.strategies;

import CoojaTopologyTester.Seed;
import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.MutationStrategy;
import CoojaTopologyTester.ConfigLoader;
import CoojaTopologyTester.Node;
import java.util.List;
import java.util.Random;

public class RandomStrategy extends MutationStrategy {

    @Override
    public Seed mutate(Seed seed, List<Invariant> invariants) {
        Random random = new Random();
        Node[] newTopology;

        do {
            int numNodes = random.nextInt(20);
            newTopology = new Node[numNodes];
            for (int i = 0; i < numNodes; i++) {
                double[] position = {
                    random.nextDouble() * 10,
                    random.nextDouble() * 10,
                    0.0
                };
                if (ConfigLoader.moteTypes.size() <= 0)
                {
                    System.out.println("No Mote types in Config Loader");
                }
                String nodeType = ConfigLoader.moteTypes.get(random.nextInt(ConfigLoader.moteTypes.size()));
                newTopology[i] = new Node(position, nodeType);
            }
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
