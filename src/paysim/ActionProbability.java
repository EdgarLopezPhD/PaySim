package paysim;

public class ActionProbability {
    String type;
    private double month;
    private double day;
    private double hour;
    private double nrOfTransactions;
    private double totalSum;
    private double average;
    private double std;

    public ActionProbability() {
        type = "";
        month = 0;
        day = 0;
        hour = 0;
        nrOfTransactions = 0;
        totalSum = 0;
        average = 0;
        std = 0;
    }

    @Override
    public String toString() {
        return "Type: " + type + "\nMonth: " + month + "\nDay: " + day + "\nHour: " + hour
                + "\nNrOfTransactions: " + nrOfTransactions + "\nTotalSum: " + totalSum
                + "\nAverage: " + average + "\nStd: " + std + "\n";
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMonth(double month) {
        this.month = month;
    }

    public void setDay(double day) {
        this.day = day;
    }

    public void setHour(double hour) {
        this.hour = hour;
    }

    public double getNrOfTransactions() {
        return nrOfTransactions;
    }

    public void setNrOfTransactions(double nrOfTransactions) {
        this.nrOfTransactions = nrOfTransactions;
    }

    public void setTotalSum(double totalSum) {
        this.totalSum = totalSum;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getStd() {
        return std;
    }

    public void setStd(double std) {
        this.std = std;
    }
}