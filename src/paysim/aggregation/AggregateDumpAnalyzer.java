package paysim.aggregation;

import paysim.parameters.TransactionParameters;
import paysim.utils.CSVReader;

import java.util.ArrayList;

public class AggregateDumpAnalyzer {
    //The total number of transactions made for each action
    private double totNbTransfer = 0;
    private double totNbDebit = 0;
    private double totNbCashIn = 0;
    private double totNbCashOut = 0;
    private double totNbDeposit = 0;
    private double totNbPayments = 0;

    //The avg of the avg for each action
    private double avgAvgTransfer = 0;
    private double avgAvgDebit = 0;
    private double avgAvgCashIn = 0;
    private double avgAvgCashOut = 0;
    private double avgAvgDeposit = 0;
    private double avgAvgPayments = 0;

    //The avg of the std for each action
    private double avgStdTransfer = 0;
    private double avgStdDebit = 0;
    private double avgStdCashIn = 0;
    private double avgStdCashOut = 0;
    private double avgStdDeposit = 0;
    private double avgStdPayment = 0;

    private ArrayList<String[]> fileContents;

    public AggregateDumpAnalyzer(String fileName) {
        init(fileName);
    }

    private void init(String filename) {
        fileContents = CSVReader.read(filename);
    }

    public void analyze() {
        computeTotal();
        computeAvgAvg();
        computeAvgStd();
    }

    private void computeTotal() {
        totNbCashIn = getCount(TransactionParameters.indexOf("CASH_IN"));
        totNbCashOut = getCount(TransactionParameters.indexOf("CASH_OUT"));
        totNbDebit = getCount(TransactionParameters.indexOf("DEBIT"));
        totNbDeposit = getCount(TransactionParameters.indexOf("DEPOSIT"));
        totNbPayments = getCount(TransactionParameters.indexOf("PAYMENT"));
        totNbTransfer = getCount(TransactionParameters.indexOf("TRANSFER"));
    }

    private void computeAvgAvg() {
        avgAvgCashIn = getAvgAvg(TransactionParameters.indexOf("CASH_IN"));
        avgAvgCashOut = getAvgAvg(TransactionParameters.indexOf("CASH_OUT"));
        avgAvgDebit = getAvgAvg(TransactionParameters.indexOf("DEBIT"));
        avgAvgDeposit = getAvgAvg(TransactionParameters.indexOf("DEPOSIT"));
        avgAvgPayments = getAvgAvg(TransactionParameters.indexOf("PAYMENT"));
        avgAvgTransfer = getAvgAvg(TransactionParameters.indexOf("TRANSFER"));
    }

    private void computeAvgStd() {
        avgStdCashIn = getAvgStd(TransactionParameters.indexOf("CASH_IN"));
        avgStdCashOut = getAvgStd(TransactionParameters.indexOf("CASH_OUT"));
        avgStdDebit = getAvgStd(TransactionParameters.indexOf("DEBIT"));
        avgStdDeposit = getAvgStd(TransactionParameters.indexOf("DEPOSIT"));
        avgStdPayment = getAvgStd(TransactionParameters.indexOf("PAYMENT"));
        avgStdTransfer = getAvgAvg(TransactionParameters.indexOf("TRANSFER"));
    }

    private double getCount(int actionId) {
        double count = 0;

        for (String[] line : fileContents) {
            String action = line[0];
            String receivedAction = TransactionParameters.getAction(actionId);
            if (receivedAction.equals(action)) {
                count += Double.parseDouble(line[4]);
            }
        }

        return count;
    }

    private double getAvgAvg(int actionId) {
        double avg = 0, nr = 0;

        for (String[] line : fileContents) {
            String action = line[0];
            String receivedAction = TransactionParameters.getAction(actionId);
            if (receivedAction.equals(action)) {
                avg += Double.parseDouble(line[6]);
                nr++;
            }
        }
        avg = avg / nr;

        return avg;
    }

    private double getAvgStd(int actionId) {
        double avgStd = 0;
        double nr = 0;

        for (String[] line : fileContents) {
            String action = line[0];

            String receivedAction = TransactionParameters.getAction(actionId);
            if (receivedAction.equals(action)) {
                avgStd += Double.parseDouble(line[7]);
                nr++;
            }
        }
        avgStd = avgStd / nr;

        return avgStd;
    }


    public double getTotNbTransfer() {
        return totNbTransfer;
    }

    public double getTotNbDebit() {
        return totNbDebit;
    }

    public double getTotNbCashIn() {
        return totNbCashIn;
    }

    public double getTotNbCashOut() {
        return totNbCashOut;
    }

    public double getTotNbPayments() {
        return totNbPayments;
    }

    public double getAvgAvgTransfer() {
        return avgAvgTransfer;
    }

    public double getAvgAvgDebit() {
        return avgAvgDebit;
    }

    public double getAvgAvgCashIn() {
        return avgAvgCashIn;
    }

    public double getAvgAvgCashOut() {
        return avgAvgCashOut;
    }

    public double getAvgAvgPayments() {
        return avgAvgPayments;
    }

    @Override
    public String toString() {
        return "AggregateDumpAnalyzer [totNbTransfer=" + totNbTransfer
                + ", totNbDebit=" + totNbDebit + ", totNbCashIn="
                + totNbCashIn + ", totNbCashOut=" + totNbCashOut
                + ", totNbDeposit=" + totNbDeposit + ", totNbPayments="
                + totNbPayments + ", avgAvgTransfer=" + avgAvgTransfer
                + ", avgAvgDebit=" + avgAvgDebit + ", avgAvgCashIn="
                + avgAvgCashIn + ", avgAvgCashOut=" + avgAvgCashOut
                + ", avgAvgDeposit=" + avgAvgDeposit + ", avgAvgPayments="
                + avgAvgPayments + ", avgStdTransfer=" + avgStdTransfer
                + ", avgStdDebit=" + avgStdDebit + ", avgStdCashIn="
                + avgStdCashIn + ", avgStdCashOut=" + avgStdCashOut
                + ", avgStdDeposit=" + avgStdDeposit + ", avgStdPayment="
                + avgStdPayment + "]";
    }
}