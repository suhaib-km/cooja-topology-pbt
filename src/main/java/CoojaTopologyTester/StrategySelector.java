package CoojaTopologyTester;

import CoojaTopologyTester.strategies.RandomStrategy;
import CoojaTopologyTester.strategies.CompleteDisplacementStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StrategySelector {
    private final List<MutationStrategy> strategies;
    private final Random random = new Random();

    public StrategySelector() {
        strategies = Arrays.asList(
                new RandomStrategy(),
                new CompleteDisplacementStrategy()
                // todo: Add other strategies
        );
    }

    public MutationStrategy selectStrategy() {
        double totalEnergy = strategies.stream().mapToDouble(MutationStrategy::getEnergy).sum();
        double randomValue = random.nextDouble() * totalEnergy;

        double cumulativeEnergy = 0.0;
        for (MutationStrategy strategy : strategies) {
            cumulativeEnergy += strategy.getEnergy();
            if (cumulativeEnergy >= randomValue) {
                return strategy;
            }
        }

        return strategies.get(random.nextInt(strategies.size()));
    }
}
