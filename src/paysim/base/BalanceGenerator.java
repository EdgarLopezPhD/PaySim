package paysim.base;

public class BalanceGenerator {
    private double low, high;

    public BalanceGenerator(double low, double high) {
        this.low = low;
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }
}
