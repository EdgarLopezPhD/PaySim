package org.paysim.paysim.base;

import java.io.Serializable;
import java.util.ArrayList;

import org.paysim.paysim.output.Output;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int step;
    private final String action;
    private final double amount;

    private final String nameOrig;
    private final double oldBalanceOrig, newBalanceOrig;

    private final String nameDest;
    private final double oldBalanceDest, newBalanceDest;

    private boolean isFraud = false;
    private boolean isFlaggedFraud = false;
    private boolean isUnauthorizedOverdraft = false;

    public Transaction(int step, String action, double amount, String nameOrig, double oldBalanceOrig,
                       double newBalanceOrig, String nameDest, double oldBalanceDest, double newBalanceDest) {
        this.step = step;
        this.action = action;
        this.amount = amount;
        this.nameOrig = nameOrig;
        this.oldBalanceOrig = oldBalanceOrig;
        this.newBalanceOrig = newBalanceOrig;
        this.nameDest = nameDest;
        this.oldBalanceDest = oldBalanceDest;
        this.newBalanceDest = newBalanceDest;
    }

    public boolean isFailedTransaction(){
        return isFlaggedFraud || isUnauthorizedOverdraft;
    }

    public void setFlaggedFraud(boolean isFlaggedFraud) {
        this.isFlaggedFraud = isFlaggedFraud;
    }

    public void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
    }

    public void setUnauthorizedOverdraft(boolean isUnauthorizedOverdraft) {
        this.isUnauthorizedOverdraft = isUnauthorizedOverdraft;
    }

    public boolean isFlaggedFraud() {
        return isFlaggedFraud;
    }

    public boolean isFraud() {
        return isFraud;
    }

    public int getStep() {
        return step;
    }

    public String getAction() {
        return action;
    }

    public double getAmount() {
        return amount;
    }

    public String getNameOrig() {
        return nameOrig;
    }

    public double getOldBalanceOrig() {
        return oldBalanceOrig;
    }

    public double getNewBalanceOrig() {
        return newBalanceOrig;
    }

    public String getNameDest() {
        return nameDest;
    }

    public double getOldBalanceDest() {
        return oldBalanceDest;
    }

    public double getNewBalanceDest() {
        return newBalanceDest;
    }

    @Override
    public String toString(){
        ArrayList<String> properties = new ArrayList<>();

        properties.add(String.valueOf(step));
        properties.add(action);
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, amount));
        properties.add(nameOrig);
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, oldBalanceOrig));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, newBalanceOrig));
        properties.add(nameDest);
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, oldBalanceDest));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, newBalanceDest));
        properties.add(Output.formatBoolean(isFraud));
        properties.add(Output.formatBoolean(isFlaggedFraud));
        properties.add(Output.formatBoolean(isUnauthorizedOverdraft));

        return String.join(Output.OUTPUT_SEPARATOR, properties);
    }
}
