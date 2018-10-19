package paysim.actors;

import paysim.PaySim;
import paysim.base.Transaction;

public class Mule extends Client {
    private static final String MULE_IDENTIFIER = "C";

    public Mule(String name) {
        super(MULE_IDENTIFIER + name);
    }

    void fraudulentCashOut(PaySim paysim, double amount) {
        String action = "CASH_OUT";

        Merchant merchantTo = paysim.getRandomMerchant();
        String nameOrig = this.getName();
        String nameDest = merchantTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);
        t.setFraud(this.isFraud());
        paysim.getTransactions().add(t);
    }
}
