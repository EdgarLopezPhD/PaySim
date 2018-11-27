package paysim.base;

public class StepActionProfile {
    private final String action;
    private final int month, day, hour, count;
    private final double totalSum, avgAmount, stdAmount;

    public StepActionProfile(String action, int month, int day, int hour, int count, double totalSum, double avgAmount, double stdAmount) {
        this.action = action;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.count = count;
        this.totalSum = totalSum;
        this.avgAmount = avgAmount;
        this.stdAmount = stdAmount;
    }

    @Override
    public String toString() {
        return "Action: " + action + "\nMonth: " + month + "\nDay: " + day + "\nHour: " + hour
                + "\nNbTransactions: " + count + "\nTotalSum: " + totalSum
                + "\nAverage: " + avgAmount + "\nStd: " + stdAmount + "\n";
    }

    public int getCount() {
        return count;
    }

    public double getAvgAmount() {
        return avgAmount;
    }

    public double getStdAmount() {
        return stdAmount;
    }

    public String getAction() {
        return action;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public double getTotalSum() {
        return totalSum;
    }
}