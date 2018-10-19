package paysim.base;

public class ActionProbability {
    private final String action;
    private final int month, day, hour, nbTransactions;
    private final double totalSum, average, std;

    public ActionProbability(String action, int month, int day, int hour, int nbTransactions, double totalSum, double average, double std) {
        this.action = action;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.nbTransactions = nbTransactions;
        this.totalSum = totalSum;
        this.average = average;
        this.std = std;
    }

    @Override
    public String toString() {
        return "Action: " + action + "\nMonth: " + month + "\nDay: " + day + "\nHour: " + hour
                + "\nNbTransactions: " + nbTransactions + "\nTotalSum: " + totalSum
                + "\nAverage: " + average + "\nStd: " + std + "\n";
    }

    public int getNbTransactions() {
        return nbTransactions;
    }

    public double getAverage() {
        return average;
    }

    public double getStd() {
        return std;
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