package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import paysim.Transaction.TransactionType;

public class AggregateDumpAnalyzer {
    //The total number of transactions made for each type
    private double totNrOfTransfer = 0;
    private double totNrOfDebit = 0;
    private double totNrOfCashIn = 0;
    private double totNrOfCashOut = 0;
    private double totNrOfDeposit = 0;
    private double totNrOfPayments = 0;

    //The avg of the avg for each type
    private double avgAvgTransfer = 0;
    private double avgAvgDebit = 0;
    private double avgAvgCashIn = 0;
    private double avgAvgCashOut = 0;
    private double avgAvgDeposit = 0;
    private double avgAvgPayments = 0;

    //The avg of the std for each type
    private double avgStdTransfer = 0;
    private double avgStdDebit = 0;
    private double avgStdCashIn = 0;
    private double avgStdCashOut = 0;
    private double avgStdDeposit = 0;
    private double avgStdPayment = 0;

    private ArrayList<String> fileContents = new ArrayList<>();

    public AggregateDumpAnalyzer(String fileName) {
        init(fileName);
    }

    private void init(String fileName) {
        try {
            File f = new File(fileName);
            FileReader fReader = new FileReader(f);
            BufferedReader reader = new BufferedReader(fReader);
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                fileContents.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void analyze() {
        computeTotal();
        computeAvgAvg();
        computeAvgStd();
    }

    private void computeTotal() {
        totNrOfCashIn = getCount(TransactionType.CASH_IN.getValue());
        totNrOfCashOut = getCount(TransactionType.CASH_OUT.getValue());
        totNrOfDebit = getCount(TransactionType.DEBIT.getValue());
        totNrOfDeposit = getCount(TransactionType.DEPOSIT.getValue());
        totNrOfPayments = getCount(TransactionType.PAYMENT.getValue());
        totNrOfTransfer = getCount(TransactionType.TRANSFER.getValue());
    }

    private void computeAvgAvg() {
        avgAvgCashIn = getAvgAvg(TransactionType.CASH_IN.getValue());
        avgAvgCashOut = getAvgAvg(TransactionType.CASH_OUT.getValue());
        avgAvgDebit = getAvgAvg(TransactionType.DEBIT.getValue());
        avgAvgDeposit = getAvgAvg(TransactionType.DEPOSIT.getValue());
        avgAvgPayments = getAvgAvg(TransactionType.PAYMENT.getValue());
        avgAvgTransfer = getAvgAvg(TransactionType.TRANSFER.getValue());
    }

    private void computeAvgStd() {
        avgStdCashIn = getAvgStd(TransactionType.CASH_IN.getValue());
        avgStdCashOut = getAvgStd(TransactionType.CASH_OUT.getValue());
        avgStdDebit = getAvgStd(TransactionType.DEBIT.getValue());
        avgStdDeposit = getAvgStd(TransactionType.DEPOSIT.getValue());
        avgStdPayment = getAvgStd(TransactionType.PAYMENT.getValue());
        avgStdTransfer = getAvgAvg(TransactionType.TRANSFER.getValue());
    }

    public void printSummary() {
        DecimalFormat format = new DecimalFormat("#.##########");

        String split[] = this.toString().split(",");
        for (String s : split) {
            System.out.println(s + "\n");
        }

        double total = this.totNrOfCashIn + this.totNrOfCashOut + this.totNrOfDebit +
                this.totNrOfDeposit + this.totNrOfPayments + this.totNrOfTransfer;
        System.out.println("\n\nTot\t" + format.format(total) + "\n");
    }

    private boolean isNumber(String type) {
        try {
            Double.parseDouble(type);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //Handler function
    private double getCount(int typeNr) {
        double count = 0;

        for (String s : this.fileContents) {
            String split[] = s.split(",");
            String type = split[0];

            //Handle from the original aggregate file
            if (!isNumber(type)) {
                String receivedType = TransactionType.nameOf(typeNr);
                if (receivedType.equals(type)) {
                    count += Double.parseDouble(split[4]);
                }
            } else {
                //Handle from the generated aggregate file
                if (Double.parseDouble(type) == typeNr) {
                    count += Double.parseDouble(split[4]);
                }
            }
        }

        return count;
    }

    private double getAvgAvg(int typeNr) {
        double avg = 0, nr = 0;

        for (String s : this.fileContents) {
            String split[] = s.split(",");
            String type = split[0];

            //Handle from the original aggregate file
            if (!isNumber(type)) {
                String receivedType = TransactionType.nameOf(typeNr);
                if (receivedType.equals(type)) {
                    avg += Double.parseDouble(split[6]);
                    nr++;
                }
            } else {
                //Handle from the generated aggregate file
                if (Double.parseDouble(type) == typeNr) {
                    avg += Double.parseDouble(split[6]);
                    nr++;
                }
            }
        }
        avg = avg / nr;

        return avg;
    }

    private double getAvgStd(int typeNr) {
        double avgStd = 0;
        double nr = 0;

        for (String s : this.fileContents) {
            String splitted[] = s.split(",");
            String type = splitted[0];

            //Handle from the original aggregate file
            if (!isNumber(type)) {
                String receivedType = TransactionType.nameOf(typeNr);
                if (receivedType.equals(type)) {
                    avgStd += Double.parseDouble(splitted[7]);
                    nr++;
                }
            } else {
                //Handle from the generated aggregate file
                if (Double.parseDouble(type) == typeNr) {
                    avgStd += Double.parseDouble(splitted[7]);
                    nr++;
                }
            }

        }
        avgStd = avgStd / nr;

        return avgStd;
    }


    public double getTotNrOfTransfer() {
        return totNrOfTransfer;
    }

    public double getTotNrOfDebit() {
        return totNrOfDebit;
    }

    public double getTotNrOfCashIn() {
        return totNrOfCashIn;
    }

    public double getTotNrOfCashOut() {
        return totNrOfCashOut;
    }

    public double getTotNrOfPayments() {
        return totNrOfPayments;
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
        return "AggregateDumpAnalyzer [totNrOfTransfer=" + totNrOfTransfer
                + ", totNrOfDebit=" + totNrOfDebit + ", totNrOfCashIn="
                + totNrOfCashIn + ", totNrOfCashOut=" + totNrOfCashOut
                + ", totNrOfDeposit=" + totNrOfDeposit + ", totNrOfPayments="
                + totNrOfPayments + ", avgAvgTransfer=" + avgAvgTransfer
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