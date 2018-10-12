package paysim.actors;

import paysim.PaySim;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Fraudster implements Steppable {
	public double profit = 0;
	public double clientsAffected = 0;

	@Override
	public void step(SimState state) {
		PaySim paysim = (PaySim) state;
		double randNr = paysim.random.nextDouble();
		if (randNr < paysim.getFraudProbability()
				&& paysim.schedule.getSteps() > 0) {
			Client c = paysim.getRandomClient();
			if (paysim.debugFlag) {
				System.out.println(this.getName() + " Do Fraud on client "
						+ c.toString() + " " + c.getBalance());
			}
			c.setFraud(true);
			double balance = c.getBalance();
			// create mule client
			if (balance > 0) {
				int loops = (int) Math.ceil(balance / paysim.transferLimit);
				for (int i = 0; i < loops; i++) {
					Client muleClient = new Client();
					muleClient.setFraud(true);
					if (balance > paysim.transferLimit) {
						c.handleTransfer(paysim, muleClient,
								paysim.transferLimit);
						balance -= paysim.transferLimit;
					} else {
						c.handleTransfer(paysim, muleClient, balance);
						balance = 0;
					}

					profit += muleClient.getBalance();
					muleClient.handleCashOut(paysim, paysim.getRandomClient(),
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
		return this.toString();
	}

	public String toString() {
		return "F" + Integer.toString(this.hashCode());
	}
}
