package paysim.actors;

public class SuperActor {
    private final String name;
    double balance = 0;
    private boolean isFraud = false;

    SuperActor(String name) {
        this.name = name;
    }

    void deposit(double amount) {
        balance += amount;
    }

    void withdraw(double amount) {
        if (balance < amount) {
            balance = 0;
        } else {
            balance -= amount;
        }
    }

    boolean isFraud() {
        return isFraud;
    }

    void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
    }

    double getBalance() {
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
