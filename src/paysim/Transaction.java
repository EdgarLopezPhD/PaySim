package paysim;

import java.io.Serializable;
import java.util.Arrays;

public class Transaction implements Serializable {
    public enum TransactionType {
        CASH_IN(1, "CASH_IN"), CASH_OUT(2, "CASH_OUT"), DEBIT(3, "DEBIT"),
        DEPOSIT(4, "DEPOSIT"), PAYMENT(5, "PAYMENT"), TRANSFER(6, "TRANSFER");

        private final int id;
        private final String name;

        TransactionType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getValue() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static String nameOf(int id) {
            return Arrays.stream(values())
                    .filter(t -> t.id == id)
                    .findFirst()
                    .map(TransactionType::getName)
                    .orElse(null);
        }

        public static boolean isValid(String name){
            return Arrays.stream(values())
                    .map(TransactionType::getName)
                    .anyMatch(n -> n.equals(name));
        }
    }
	private static final long serialVersionUID = 1L;
	int type;
	double amount;
	String description;
	Client clientOrigBefore = new Client();
	Client clientOrigAfter = new Client();
	Client clientDestBefore = new Client();
	Client clientDestAfter = new Client();
	boolean isFraud = false;
	boolean isFlaggedFraud = false;
	
	public boolean isFlaggedFraud() {
		return isFlaggedFraud;
	}

	public void setFlaggedFraud(boolean isFlaggedFraud) {
		this.isFlaggedFraud = isFlaggedFraud;
	}

	public boolean isFraud() {
		return isFraud;
	}

	Merchant merchantBefore = new Merchant();
	public Merchant getMerchantBefore() {
		return merchantBefore;
	}

	public void setMerchantBefore(Merchant merchantBefore) {
		this.merchantBefore = merchantBefore;
	}

	public Merchant getMerchantAfter() {
		return merchantAfter;
	}

	public void setMerchantAfter(Merchant merchantAfter) {
		this.merchantAfter = merchantAfter;
	}

	Merchant merchantAfter = new Merchant();
	int fraudster = 0;
	double newBalanceDest = 0;
	double newBalanceOrig = 0;
	long step;
	String profileOrig, profileDest;
	int day = 0;
	int hour = 0;

	public int getFraudster() {
		return fraudster;
	}

	public void setFraudster(int fraudster) {
		this.fraudster = fraudster;
	}

	public long getStep() {
		return step;
	}

	public void setStep(long step) {
		this.step = step;
	}

	public Transaction() {
		this.type = 0;
		this.amount = 0;
		this.newBalanceDest = 0;
		this.newBalanceOrig = 0;
	}

//	public Transaction(Long step, Client clientOrig, Client clientDest,
//			int type, double amount, String description) {
//		super();
//		this.step = step;
//		this.clientOrig = clientOrig;
//		this.newBalanceOrig = clientOrig.balance;
//		this.profileOrig = clientOrig.profile.toString();
//		this.clientDest = clientDest;
//		this.newBalanceDest = clientDest.balance;
//		this.profileDest = clientDest.profile.toString();
//		this.type = type;
//		this.amount = amount;
//		this.description = description;
//	}
//
	
	//The constructor used in my agent
	public Transaction(Long step, Client clientOrig, int type, double amount,
			String description) {
		super();
		this.step = step;
		this.clientOrigBefore.setClient(clientOrig);;
		this.newBalanceOrig = clientOrig.balance;
		this.type = type;
		this.amount = amount;
		this.description = description;
	}
	
	//Used for transfer
	public Transaction(Long step, Client clientOriginalBefore, Client clientOrigAfter, int type, double amount,
			String description) {
		super();
		this.step = step;
		this.clientOrigBefore.setClient(clientOriginalBefore);
		this.clientOrigAfter.setClient(clientOrigAfter);
		
		this.newBalanceOrig = this.clientOrigBefore.getBalance();
		this.newBalanceDest = clientOrigAfter.balance;
		
		this.type = type;
		this.amount = amount;
		this.description = description;
	}

//	public Transaction(Long step, Client clientOrig, Client clientDest,
//			int type, double amount, String description, int fraudster) {
//		super();
//		this.step = step;
//		this.clientOrig = clientOrig;
//		this.newBalanceOrig = clientOrig.balance;
//		this.profileOrig = clientOrig.profile.toString();
//		this.clientDest = clientDest;
//		this.newBalanceDest = clientDest.balance;
//		this.profileDest = clientDest.profile.toString();
//		this.type = type;
//		this.amount = amount;
//		this.description = description;
//		this.fraudster = fraudster;
//	}
//
//	public Transaction(Long step, Client clientOrig, int type, double amount,
//			String description, int fraudster) {
//		super();
//		this.step = step;
//		this.clientOrig = clientOrig;
//		this.newBalanceOrig = clientOrig.balance;
//		this.profileOrig = clientOrig.profile.toString();
//		this.type = type;
//		this.amount = amount;
//		this.description = description;
//		this.fraudster = fraudster;
//	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getAmount() {
		return amount;
	}

	public Client getClientOrigBefore() {
		return clientOrigBefore;
	}

	public Client getClientOrigAfter() {
		return clientOrigAfter;
	}

	public Client getClientDestBefore() {
		return clientDestBefore;
	}

	public void setClientDestBefore(Client clientDestBefore) {
		this.clientDestBefore = clientDestBefore;
	}

	public Client getClientDestAfter() {
		return clientDestAfter;
	}

	public void setClientDestAfter(Client clientDestAfter) {
		this.clientDestAfter = clientDestAfter;
	}

	public String toString() {
		String ps = null;

		if(this.newBalanceDest == 0){
			ps = Long.toString(step) + " " + clientOrigBefore.toString() + "\t" +"Amount:\t" + Double.toString(amount)
					+ "\tnew Balance " + Double.toString(newBalanceOrig) + "\t" + "Action: " + this.description +
					"Day:\t" + this.day + "\tHour:\t" + this.hour + "\n";
		}else{
			ps = Long.toString(step) + " " + clientOrigBefore.toString() + "\t" + this.clientOrigBefore.toString() +"(" + this.newBalanceOrig
					 + ") Transfered: " + Double.toString(amount) + " to " + this.clientOrigAfter.toString() + " (" + this.newBalanceDest + ")\t" + 
					 "\tnew Balance " + Double.toString(newBalanceOrig) + "\t" + "Action: " + this.description +
					 "Day:\t" + this.day + "\tHour:\t" + this.hour + "\n";
		}
		
		return ps;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public void setFraud(boolean isFraud) {
		this.isFraud = isFraud;
	}
	
//	public String getRecord() {
//		String ps = null;
//		ps = Long.toString(step) + ",'" + clientOrig + "','" + clientOrig.age + "','"
//				+ profileOrig + "','" + clientOrig.getLocation()
//				+ "'," + Integer.toString(type) + "," + Double.toString(amount)
//				+ "," + Double.toString(newBalanceOrig) + ",'";
//		if (clientDest != null) {
//			ps += clientDest + "','" + profileDest + "'," + "'"
//					+ clientDest.getLocation() + "',"
//					+ Double.toString(newBalanceDest) + ",";
//		} else {
//			ps += "null','null','null',0,";
//		}
//		ps += fraudster + "";
//		return ps;
//	}

}
