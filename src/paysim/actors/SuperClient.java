package paysim.actors;

import paysim.PaySim;
import paysim.RepetitionContainer;

public class SuperClient {
    private boolean isFraud = false;
    int numDeposits = 0;
    int numWithdraws = 0;
    int numTransfers = 0;
    int currDay = 0;
    int currHour = 0;
    public double balance = 0;
    int currStep = 0;
    RepetitionContainer cont = null;

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

    public boolean isFraud() {
        return isFraud;
    }

    public void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
    }

    public void setCont(RepetitionContainer cont) {
        this.cont = cont;
    }

    public void setCurrDay(int currDay) {
        this.currDay = currDay;
    }

    public void setCurrHour(int currHour) {
        this.currHour = currHour;
    }

    public int getCurrStep() {
        return this.currStep;
    }

    public int getNumDeposits() {
        return numDeposits;
    }

    public int getNumWithdraws() {
        return numWithdraws;
    }

    public int getNumTransfers() {
        return numTransfers;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
