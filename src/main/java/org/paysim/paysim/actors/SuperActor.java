package org.paysim.paysim.actors;

class SuperActor {
    private final String name;
    private boolean isFraud = false;
    double balance = 0;
    double overdraftLimit;

    SuperActor(String name) {
        this.name = name;
    }

    void deposit(double amount) {
        balance += amount;
    }

    boolean withdraw(double amount) {
        boolean unauthorizedOverdraft = false;

        if (balance - amount < overdraftLimit) {
            unauthorizedOverdraft = true;
        } else {
            balance -= amount;
        }

        return unauthorizedOverdraft;
    }

    boolean isFraud() {
        return isFraud;
    }

    void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
    }

    protected double getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
