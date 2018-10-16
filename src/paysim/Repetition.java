package paysim;

public class Repetition {
    private String type;
    private double low, high, avg, std;

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

    public void setType(String type) {
        this.type = type;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }
}