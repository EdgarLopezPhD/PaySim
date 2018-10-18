package paysim.actors;

import paysim.PaySim;
import paysim.base.Repetition;

public class SuperActor {
    private final String name;
    double balance = 0;
    int step = 0;
    private boolean isFraud = false;
    Repetition cont = null;

    SuperActor(String name){
        this.name = name;
    }

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

    public void setStep(int step) {
        this.step = step;
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

    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        return name;
    }
}
