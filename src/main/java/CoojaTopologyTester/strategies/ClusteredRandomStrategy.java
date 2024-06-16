package CoojaTopologyTester.strategies;

import CoojaTopologyTester.Seed;
import CoojaTopologyTester.Node;
import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.ConfigLoader;
import CoojaTopologyTester.MutationStrategy;

import java.util.List;
import java.util.Random;

public class ClusteredRandomStrategy extends MutationStrategy {

    @Override
    public Seed mutate(Seed seed, List<Invariant> invariants) {
        List<String> nodeTypes = ConfigLoader.moteTypes;
        Random rand = new Random();
        Node[] newTopology;
        do {
            newTopology = new Node[seed.getTopology().length];
            int numClusters = rand.nextInt(3) + 1;
            Node[] clusterCenters = new Node[numClusters];
            
            // Generate cluster centers
            for (int i = 0; i < numClusters; i++) {
                double[] position = {rand.nextDouble(), rand.nextDouble(), rand.nextDouble()};
                clusterCenters[i] = new Node(position, getRandomNodeType(nodeTypes));
            }
            
            // Assign nodes to clusters
            for (int i = 0; i < newTopology.length; i++) {
                int clusterIdx = rand.nextInt(numClusters);
                double[] position = {
                    clusterCenters[clusterIdx].getPosition()[0] + rand.nextDouble(),
                    clusterCenters[clusterIdx].getPosition()[1] + rand.nextDouble(),
                    clusterCenters[clusterIdx].getPosition()[2] + rand.nextDouble()
                };
                newTopology[i] = new Node(position, clusterCenters[clusterIdx].getType());
            }
        } while (!checkInvariants(newTopology, invariants));
        
        return new Seed(newTopology, seed.getEnergy());
    }

    private String getRandomNodeType(List<String> nodeTypes) {
        Random rand = new Random();
        if (nodeTypes.size() <= 0)
        {
            System.out.println("No Node types");
        }
        return nodeTypes.get(rand.nextInt(nodeTypes.size()));
    }

    private boolean checkInvariants(Node[] topology, List<Invariant> invariants) {
        
        for (Invariant property : invariants) {
            if (!property.check(topology, ConfigLoader.moteTypes)) {
                return false;
            }
        }
        return true;
    }
}
