package paysim;

public class Merchant extends SuperClient{

	private double balance;
	String name ="";
	public String getName() {
		return this.toString();
	}

	public void setName(String name) {
		this.name = name;
	}

	private int nrOfSales;
	
	public void setMerchant(Merchant m){
		this.balance = m.getBalance();
		this.nrOfSales = m.getNrOfSales();
		this.name = m.getName();
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getNrOfSales() {
		return nrOfSales;
	}
	
	public String toString() {
		return "M" + Integer.toString(this.hashCode());
	}

	public void setNrOfSales(int nrOfSales) {
		this.nrOfSales = nrOfSales;
	}

}
