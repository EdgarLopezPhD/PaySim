package paysim.actors;

import static java.lang.Math.abs;

public class Merchant extends SuperClient {
    private double balance;
    private String name = "";

    public String getName() {
        if (this.name.equals(""))
            this.name = this.toString();
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMerchant(Merchant m) {
        this.balance = m.getBalance();
        this.name = m.getName();
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String toString() {
        return "M" + Integer.toString(abs(this.hashCode()));
    }
}
