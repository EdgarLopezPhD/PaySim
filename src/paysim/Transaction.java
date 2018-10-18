package paysim;

import java.io.Serializable;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long step;
    private final String type;
    private final double amount;

    private final String nameOrig;
    private final double oldBalanceOrig, newBalanceOrig;

    private final String nameDest;
    private final double oldBalanceDest, newBalanceDest;

    private boolean isFraud = false;
    private boolean isFlaggedFraud = false;

    public Transaction(long step, String type, double amount, String nameOrig, double oldBalanceOrig,
                       double newBalanceOrig, String nameDest, double oldBalanceDest, double newBalanceDest) {
        this.step = step;
        this.type = type;
        this.amount = amount;
        this.nameOrig = nameOrig;
        this.oldBalanceOrig = oldBalanceOrig;
        this.newBalanceOrig = newBalanceOrig;
        this.nameDest = nameDest;
        this.oldBalanceDest = oldBalanceDest;
        this.newBalanceDest = newBalanceDest;
    }

    public String toString() {
        String ps = "";

        /**if (this.newBalanceDest == 0) {
            ps = Long.toString(step) + " " + clientOrigBefore.toString() + "\t" + "Amount:\t" + Double.toString(amount)
                    + "\tnew Balance " + Double.toString(newBalanceOrig) + "\t" + "\n";
        } else {
            ps = Long.toString(step) + " " + clientOrigBefore.toString() + "\t" + this.clientOrigBefore.toString() + "(" + this.newBalanceOrig
                    + ") Transfered: " + Double.toString(amount) + " to " + this.clientOrigAfter.toString() + " (" + this.newBalanceDest + ")\t" +
                    "\tnew Balance " + Double.toString(newBalanceOrig) + "\t" + "\n";
        }**/

        return ps;
    }

    public boolean isFlaggedFraud() {
        return isFlaggedFraud;
    }

    public void setFlaggedFraud(boolean isFlaggedFraud) {
        this.isFlaggedFraud = isFlaggedFraud;
    }

    public boolean isFraud() {
        return isFraud;
    }

    public long getStep() {
        return step;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
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
}
