package CoojaTopologyTester.invariants;

import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.Node;
import java.util.List;

public class NodeCountInvariant implements Invariant {
    private String type;
    private int minCount;
    private int maxCount;

    public NodeCountInvariant(String type, int minCount, int maxCount) {
        this.type = type;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    @Override
    public boolean check(Node[] topology, List<String> moteTypes) {
        int count = 0;
        for (Node node : topology) {
            double[] position = node.getPosition();
            if (moteTypes.get((int) position[0]).equals(type)) {
                count++;
            }
        }
        return count >= minCount && count <= maxCount;
    }
}
