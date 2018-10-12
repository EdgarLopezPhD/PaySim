package paysim.aggregation;

public class AggregateTransactionRecord implements Comparable<AggregateTransactionRecord> {
    String type, month, tDay, tHour, tCount, tSum, tAvg, tStd, tStep;

    public AggregateTransactionRecord(String type, String month, String tDay, String tHour, String tCount,
                                      String tSum, String tAvg, String tStd, String tStep) {
        this.type = type;
        this.month = month;
        this.tDay = tDay;
        this.tHour = tHour;
        this.tCount = tCount;
        this.tSum = tSum;
        this.tAvg = tAvg;
        this.tStd = tStd;
        this.tStep = tStep;
    }

    @Override
    public int compareTo(AggregateTransactionRecord record) {
        double inDay = Double.parseDouble(tDay);
        double inHour = Double.parseDouble(tHour);
        double inType = Double.parseDouble(type);
        double inputDay = Double.parseDouble(record.gettDay());
        double inputHour = Double.parseDouble(record.gettHour());
        double inputType = Double.parseDouble(record.getType());

        double valIn = ((inType * 1000000) + (10000 * inDay) + (10 * inHour));
        double valInput = ((inputType * 1000000) + (10000 * inputDay) + (10 * inputHour));

        if (valIn > valInput) {
            return 1;
        } else if (valIn < valInput) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "AggregateTransactionRecord [type=" + type + ", month=" + month
                + ", tDay=" + tDay + ", tHour=" + tHour + ", tCount=" + tCount
                + ", tSum=" + tSum + ", tAvg=" + tAvg + ", tStd=" + tStd
                + ", tStep=" + tStep + "]";
    }

    public boolean equals(AggregateTransactionRecord rec) {
        if (this.type.equals(rec.getType()) &&
                this.tHour.equals(rec.gettHour()) &&
                this.tDay.equals(rec.gettDay())) {
            return true;
        } else {
            return false;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMonth() {
        return month;
    }

    public String gettDay() {
        return tDay;
    }

    public String gettHour() {
        return tHour;
    }

    public String gettCount() {
        return tCount;
    }

    public String gettSum() {
        return tSum;
    }

    public String gettAvg() {
        return tAvg;
    }

    public String gettStd() {
        return tStd;
    }
}
