package paysim.actors;

import paysim.PaySim;
import paysim.parameters.Parameters;
import sim.engine.SimState;
import sim.engine.Steppable;

import static java.lang.Math.abs;

public class Fraudster implements Steppable {
    private String name = "";
    public double profit = 0, clientsAffected = 0;

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
                    Client muleClient = new Client();
                    muleClient.setFraud(true);
                    if (balance > Parameters.transferLimit) {
                        c.handleTransfer(paysim, muleClient,
                                Parameters.transferLimit);
                        balance -= Parameters.transferLimit;
                    } else {
                        c.handleTransfer(paysim, muleClient, balance);
                        balance = 0;
                    }

                    profit += muleClient.getBalance();
                    muleClient.handleCashOut(paysim, paysim.getRandomMerchant(),
                            muleClient.getBalance());
                    clientsAffected++;
                    paysim.clients.add(muleClient);
                    if (c.getBalance() <= 0)
                        break;
                }
            }
        }

    }

    public String getName() {
        if (this.name.equals(""))
            this.name = this.toString();
        return this.name;
    }

    public String toString() {
        return "F" + Integer.toString(abs(this.hashCode()));
    }
}
