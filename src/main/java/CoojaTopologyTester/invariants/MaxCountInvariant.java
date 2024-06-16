package CoojaTopologyTester.invariants;

import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.Node;

import java.util.List;

public class MaxCountInvariant implements Invariant {
    private final String moteType;
    private final int maxMotes;

    public MaxCountInvariant(String moteType, int maxMotes) {
        this.moteType = moteType;
        this.maxMotes = maxMotes;
    }

    @Override
    public boolean check(Node[] topology, List<String> moteTypes) {
        int count = 0;
        for (Node node : topology) {
            if (node.getType().equals(moteType)) {
                count++;
            }
        }
        return count <= maxMotes;
    }
}
