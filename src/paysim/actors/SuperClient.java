package paysim.actors;

import paysim.PaySim;
import paysim.base.Repetition;

public class SuperClient {
    private boolean isFraud = false;
    int numDeposits = 0;
    int numWithdraws = 0;
    int numTransfers = 0;
    public double balance = 0;
    int currStep = 0;
    Repetition cont = null;

    public int chooseAction(PaySim paysim, double probArr[]) {
        double randNr = paysim.random.nextDouble();
        double total = 0;

        for (int i = 0; i < probArr.length; i++) {
            double currProb = probArr[i];

            if (total <= randNr && randNr <= (total + currProb)) {
                return i + 1;
            } else {
                total += currProb;
            }
        }
        return -1;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) {
        if (balance < amount) {
            balance = 0;
        } else {
            balance -= amount;
        }
    }

    public void transfer(Client cOne, Client cTwo, double amount) {
        cOne.withdraw(amount);
        cTwo.deposit(amount);
    }

    public void setCurrStep(int currStep) {
        this.currStep = currStep;
    }

    public boolean isFraud() {
        return isFraud;
    }

    public void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
    }

    public void setCont(Repetition cont) {
        this.cont = cont;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
