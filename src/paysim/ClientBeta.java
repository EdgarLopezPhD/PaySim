package paysim;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;

public class ClientBeta extends SuperClient implements Steppable{

	private String name = "";
	private boolean willRepeat = false;
	boolean failed = false;
	private double[] probabilityArr;
	private ArrayList<ActionProbability> probList;
	private ArrayList<Integer> stepsToRepeat = new ArrayList<>();
	private int nrOfStepsToRepeat = 0;
	private ArrayList<String> paramFile = new ArrayList<>();
	String currType = "";
	private CurrentStepHandler stepHandler = null;
	private double x;
	private double y;

	public double getDiff(ClientBeta c){
		double xFactor = c.getX() - this.x;
		xFactor = xFactor * xFactor;
		double yFactor = c.getY() - this.y;
		yFactor = yFactor * yFactor;
		
		double result = xFactor + yFactor;
		result = Math.sqrt(result);
		return result;
	}

	public void move(PaySim paysim){
		int randNrX = 0;
		int randNrY = 0;
		
		
		do {
			randNrX = paysim.random.nextInt()%5;
		} while (randNrX == 0);
		
		do {
			randNrY = paysim.random.nextInt()%5;
		} while (randNrY == 0);
		
		System.out.println("RandedX:\t" + randNrX + "\tRandedY:\t" + randNrY + "\n");
		
		this.x += randNrX;
		this.y += randNrY;
		
		if(this.x > 1000){
			this.x = 1000;
		}else if(this.x < 0){
			this.x = 0;
		}
		
		if(this.y > 1000){
			this.y = 1000;
		}else if(this.y < 0){
			this.y = 0;
		}
		
		System.out.println("X:\t" + this.x + "\tY:\t" + this.y + "\n");
		
	}

	@Override
	public void step(SimState state) {
		//System.out.println(this.name + "\tBegun\n");
		handleAction(state);
		
		//If the action is to be repeated, then make the repetitions
		if(this.cont != null){		
			handleRepetition(state);
		}

		//System.out.println(this.name + "\tFinished\n");
	}
	
	public int getNrOfStepsToRepeat() {
		return nrOfStepsToRepeat;
	}

	public void setNrOfStepsToRepeat(int nrOfStepsToRepeat) {
		this.nrOfStepsToRepeat = nrOfStepsToRepeat;
	}

	public void handleAction(SimState state){
		PaySim paysim = (PaySim)state;
		//Must stored in properties
		int action = 0;
		
		//Based on the calculated probablity, an action is chosen
		do{
			action = this.chooseAction(paysim, this.probabilityArr);
			if(action == -1){
				if(this.probabilityArr.length == 0){
					return;
				}
			}
		}while(action == -1);
		
		
		switch (action) {

		// CASH_IN
		case 1:
			handleCashIn(paysim);
			break;

		// CASH_OUT
		case 2:
			handleCashOut(paysim);
			break;
			
		// DEBIT
		case 3:
			handleDebit(paysim);
			break;

		// DEPOSIT
		case 4:
			handleDeposit(paysim);
			break;
			
		// PAYMENT
		case 5:
			handlePayment(paysim);
			break;
		
		//TRANSFER
		case 6:
			handleTransfer(paysim);
			break;
			
		}
	}
	
	public void handleRepetition(SimState state){
		PaySim paysim = (PaySim)state;
		
		
		//Based on the nr of times to repeat, make the repetition
		for(int i=0; i<this.stepsToRepeat.size(); i++){
			//System.out.println("Type:\t" + cont.getType() + "\n");
			int currentStep = this.stepsToRepeat.get(i);
			this.currDay = (int)(currentStep/24) + 1;
			this.currHour = (int)  (currentStep - ((this.currDay - 1) * 24));

			switch (this.cont.getType()) {
			// CASH_IN
			case "CASH_IN":
				this.currType = "CASH_IN";
				handleCashInRepetition(paysim);
				break;

			// CASH_OUT
			case "CASH_OUT":
				this.currType = "CASH_OUT";
				handleCashOutRepetition(paysim);
				break;

			// DEBIT
			case "DEBIT":
				this.currType = "DEBIT";
				handleDebitRepetition(paysim);
				break;

			// DEPOSIT
			case "DEPOSIT":
				this.currType = "DEPOSIT";
				handleDepositRepetition(paysim);
				break;

			// PAYMENT
			case "PAYMENT":
				this.currType = "PAYMENT";
				handlePaymentRepetition(paysim);
				break;

			// TRANSFER
			case "TRANSFER":
				this.currType = "TRANSFER";
				handleTrasferRepetition(paysim);
				break;

			}
		}
		
	}
	
