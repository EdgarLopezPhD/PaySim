package org.paysim.paysim.actors;

import org.paysim.paysim.PaySim;
import org.paysim.paysim.base.Transaction;

public class Mule extends Client {
    private static final String MULE_IDENTIFIER = "C";

    public Mule(String name, Bank bank) {
        super(MULE_IDENTIFIER + name, bank);
        this.overdraftLimit = 0;
    }

    void fraudulentCashOut(PaySim paysim, int step, double amount) {
        String action = "CASH_OUT";

        Merchant merchantTo = paysim.pickRandomMerchant();
        String nameOrig = this.getName();
        String nameDest = merchantTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(step, action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);
        t.setFraud(this.isFraud());
        paysim.getTransactions().add(t);
    }
}
