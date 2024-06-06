package CoojaTopologyTester;

import java.util.List;
import java.util.Random;

public class StrategySelector {
    public static MutationStrategy selectStrategy(List<MutationStrategy> strategies) {
        double totalEnergy = strategies.stream().mapToDouble(MutationStrategy::getEnergy).sum();
        double randomValue = new Random().nextDouble() * totalEnergy;

        double cumulativeEnergy = 0.0;
        for (MutationStrategy strategy : strategies) {
            cumulativeEnergy += strategy.getEnergy();
            if (cumulativeEnergy >= randomValue) {
                return strategy;
            }
        }
        return strategies.get(strategies.size() - 1);
    }
}