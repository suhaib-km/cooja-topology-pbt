package CoojaTopologyTester.strategies;

import CoojaTopologyTester.Seed;
import CoojaTopologyTester.Node;
import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.ConfigLoader;
import CoojaTopologyTester.MutationStrategy;

import java.util.List;
import java.util.Random;

public class CompleteDisplacementStrategy extends MutationStrategy {

    @Override
    public Seed mutate(Seed seed, List<Invariant> invariants) {
        Random random = new Random();
        Node[] newTopology;

        do {
            newTopology = new Node[seed.getTopology().length];
            double[] center = calculateCenter(seed.getTopology());

            boolean pullTowardsCenter = random.nextBoolean();

            // Transfer existing node types to new topology
            for (int i = 0; i < seed.getTopology().length; i++) {
                double[] directionVector = calculateDirectionVector(seed.getTopology()[i].getPosition(), center);
                double[] unitVector = normalize(directionVector);

                double displacementFactor = pullTowardsCenter ? -1 : 1;
                double[] newPosition = new double[3];
                for (int j = 0; j < 3; j++) {
                    newPosition[j] = seed.getTopology()[i].getPosition()[j] + displacementFactor * unitVector[j] * random.nextDouble();
                }
                String nodeType = seed.getTopology()[i].getType(); // Preserve original node type
                newTopology[i] = new Node(newPosition, nodeType);
            }
        } while (!checkInvariants(newTopology, invariants));

        return new Seed(newTopology, seed.getEnergy());
    }

    private double[] calculateCenter(Node[] topology) {
        double[] center = new double[3];
        for (Node node : topology) {
            double[] position = node.getPosition();
            for (int i = 0; i < 3; i++) {
                center[i] += position[i];
            }
        }
        for (int i = 0; i < 3; i++) {
            center[i] /= topology.length;
        }
        return center;
    }

    private double[] calculateDirectionVector(double[] point, double[] center) {
        double[] directionVector = new double[3];
        for (int i = 0; i < 3; i++) {
            directionVector[i] = center[i] - point[i];
        }
        return directionVector;
    }

    private double[] normalize(double[] vector) {
        double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        double[] unitVector = new double[3];
        for (int i = 0; i < 3; i++) {
            unitVector[i] = vector[i] / length;
        }
        return unitVector;
    }

    private boolean checkInvariants(Node[] topology, List<Invariant> invariants) {
        List<String> nodeTypes = ConfigLoader.moteTypes;

        for (Invariant property : invariants) {
            if (!property.check(topology, nodeTypes)) {
                return false;
            }
        }
        return true;
    }
}
