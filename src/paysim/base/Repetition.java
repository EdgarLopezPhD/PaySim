package paysim.base;

public class Repetition {
    private final String type;
    private final double low, high, avg, std;

    public Repetition(String type, double low, double high, double avg, double std) {
        this.type = type;
        this.low = low;
        this.high = high;
        this.avg = avg;
        this.std = std;
    }

    @Override
    public String toString() {
        return "Repetition [type=" + type + ", low=" + low + ", high="
                + high + ", avg=" + avg + ", std=" + std
                + "]";
    }

    public String getType() {
        return type;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }
}