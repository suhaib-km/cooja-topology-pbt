package CoojaTopologyTester;

public abstract class MutationStrategy {
    private double energy = 1.0;

    public abstract Seed mutate(Seed seed);

    public double getEnergy() {
        return energy;
    }

    public void updateEnergy(double energy) {
        this.energy = energy;
    }
}
