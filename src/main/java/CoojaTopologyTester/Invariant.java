package CoojaTopologyTester;

import java.util.List;

public interface Invariant {
    boolean check(Node[] topology, List<String> moteTypes);
}
