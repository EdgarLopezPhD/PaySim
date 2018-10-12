package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class TransferMaxHandler {
    private ArrayList<String> fileContents = new ArrayList<>();
    PaySim paysim;

    //The max limits for each type
    int transferMax = 0;
    int paymentMax = 0;
    int cashInMax = 0;
    int cashOutMax = 0;
    int debitMax = 0;
    int depositMax = 0;

    public TransferMaxHandler(String path) {
        try {
            File f = new File(path);
            FileReader reader = new FileReader(f);
            BufferedReader bufReader = new BufferedReader(reader);
            String line = "";
            bufReader.readLine();
            while ((line = bufReader.readLine()) != null) {
                fileContents.add(line);
            }
            bufReader.close();
            initMax(fileContents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMax(ArrayList<String> fileContents) {
        transferMax = Integer.parseInt(fileContents.get(6).split(",")[1]);
        paymentMax = Integer.parseInt(fileContents.get(4).split(",")[1]);
        cashInMax = Integer.parseInt(fileContents.get(0).split(",")[1]);
        cashOutMax = Integer.parseInt(fileContents.get(1).split(",")[1]);
        debitMax = Integer.parseInt(fileContents.get(2).split(",")[1]);
        depositMax = Integer.parseInt(fileContents.get(7).split(",")[1]);
    }

    @Override
    public String toString() {
        return "TransferMaxHandler [transferMax=" + transferMax
                + ", paymentMax=" + paymentMax + ", cashInMax=" + cashInMax
                + ", cashOutMax=" + cashOutMax + ", debitMax=" + debitMax
                + ", depositMax=" + depositMax + "]";
    }

    public double getMaxGivenType(String type) {
        switch (type) {
            case "TRANSFER":
                return this.transferMax;

            case "CASH_IN":
                return this.cashInMax;

            case "CASH_OUT":
                return this.cashOutMax;

            case "DEBIT":
                return this.debitMax;

            case "DEPOSIT":
                return this.depositMax;

            case "PAYMENT":
                return this.paymentMax;

            default:
                return -1.0;
        }
    }
}