	//Setters/getters
	
	public void setClientBeta(ClientBeta c){
		this.balance = c.getBalance();
		this.currStep = c.getCurrStep();
		this.name = c.getName();
		this.numDeposits = c.getNumDeposits();
		this.numTransfers = c.getNumTransfers();
		this.numWithdraws = c.getNumWithdraws();
	}
	
	public double[] getProbabilityArr() {
		return probabilityArr;
	}

	public void setProbabilityArr(double[] probabilityArr) {
		this.probabilityArr = probabilityArr;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	@Override
	public String toString(){
		String totString = super.toString();
		totString += "\t" + this.name;
		return this.name;
	}
	
//	* 1:	CASH_IN	***
//	 * 2:	CASH_OUT ***
//	 * 3:	DEBIT
//	 * 4:	DEPOSIT ***
//	 * 5:	PAYMENT ***
//	 * 6:	TRANSFER
	
	//Handler functions for action
	
	public void handleCashIn(PaySim paysim){
		//Get the probabilities that correspond to that current day
		ActionProbability prob = getProb("CASH_IN", paysim);
		
		//With the probability for that day gained, get the next random number in that distribution
		if(prob != null){
			double amount = this.getAmount(prob, paysim);
			ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
			ClientBeta ClientBetaToTransferBefore = new ClientBeta();
			ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
			
			ClientBeta before = new ClientBeta();
			before.setClientBeta(this);
			ClientBetaToTransferAfter.withdraw(amount);
			this.deposit(amount);
			
			Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 1, amount, "CashIn");
			t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
			t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
			t2.setDay(this.currDay);
			t2.setHour(this.currHour);
			paysim.getTrans().add(t2);
//			System.out.println("Lenght of prob arr CORRECT:\t" + this.probabilityArr.length + "\n");
//			printActionProbability();
		}else{
			Manager.nrFailed++;
//			System.out.println("Lenght of prob arr NOT-CORRECT:\t" + this.probabilityArr.length + "\n");
//			Manager.nrFailed++;
//			System.out.println("Curr Prob Type:\tCASH_OUT" + "\n"
//					+ "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
//					);
			printActionProbability();
		}
		
	}
	
