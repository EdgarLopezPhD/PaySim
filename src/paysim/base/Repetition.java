package paysim.base;

public class Repetition {
    private final String action;
    private final int low, high;
    private final double avg, std;

    public Repetition(String action, int low, int high, double avg, double std) {
        this.action = action;
        this.low = low;
        this.high = high;
        this.avg = avg;
        this.std = std;
    }

    @Override
    public String toString() {
        return "Repetition [action=" + action + ", low=" + low + ", high="
                + high + ", avg=" + avg + ", std=" + std
                + "]";
    }

    public String getAction() {
        return action;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }
}