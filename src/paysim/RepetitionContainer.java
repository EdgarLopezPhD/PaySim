package paysim;

public class RepetitionContainer {
    private String type;
    private double low, high, count, avg, std;

    public RepetitionContainer() {
        type = "";
        low = 0;
        high = 0;
        count = 0;
        avg = 0;
        std = 0;
    }

    @Override
    public String toString() {
        return "RepetitionContainer [type=" + type + ", low=" + low + ", high="
                + high + ", count=" + count + ", avg=" + avg + ", std=" + std
                + "]";
    }

    public boolean equals(RepetitionContainer cont) {
        return type.equals(cont.getType()) &&
                low == cont.getLow() &&
                high == cont.getHigh();
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

    public void setLow(double low) {
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public void setStd(double std) {
        this.std = std;
    }

}