	public void handleCashOut(PaySim paysim){
		//Get the probabilities that correspond to that current day
		ActionProbability prob = getProb("CASH_OUT", paysim);
		
		//With the probability for that day gained, get the next random number in that distribution
		if(prob != null){
			double amount = this.getAmount(prob, paysim);
			
			ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
			ClientBeta ClientBetaToTransferBefore = new ClientBeta();
			ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
			
			ClientBeta before = new ClientBeta();
			before.setClientBeta(this);
			this.withdraw(amount);
			ClientBetaToTransferAfter.deposit(amount);
			
			Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 2, amount, "CashOut");
			t2.setDay(this.currDay);
			t2.setHour(this.currHour);
			t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
			t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
			paysim.getTrans().add(t2);
//			System.out.println("Lenght of prob arr CORRECT:\t" + this.probabilityArr.length + "\n");
//			printActionProbability();
		}else{
//			System.out.println("Lenght of prob arr NOT-CORRECT:\t" + this.probabilityArr.length + "\n");
//			Manager.nrFailed++;
//			System.out.println("Curr Prob Type:\tCASH_OUT" + "\n"
//					+ "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
//					);
			printActionProbability();
		}		
		
	}
	
	public void handleDeposit(PaySim paysim){
		//Get the probabilities that correspond to that current day
//		System.out.println("Dumping..\n\n");
		
//		for(ActionProbability temp: paysim.getaProbList()){
//			System.out.println(temp.toString() + "\n");
//		}
		ActionProbability prob = getProb("DEPOSIT", paysim);
		
		if(prob != null){
			//With the probability for that day gained, get the next random number in that distribution
			double amount = this.getAmount(prob, paysim);
			
			//Store the ClientBeta before the deposit to keep track of the previous balance
			ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
			ClientBeta ClientBetaToTransferBefore = new ClientBeta();
			ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
			
			ClientBeta before = new ClientBeta();
			before.setClientBeta(this);
			ClientBetaToTransferAfter.withdraw(amount);
			this.deposit(amount);
			this.numDeposits++;
			
			Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 4, amount, "Deposit");
			t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
			t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
			t2.setDay(this.currDay);
			t2.setHour(this.currHour);
			paysim.getTrans().add(t2);
//			System.out.println("Lenght of prob arr CORRECT:\t" + this.probabilityArr.length + "\n");
//			printActionProbability();
		}else{
//			System.out.println("Lenght of prob arr NOT-CORRECT:\t" + this.probabilityArr.length + "\n");
//			Manager.nrFailed++;
//			System.out.println("Curr Prob Type:\tDEPOSIT" + "\n"
//					+ "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
//					);
			printActionProbability();
		}
		
	}
		
	public void handlePayment(PaySim paysim){

		
		Merchant mAfter = getRandomMerchant(paysim);
		Merchant merchantToTransferBefore = new Merchant();
		merchantToTransferBefore.setMerchant(mAfter);
		ActionProbability prob = getProb("PAYMENT", paysim);
		
		if(prob != null){
			double amount = this.getAmount(prob, paysim);
			
			ClientBeta before = new ClientBeta();
			before.setClientBeta(this);
			this.withdraw(amount);
			mAfter.deposit(amount);
			
			Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this , 5, amount, "Payment");
			t2.setMerchantAfter(mAfter);
			t2.setMerchantBefore(merchantToTransferBefore);
			t2.setDay(this.currDay);
			t2.setHour(this.currHour);
			paysim.getTrans().add(t2);
//			System.out.println("Lenght of prob arr CORRECT:\t" + this.probabilityArr.length + "\n");
//			printActionProbability();
		}else{
//			System.out.println("Lenght of prob arr NOT-CORRECT:\t" + this.probabilityArr.length + "\n");
//			Manager.nrFailed++;
//			System.out.println("Curr Prob Type:\tPAYMENT" + "\n"
//					+ "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
//					);
			printActionProbability();
		}
		
	}
		
	public void handleTransfer(PaySim paysim){
		//Get the probabilities that correspond to that current day
		ActionProbability prob = getProb("TRANSFER", paysim);
		
		//With the probability for that day gained, get the next random number in that distribution
		if(prob != null){
			double amount = this.getAmount(prob, paysim);
			ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
			ClientBeta ClientBetaToTransferBefore = new ClientBeta();
			ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
			
			ClientBeta before = new ClientBeta();
			before.setClientBeta(this);
			this.withdraw(amount);
			ClientBetaToTransferAfter.deposit(amount);
			
			Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 6, amount, "Transfer");
			t2.setDay(this.currDay);
			t2.setHour(this.currHour);
			t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
			t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
			paysim.getTrans().add(t2);
//			System.out.println("Lenght of prob arr CORRECT:\t" + this.probabilityArr.length + "\n");
//			printActionProbability();
		}else{
//			System.out.println("Lenght of prob arr NOT-CORRECT:\t" + this.probabilityArr.length + "\n");
//			Manager.nrFailed++;
//			System.out.println("Curr Prob Type:\tTRANSFER" + "\n"
//					+ "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
//					);
			printActionProbability();
		}
		
	}
	
	public void handleDebit(PaySim paysim){
		//Get the probabilities that correspond to that current day
		ActionProbability prob = getProb("DEBIT", paysim);
		
		//With the probability for that day gained, get the next random number in that distribution
		if(prob != null){
			double amount = this.getAmount(prob, paysim);
			ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
			ClientBeta ClientBetaToTransferBefore = new ClientBeta();
			ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
			
			ClientBeta before = new ClientBeta();
			before.setClientBeta(this);
			this.withdraw(amount);
			ClientBetaToTransferAfter.deposit(amount);
			
			Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 3, amount, "Debit");
			t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
			t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
			t2.setDay(this.currDay);
			t2.setHour(this.currHour);
			paysim.getTrans().add(t2);
//			System.out.println("Lenght of prob arr CORRECT:\t" + this.probabilityArr.length + "\n");
//			printActionProbability();
		}else{
//			System.out.println("Lenght of prob arr NOT-CORRECT:\t" + this.probabilityArr.length + "\n");
//			Manager.nrFailed++;
//			System.out.println("Curr Prob Type:\tDEBIT" + "\n"
//					+ "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
//					);
			printActionProbability();
		}
		
	}
		
		
	
	//Handler functions for repetition
	
	public void handleCashInRepetition(PaySim paysim){		
		double amount = this.getAmountRepetition(this.currType, this.currDay, this.currHour, paysim);
		if(amount == -1){
			return;
		}
		
		ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
		ClientBeta ClientBetaToTransferBefore = new ClientBeta();
		ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
		
		ClientBeta before = new ClientBeta();
		before.setClientBeta(this);
		ClientBetaToTransferAfter.withdraw(amount);
		this.deposit(amount);
		
		Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 1, amount, "CashIn");
		t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
		t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
		t2.setDay(this.currDay);
		t2.setHour(this.currHour);
		paysim.getTrans().add(t2);
	
	}
	
	public void handleCashOutRepetition(PaySim paysim){
		double amount = this.getAmountRepetition(this.currType, this.currDay, this.currHour, paysim);
		if(amount == -1){
			return;
		}
		
		ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
		ClientBeta ClientBetaToTransferBefore = new ClientBeta();
		ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
		
		ClientBeta before = new ClientBeta();
		before.setClientBeta(this);
		this.withdraw(amount);
		ClientBetaToTransferAfter.deposit(amount);
		
		Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 2, amount, "CashOut");
		t2.setDay(this.currDay);
		t2.setHour(this.currHour);
		t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
		t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
		paysim.getTrans().add(t2);
	}
	
	public void handleDebitRepetition(PaySim paysim){
		double amount = this.getAmountRepetition(this.currType, this.currDay, this.currHour, paysim);
		if(amount == -1){
			return;
		}
		
		ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
		ClientBeta ClientBetaToTransferBefore = new ClientBeta();
		ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
		
		ClientBeta before = new ClientBeta();
		before.setClientBeta(this);
		this.withdraw(amount);
		ClientBetaToTransferAfter.deposit(amount);
		
		Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 3, amount, "Debit");
		t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
		t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
		t2.setDay(this.currDay);
		t2.setHour(this.currHour);
		paysim.getTrans().add(t2);
	}
	
	public void handleDepositRepetition(PaySim paysim){
		double amount = this.getAmountRepetition(this.currType, this.currDay, this.currHour, paysim);
		if(amount == -1){
			return;
		}
		
		//Store the ClientBeta before the deposit to keep track of the previous balance
		ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
		ClientBeta ClientBetaToTransferBefore = new ClientBeta();
		ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
		
		ClientBeta before = new ClientBeta();
		before.setClientBeta(this);
		ClientBetaToTransferAfter.withdraw(amount);
		this.deposit(amount);
		this.numDeposits++;
		
		Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 4, amount, "Deposit");
		t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
		t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
		t2.setDay(this.currDay);
		t2.setHour(this.currHour);
		paysim.getTrans().add(t2);
	}

	public void handlePaymentRepetition(PaySim paysim){

		Merchant mAfter = getRandomMerchant(paysim);
		Merchant merchantToTransferBefore = new Merchant();
		merchantToTransferBefore.setMerchant(mAfter);

		double amount = this.getAmountRepetition(this.currType, this.currDay, this.currHour, paysim);
		if(amount == -1){
			return;
		}
		
		ClientBeta before = new ClientBeta();
		before.setClientBeta(this);
		this.withdraw(amount);
		mAfter.deposit(amount);
		
		Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this , 5, amount, "Payment");
		t2.setMerchantAfter(mAfter);
		t2.setMerchantBefore(merchantToTransferBefore);
		t2.setDay(this.currDay);
		t2.setHour(this.currHour);
		paysim.getTrans().add(t2);
	}
	
	public void handleTrasferRepetition(PaySim paysim){
		double amount = this.getAmountRepetition(this.currType, this.currDay, this.currHour, paysim);
		if(amount == -1){
			return;
		}
		try {
			ClientBeta ClientBetaToTransferAfter = getRandomClientBeta(amount, paysim);
			ClientBeta ClientBetaToTransferBefore = new ClientBeta();
			ClientBetaToTransferBefore.setClientBeta(ClientBetaToTransferAfter);
			
			ClientBeta before = new ClientBeta();
			before.setClientBeta(this);
			this.withdraw(amount);
			ClientBetaToTransferAfter.deposit(amount);
			
			Transaction t2 = new Transaction(paysim.schedule.getSteps(), before, this, 6, amount, "Transfer");
			t2.setDay(this.currDay);
			t2.setHour(this.currHour);
			t2.setClientBetaDestAfter(ClientBetaToTransferAfter);
			t2.setClientBetaDestBefore(ClientBetaToTransferBefore);
			paysim.getTrans().add(t2);
		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println("returned\n");
			return;
		}
		
		
		
	}
	
	
	
	
	
	
	
	
	public ArrayList<ActionProbability> getProbList() {
		return probList;
	}

	public boolean isWillRepeat() {
		return willRepeat;
	}

	public ClientBeta getRandomClientBeta(double amount, PaySim paysim){
		ClientBeta ClientBetaToTransfer = new ClientBeta();
		int counter = 0;
		do{
			ClientBetaToTransfer = paysim.getClientsBeta().get(paysim.random.nextInt(paysim.getClientsBeta().size()));
			counter ++;
			if(counter > 50000){
				break;
			}
		}while(ClientBetaToTransfer.getBalance() < amount);
		
		
		return ClientBetaToTransfer;
	}

	public Merchant getRandomMerchant(PaySim paysim){
		Merchant m = paysim.getMerchants().get(paysim.random.nextInt(paysim.getMerchants().size()));
		
		return m;
	}
	
	
	public void setWillRepeat(boolean willRepeat) {
		this.willRepeat = willRepeat;
	}

	public void setProbList(ArrayList<ActionProbability> probList) {
		this.probList = probList;
	}


	private double getAmount(ActionProbability prob, PaySim paysim){
		double amount = 0;
		
		do{
			amount = paysim.random.nextGaussian() * prob.getStd() + prob.getAverage();
		}while(amount <= 0);
		
		return amount;
	}
	
	private double getAmountRepetition(String type, int day, int hour, PaySim paysim){
		AggregateTransactionRecord transRecord = this.stepHandler.getRecord(type, day, hour);
		if(transRecord == null){
			return -1;
		}
		double amount = 0;		
		do{
			amount = paysim.random.nextGaussian() * Double.parseDouble(transRecord.gettStd()) + Double.parseDouble(transRecord.gettAvg());
		}while(amount <= 0);
		
		return amount;
	}
	
	public ArrayList<Integer> getStepsToRepeat() {
		return stepsToRepeat;
	}

	public void setStepsToRepeat(ArrayList<Integer> stepsToRepeat) {
		this.stepsToRepeat = stepsToRepeat;
	}
	
	private ActionProbability getProb(String probToGet, PaySim p){
			
		for(ActionProbability temp: this.probList){
			if(temp.getType().equals(probToGet)){
				return temp;
			}
		}
		return null;
	}
	
	private void printActionProbability(){
		System.out.println("Printing prob\n");
		for(ActionProbability p: this.probList){
			System.out.println(p.getType() + "\n" + p.getNrOfTransactions() + "\n");
		}
		System.out.println("\n\n");
	}

	public ArrayList<String> getParamFile() {
		return paramFile;
	}

	public void setParamFile(ArrayList<String> paramFile) {
		this.paramFile = paramFile;
	}


	public CurrentStepHandler getStepHandler() {
		return stepHandler;
	}

	public void setStepHandler(CurrentStepHandler stepHandler) {
		this.stepHandler = stepHandler;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	
	

}
