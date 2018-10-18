package paysim.aggregation;

import paysim.parameters.TransactionParameters;

public class AggregateTransactionRecord implements Comparable<AggregateTransactionRecord> {
    private final String action, month, day, hour, count, sum, avg, std, step;

    public AggregateTransactionRecord(String action, String month, String day, String hour, String count,
                                      String sum, String avg, String std, String tStep) {
        this.action = action;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.count = count;
        this.sum = sum;
        this.avg = avg;
        this.std = std;
        this.step = tStep;
    }

    @Override
    public int compareTo(AggregateTransactionRecord record) {
        double inDay = Double.parseDouble(day);
        double inHour = Double.parseDouble(hour);
        double inAction = (double) (TransactionParameters.indexOf(action));
        double inputDay = Double.parseDouble(record.getDay());
        double inputHour = Double.parseDouble(record.getHour());
        double inputAction = (double) (TransactionParameters.indexOf(record.getAction()));

        double valIn = ((inAction * 1000000) + (10000 * inDay) + (10 * inHour));
        double valInput = ((inputAction * 1000000) + (10000 * inputDay) + (10 * inputHour));

        if (valIn > valInput) {
            return 1;
        } else if (valIn < valInput) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "AggregateTransactionRecord [action=" + action + ", month=" + month
                + ", day=" + day + ", hour=" + hour + ", count=" + count
                + ", sum=" + sum + ", avg=" + avg + ", std=" + std
                + ", step=" + step + "]";
    }

    public boolean equals(AggregateTransactionRecord rec) {
        return action.equals(rec.getAction()) && step.equals(rec.getStep());
    }

    public String getAction() {
        return action;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public String getHour() {
        return hour;
    }

    public String getCount() {
        return count;
    }

    public String getSum() {
        return sum;
    }

    public String getAvg() {
        return avg;
    }

    public String getStd() {
        return std;
    }

    public String getStep() {
        return step;
    }
}
