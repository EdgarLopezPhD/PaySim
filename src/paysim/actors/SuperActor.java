package paysim.actors;

class SuperActor {
    private final String name;
    private boolean isFraud = false;
    double balance = 0;

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
