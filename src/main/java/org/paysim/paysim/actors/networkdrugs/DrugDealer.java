package org.paysim.paysim.actors.networkdrugs;

import sim.engine.SimState;

import org.paysim.paysim.PaySim;
import org.paysim.paysim.actors.Client;

public class DrugDealer extends Client {
    private double thresholdForCashOut;
    private double drugMoneyInAccount;

    public DrugDealer(PaySim paySim, double thresholdForCashOut) {
        super(paySim);
        this.thresholdForCashOut = thresholdForCashOut;
        this.drugMoneyInAccount = 0;
    }

    @Override
    public void step(SimState state) {
        PaySim paySim = (PaySim) state;
        int step = (int) paySim.schedule.getSteps();

        super.step(state);

        if (wantsToCashOutProfit()) {
            double amount = pickAmountCashOutProfit();
            super.handleCashOut(paySim, step, amount);
            drugMoneyInAccount -= amount;
        }
    }

    private boolean wantsToCashOutProfit(){
        //TODO: implement a randomized version
        return drugMoneyInAccount > thresholdForCashOut;
    }

    private double pickAmountCashOutProfit(){
        //TODO: implement a randomized version
        return thresholdForCashOut;
    }

    protected void addMoneyFromDrug(double amount){
        drugMoneyInAccount += amount;
    }
}
