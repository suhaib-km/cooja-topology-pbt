package CoojaTopologyTester.strategies;

import java.util.Random;
import CoojaTopologyTester.Seed;
import CoojaTopologyTester.MutationStrategy;


public class ClusteredRandomStrategy extends MutationStrategy {
    @Override
    public Seed mutate(Seed seed) {
        Random rand = new Random();
        double[][] newTopology = new double[seed.getTopology().length][3];
        int numClusters = rand.nextInt(3) + 1; // 1 to 3 clusters
        double[][] clusterCenters = new double[numClusters][3];
        
        for (int i = 0; i < numClusters; i++) {
            clusterCenters[i][0] = rand.nextDouble() * 10;
            clusterCenters[i][1] = rand.nextDouble() * 10;
            clusterCenters[i][2] = rand.nextDouble() * 10;
        }
        
        for (int i = 0; i < newTopology.length; i++) {
            int cluster = rand.nextInt(numClusters);
            newTopology[i][0] = clusterCenters[cluster][0] + (rand.nextDouble() - 0.5);
            newTopology[i][1] = clusterCenters[cluster][1] + (rand.nextDouble() - 0.5);
            newTopology[i][2] = clusterCenters[cluster][2] + (rand.nextDouble() - 0.5);
        }
        
        return new Seed(newTopology, seed.getEnergy());
    }
}