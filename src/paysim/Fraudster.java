package paysim;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;

public class Fraudster implements Steppable {
	double profit = 0;
	double clientsAffected = 0;

	@Override
	public void step(SimState state) {
		PaySim paysim = (PaySim) state;
		// TODO Auto-generated method stub
		Bag neighbors = null;
		int numCardsStolen = 0;
		Double2D loc = paysim.land.getObjectLocation(this);
		double randNr = paysim.random.nextDouble();
		if (randNr < paysim.getFraudProbability()
				&& paysim.schedule.getSteps() > 0) {
			Client c = paysim.getRandomClient();
			if (paysim.debugFlag) {
				System.out.println(this.getName() + " Do Fraud on client "
						+ c.toString() + " " + c.getBalance());
			}
			c.setVictim(true);
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
