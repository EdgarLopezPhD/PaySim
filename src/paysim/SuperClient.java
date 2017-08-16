package paysim;

import java.util.ArrayList;

import sim.engine.SimState;

public class SuperClient {

	String currency = "SEK";
	int numDeposits = 0;
	int numWithdraws = 0;
	int numTransfers = 0;
	int numDebit = 0;
	int numPayment = 0;
	int numCashIn = 0;
	int numCashOut = 0;
	int currDay = 0;
	int currHour = 0;
	double balance = 0;
	int currStep = 0;
	private boolean isEnable = true;
	private boolean isFraud = false;
	private boolean isVictim = false;
	
	public boolean isEnable() {
		return isVictim;
	}

	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	public boolean isVictim() {
		return isVictim;
	}

	public void setVictim(boolean isVictim) {
		this.isVictim = isVictim;
	}

	RepetitionContainer cont = null;
	TransferMaxHandler transferMaxHandler;
	
	public boolean isFraud() {
		return isFraud;
	}

	public void setFraud(boolean isFraud) {
		this.isFraud = isFraud;
	}

	public String getRow(String type, int day, int hour, ArrayList<String> fileContents){
		String row = null;
		
		for(String s: fileContents){
			String splitted[] = s.split(",");
			if(splitted[0].equals(type) &&
					splitted[2].equals(String.valueOf(day)) &&
					splitted[3].equals(String.valueOf(hour)))
			{
				row = s;
				break;
			}
		}
		
		return row;
		
	}

	public TransferMaxHandler getTransferMaxHandler() {
		return transferMaxHandler;
	}

	public void setTransferMaxHandler(TransferMaxHandler transferMaxHandler) {
		this.transferMaxHandler = transferMaxHandler;
	}

	public RepetitionContainer getCont() {
		return cont;
	}

	public void setCont(RepetitionContainer cont) {
		this.cont = cont;
	}

	public int getCurrDay() {
		return currDay;
	}

	public void setCurrDay(int currDay) {
		this.currDay = currDay;
	}

	public int getCurrHour() {
		return currHour;
	}

	public void setCurrHour(int currHour) {
		this.currHour = currHour;
	}

	
	
	public static void main(String args[]){
//		SuperClient c = new Client();
//		PaySim sim = new PaySim(1156145);
//		
//		for(int i=0; i<200; i++){
//			System.out.println(c.chooseAction(sim) + "\n");
//		}
	}
	
	public int chooseAction(PaySim paysim, double probArr[]){

		/*
		 * 1:	CASH_IN
		 * 2:	CASH_OUT
		 * 3:	DEBIT
		 * 4:	DEPOSIT
		 * 5:	PAYMENT
		 * 6:	TRANSFER
		 */
		
		double randNr = paysim.random.nextDouble();
		double total = 0;
		
		for(int i=0; i<probArr.length; i++){
			double currProb = probArr[i];
			
			if(randNr >= total && 
					randNr <= (total + currProb)){
				return i + 1;			
			}else{
				total += currProb;
			}			
		}
		return -1;
		
	}

	
	public void deposit(double ammount){
		this.balance += ammount;
	}
	
	public void withdraw(double ammount){
		if(this.balance < ammount){
			this.balance = 0;
		}else{
			this.balance -= ammount;	
		}
	}
	
	public void transfer(Client cOne, Client cTwo, double amount){
		cOne.withdraw(amount);
		cTwo.deposit(amount);
	}
	
	public void incrementStep(){
		this.currStep++;
	}
	
	
	
	//Setters and getters
	
	public int getCurrStep(){
		return this.currStep;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public void setCurrency(String currency) {
			this.currency = currency;
		}
		
	public int getNumDeposits() {
			return numDeposits;
		}
		
	public void setNumDeposits(int numDeposits) {
			this.numDeposits = numDeposits;
		}
		
	public int getNumWithdraws() {
			return numWithdraws;
		}
		
	public void setNumWithdraws(int numWithdraws) {
			this.numWithdraws = numWithdraws;
		}
		
	public int getNumTransfers() {
			return numTransfers;
		}
		
	public void setNumTransfers(int numTransfers) {
			this.numTransfers = numTransfers;
		}
		
	public double getBalance() {
			return balance;
		}
		
	public void setBalance(double balance) {
			this.balance = balance;
		}

	public void step(SimState state) {
		// TODO Auto-generated method stub
		
	}
	
	public int getNumDebit() {
		return numDebit;
	}

	public void setNumDebit(int numDebit) {
		this.numDebit = numDebit;
	}

	public int getNumPayment() {
		return numPayment;
	}

	public void setNumPayment(int numPayment) {
		this.numPayment = numPayment;
	}

	public int getNumCashIn() {
		return numCashIn;
	}

	public void setNumCashIn(int numCashIn) {
		this.numCashIn = numCashIn;
	}

	public int getNumCashOut() {
		return numCashOut;
	}

	public void setNumCashOut(int numCashOut) {
		this.numCashOut = numCashOut;
	}
	
	
		
		
}
