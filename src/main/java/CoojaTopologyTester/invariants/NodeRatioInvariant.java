package CoojaTopologyTester.invariants;

import CoojaTopologyTester.Invariant;
import CoojaTopologyTester.Node;

import java.util.List;

public class NodeRatioInvariant implements Invariant {
    private String type1;
    private String type2;
    private double minRatio;
    private double maxRatio;

    public NodeRatioInvariant(String type1, String type2, double minRatio, double maxRatio) {
        this.type1 = type1;
        this.type2 = type2;
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
    }

    @Override
    public boolean check(Node[] topology, List<String> moteTypes) {
        int countType1 = 0;
        int countType2 = 0;

        for (Node node : topology) {
            if (node.getType().equals(type1)) {
                countType1++;
            } else if (node.getType().equals(type2)) {
                countType2++;
            }
        }

        if (countType2 == 0) {
            return countType1 == 0;
        }

        double ratio = (double) countType1 / countType2;
        return ratio >= minRatio && ratio <= maxRatio;
    }
}
