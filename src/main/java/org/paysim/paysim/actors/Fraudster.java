package org.paysim.paysim.actors;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;

import org.paysim.paysim.PaySim;
import org.paysim.paysim.parameters.Parameters;

import org.paysim.paysim.output.Output;

public class Fraudster extends SuperActor implements Steppable {
    private static final String FRAUDSTER_IDENTIFIER = "C";
    private double profit = 0;
    private int nbVictims = 0;

    public Fraudster(String name) {
        super(FRAUDSTER_IDENTIFIER + name);
    }

    @Override
    public void step(SimState state) {
        PaySim paysim = (PaySim) state;
        int step = (int) state.schedule.getSteps();
        if (paysim.random.nextDouble() < Parameters.fraudProbability) {
            Client c = paysim.pickRandomClient(getName());
            c.setFraud(true);
            double balance = c.getBalance();
            // create mule client
            if (balance > 0) {
                int nbTransactions = (int) Math.ceil(balance / Parameters.transferLimit);
                for (int i = 0; i < nbTransactions; i++) {
                    boolean transferFailed;
                    Mule muleClient = new Mule(paysim.generateId(), paysim.pickRandomBank());
                    muleClient.setFraud(true);
                    if (balance > Parameters.transferLimit) {
                        transferFailed = !c.handleTransfer(paysim, step, Parameters.transferLimit, muleClient);
                        balance -= Parameters.transferLimit;
                    } else {
                        transferFailed = !c.handleTransfer(paysim, step, balance, muleClient);
                        balance = 0;
                    }

                    profit += muleClient.getBalance();
                    muleClient.fraudulentCashOut(paysim, step, muleClient.getBalance());
                    nbVictims++;
                    paysim.addClient(muleClient);
                    if (transferFailed)
                        break;
                }
            }
            c.setFraud(false);
        }
    }

    @Override
    public String toString() {
        ArrayList<String> properties = new ArrayList<>();

        properties.add(getName());
        properties.add(Integer.toString(nbVictims));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, profit));

        return String.join(Output.OUTPUT_SEPARATOR, properties);
    }
}
