package CoojaTopologyTester;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeTry;
import org.assertj.core.api.Assertions;

import java.util.*;                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
import CoojaTopologyTester.strategies.*;
import CoojaTopologyTester.Node;

public class CoojaTopologyTest {

    private static final List<Seed> seeds = new ArrayList<>();
    private final StrategySelector strategySelector = new StrategySelector();
    private static final double TARGET_DUTY_CYCLE = 0.017; // Define your target duty cycle boundary here
    private static final String CONFIG_FILE = "config.properties";
    
    private double previousHighestDutyCycle = Double.POSITIVE_INFINITY;

    static {
        ConfigLoader.loadConfig(CONFIG_FILE);
        Seed.initializeSeeds(seeds, (MutationStrategy) new RandomStrategy(), ConfigLoader.invariants);
    }

    @Property(tries = 50, shrinking = ShrinkingMode.BOUNDED, generation = GenerationMode.RANDOMIZED) 
    public void testRadioDutyCycleProperty(@ForAll("generateSeed") Seed seed) {
        System.out.println("Running test with seed: " + Arrays.toString(seed.getTopology()));

        MutationStrategy mutationStrategy = strategySelector.selectStrategy();
        Seed newSeed = mutationStrategy.mutate(seed, ConfigLoader.invariants);

        double[] dutyCycles = CoojaTopologyTester.testRadioDutyCycles(newSeed);
        if (dutyCycles == null) return;
        double dutyCycle = Arrays.stream(dutyCycles).max().getAsDouble();
       
        if (dutyCycle < previousHighestDutyCycle) {
            double currentDeviation = Math.abs(dutyCycle - TARGET_DUTY_CYCLE);
            double previousDeviation = Math.abs(previousHighestDutyCycle - TARGET_DUTY_CYCLE);
            if (previousDeviation == 0) {
                previousDeviation = 1e-6;
            }
            double relativeChangeFactor = currentDeviation / previousDeviation;
            double energyChange = seed.getEnergy() * (1.0 / (currentDeviation + 1.0)) * relativeChangeFactor;
            mutationStrategy.updateEnergy(energyChange);
            Seed.updateSeedEnergy(newSeed, dutyCycle);
            seeds.add(newSeed);
            Seed.normaliseEnergy(seeds);
            previousHighestDutyCycle = dutyCycle; 
        }

        if (Math.abs(dutyCycle) > TARGET_DUTY_CYCLE) {
            System.out.println("Property violated. Node positions: ");
            for (Node node : seed.getTopology()) {
                double[] position = node.getPosition();
                System.out.println(Arrays.toString(position));
            }
            Assertions.fail("Property violated with node positions: " + Arrays.deepToString(seed.getTopology()));
        }
    }

    @Provide
    Arbitrary<Seed> generateSeed() {
        return Arbitraries.of(seeds).flatMap(seed -> Arbitraries.lazy(() -> mutationStrategyFactory(seed)));
    }
    // @Provide
    // Arbitrary<Seed> generateSeed() {
    //     return Arbitraries.of(seeds);
    // }

    private Arbitrary<Seed> mutationStrategyFactory(Seed seed) {
        MutationStrategy strategy = strategySelector.selectStrategy();
        return Arbitraries.just(strategy.mutate(seed, ConfigLoader.invariants));
    }
}
