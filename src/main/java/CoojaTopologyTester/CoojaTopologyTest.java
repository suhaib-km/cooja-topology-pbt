package CoojaTopologyTester;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeTry;
import org.assertj.core.api.Assertions;

import java.util.*;
import CoojaTopologyTester.strategies.*;

public class CoojaTopologyTest {

    private static final List<Seed> seeds = new ArrayList<>();
    private final StrategySelector strategySelector = new StrategySelector();
    private static final double TARGET_DUTY_CYCLE = 0.5; // Define your target duty cycle boundary here

    static {
        Seed.initializeSeeds(seeds, new RandomStrategy());
    }

    @Property(tries = 1000)
    public void testRadioDutyCycleProperty(@ForAll("generateSeed") Seed seed) {
        System.out.println("Running property-based tests...");

        // Randomly choose a mutation strategy for this test run
        MutationStrategy mutationStrategy = strategySelector.selectStrategy();
        seed = mutationStrategy.mutate(seed);

        double dutyCycle = CoojaTopologyTester.testRadioDutyCycles(seed);

        // Check if the duty cycle violates the property
        double energyChange = 1.0 / (Math.abs(dutyCycle - TARGET_DUTY_CYCLE) + 1);
        mutationStrategy.updateEnergy(energyChange);
        Seed.updateSeedEnergy(seed, dutyCycle);

        if (Math.abs(dutyCycle - TARGET_DUTY_CYCLE) > 0.1) { // Example threshold
            System.out.println("Property violated. Node positions: ");
            for (double[] position : seed.getTopology()) {
                System.out.println(Arrays.toString(position));
            }
            // Use assertions to fail the test and log the positions
            Assertions.fail("Property violated with node positions: " + Arrays.deepToString(seed.getTopology()));
        }
    }

    @Provide
    Arbitrary<Seed> generateSeed() {
        return Arbitraries.of(seeds).flatMap(seed -> Arbitraries.lazy(() -> mutationStrategyFactory(seed)));
    }

    private Arbitrary<Seed> mutationStrategyFactory(Seed seed) {
        // Implement the mutation strategy choice logic here
        MutationStrategy strategy = strategySelector.selectStrategy();
        return Arbitraries.just(strategy.mutate(seed));
    }
}
