package paysim.actors;

import paysim.PaySim;
import paysim.parameters.Parameters;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Fraudster extends SuperActor implements Steppable {
    private static final String FRAUDSTER_IDENTIFIER = "C";
    private double profit = 0;
    private int clientsAffected = 0;

    public Fraudster(String name) {
        super(FRAUDSTER_IDENTIFIER + name);
    }

    @Override
    public void step(SimState state) {
        PaySim paysim = (PaySim) state;
        double randNr = paysim.random.nextDouble();
        if (randNr < Parameters.fraudProbability
                && paysim.schedule.getSteps() > 0) {
            Client c = paysim.getRandomClient();
            if (c == null) {
                System.out.println("Fraudster tried to act but where was no client, skipping.");
                return;
            }
            c.setFraud(true);
            double balance = c.getBalance();
            // create mule client
            if (balance > 0) {
                int loops = (int) Math.ceil(balance / Parameters.transferLimit);
                for (int i = 0; i < loops; i++) {
                    Mule muleClient = new Mule(paysim.generateIdentifier());
                    muleClient.setFraud(true);
                    if (balance > Parameters.transferLimit) {
                        c.handleTransfer(paysim, muleClient, Parameters.transferLimit);
                        balance -= Parameters.transferLimit;
                    } else {
                        c.handleTransfer(paysim, muleClient, balance);
                        balance = 0;
                    }

                    profit += muleClient.getBalance();
                    muleClient.fraudulentCashOut(paysim, muleClient.getBalance());
                    clientsAffected++;
                    paysim.getClients().add(muleClient);
                    if (c.getBalance() <= 0)
                        break;
                }
            }
        }
    }

    public double getProfit() {
        return profit;
    }

    public int getClientsAffected() {
        return clientsAffected;
    }
}
