package paysim;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepetitionContainer that = (RepetitionContainer) o;
        return Double.compare(that.low, low) == 0 &&
                Double.compare(that.high, high) == 0 &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, low, high);
    }
}