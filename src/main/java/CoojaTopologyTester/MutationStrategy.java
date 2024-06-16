package CoojaTopologyTester;

import java.util.List;

public abstract class MutationStrategy {
    private double energy = 1.0;

    public abstract Seed mutate(Seed seed, List<Invariant> properties);

    public double getEnergy() {
        return energy;
    }

    public void updateEnergy(double energy) {
        this.energy = energy;
    }
}
