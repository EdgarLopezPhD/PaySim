package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class AggregateDumpAnalyzer {
	
	//The total number of transactions made for each type
	private double totNrOfTransfer = 0;
	private double totNrOfDebit = 0;
	private double totNrOfCashIn = 0;
	private double totNrOfCashOut = 0;
	private double totNrOfDeposit = 0;
	private double totNrOfPayments = 0;
	
	//The avg of the avg for each type
	private double avgAvgTransfer = 0;
	private double avgAvgDebit = 0;
	private double avgAvgCashIn = 0;
	private double avgAvgCashOut = 0;
	private double avgAvgDeposit = 0;
	private double avgAvgPayments = 0;
	
	//The avg of the std for each type
	private double avgStdTransfer = 0;
	private double avgStdDebit = 0;
	private double avgStdCashIn = 0;
	private double avgStdCashOut = 0;
	private double avgStdDeposit = 0;
	private double avgStdPayment = 0;
	
	private ArrayList<String> fileContents = new ArrayList<String>();

	public AggregateDumpAnalyzer(String fileName){
		init(fileName);
	}
	
	private void init(String fileName){
		try {
			File f = new File(fileName);
			FileReader fReader = new FileReader(f);
			BufferedReader reader = new BufferedReader(fReader);
			String line = reader.readLine();
			while((line = reader.readLine()) != null){
				this.fileContents.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void analyze(){
		calculateTotal();
		calculateAvgOfAvg();
		calculateAvgStd();
		
	}
	
	
	
	
	private void calculateTotal(){
		/*
		 * 1:	CASH_IN
		 * 2:	CASH_OUT
		 * 3:	DEBIT
		 * 4:	DEPOSIT
		 * 5:	PAYMENT
		 * 6:	TRANSFER
		 */
		this.totNrOfCashIn = this.getCount(1);
		this.totNrOfCashOut = this.getCount(2);
		this.totNrOfDebit = this.getCount(3);
		this.totNrOfDeposit = this.getCount(4);
		this.totNrOfPayments = this.getCount(5);
		this.totNrOfTransfer = this.getCount(6);
		
		
		
	}
	
	private void calculateAvgOfAvg(){
		/*
		 * 1:	CASH_IN
		 * 2:	CASH_OUT
		 * 3:	DEBIT
		 * 4:	DEPOSIT
		 * 5:	PAYMENT
		 * 6:	TRANSFER
		 */
		
		this.avgAvgCashIn = this.getAvgAvg(1);
		this.avgAvgCashOut = this.getAvgAvg(2);
		this.avgAvgDebit = this.getAvgAvg(3);
		this.avgAvgDeposit = this.getAvgAvg(4);
		this.avgAvgPayments = this.getAvgAvg(5);
		this.avgAvgTransfer = this.getAvgAvg(6);
		
	}
		
	private void calculateAvgStd(){
		/*
		 * 1:	CASH_IN
		 * 2:	CASH_OUT
		 * 3:	DEBIT
		 * 4:	DEPOSIT
		 * 5:	PAYMENT
		 * 6:	TRANSFER
		 */
		
		this.avgStdCashIn = this.getAvgStd(1);
		this.avgStdCashOut = this.getAvgStd(2);
		this.avgStdDebit = this.getAvgStd(3);
		this.avgStdDeposit = this.getAvgStd(4);
		this.avgStdPayment = this.getAvgStd(5);
		this.avgStdTransfer = this.getAvgAvg(6);
		
	}
	
	public void printSummary(){
		DecimalFormat format = new DecimalFormat("#.##########");
		/*
		 * 1:	CASH_IN
		 * 2:	CASH_OUT
		 * 3:	DEBIT
		 * 4:	DEPOSIT
		 * 5:	PAYMENT
		 * 6:	TRANSFER
		 */
		
		
		String splitted[] = this.toString().split(",");
		for(String s: splitted){
			System.out.println(s + "\n");
		}
		
		double total = this.totNrOfCashIn + this.totNrOfCashOut + this.totNrOfDebit + this.totNrOfDeposit + this.totNrOfPayments + 
				this.totNrOfTransfer;
		System.out.println("\n\nTot\t" + format.format(total) + "\n");
	}
		
	private boolean isNumber(String type){
		boolean isNumber = true;
		
		try {
			Double.parseDouble(type);
		} catch (Exception e) {
			isNumber = false;
		}
		
		return isNumber;
	}
	
	private String getTypeFromNumber(int typeNr){
		/*
		 * 1:	CASH_IN
		 * 2:	CASH_OUT
		 * 3:	DEBIT
		 * 4:	DEPOSIT
		 * 5:	PAYMENT
		 * 6:	TRANSFER
		 */
		
		switch(typeNr){
		case 1:
			return "CASH_IN";
			
		case 2:
			return "CASH_OUT";
			
		case 3:
			return "DEBIT";
			
		case 4:
			return "DEPOSIT";
			
		case 5:
			return "PAYMENT";
			
		case 6:
			return "TRANSFER";

		default:
			return null;
		}
		
		
		
		
	}

	
	//Handler function
	private double getCount(int typeNr){
		double count = 0;
		
		for(String s: this.fileContents){
			String splitted[] = s.split(",");
			String type = splitted[0];
			
			//Handle from the original aggregate file
			if(!isNumber(type)){
				String receivedType = getTypeFromNumber(typeNr);
				if(receivedType.equals(type)){
					count += Double.parseDouble(splitted[4]);
				}
			}else{
				//Handle from the generated aggregate file
				if(Double.parseDouble(type) == typeNr){
					count += Double.parseDouble(splitted[4]);
				}
			}
		}
		
		return count;
	}
	
	private double getAvgAvg(int typeNr){
		double avg = 0;
		double nr = 0;
		
		for(String s: this.fileContents){
			String splitted[] = s.split(",");
			String type = splitted[0];
			
			//Handle from the original aggregate file
			if(!isNumber(type)){
				String receivedType = getTypeFromNumber(typeNr);
				if(receivedType.equals(type)){
					avg += Double.parseDouble(splitted[6]);
					nr++;
				}
			}else{
				//Handle from the generated aggregate file
				if(Double.parseDouble(type) == typeNr){
					avg += Double.parseDouble(splitted[6]);
					nr++;
				}
			}
			
		}
		avg = avg / nr;
		
		return avg;
	}
	
	private double getAvgStd(int typeNr){
		double avgStd = 0;
		double nr = 0;
		
		for(String s: this.fileContents){
			String splitted[] = s.split(",");
			String type = splitted[0];
			
			//Handle from the original aggregate file
			if(!isNumber(type)){
				String receivedType = getTypeFromNumber(typeNr);
				if(receivedType.equals(type)){
					avgStd += Double.parseDouble(splitted[7]);
					nr++;
				}
			}else{
				//Handle from the generated aggregate file
				if(Double.parseDouble(type) == typeNr){
					avgStd += Double.parseDouble(splitted[7]);
					nr++;
				}
			}
			
		}
		avgStd = avgStd / nr;
		
		return avgStd;
	}
	
	
	
	
	//Setters and getters

	public double getTotNrOfTransfer() {
		return totNrOfTransfer;
	}

	public void setTotNrOfTransfer(double totNrOfTransfer) {
		this.totNrOfTransfer = totNrOfTransfer;
	}

	public double getTotNrOfDebit() {
		return totNrOfDebit;
	}

	public void setTotNrOfDebit(double totNrOfDebit) {
		this.totNrOfDebit = totNrOfDebit;
	}

	public double getTotNrOfCashIn() {
		return totNrOfCashIn;
	}

	public void setTotNrOfCashIn(double totNrOfCashIn) {
		this.totNrOfCashIn = totNrOfCashIn;
	}

	public double getTotNrOfCashOut() {
		return totNrOfCashOut;
	}

	public void setTotNrOfCashOut(double totNrOfCashOut) {
		this.totNrOfCashOut = totNrOfCashOut;
	}

	public double getTotNrOfDeposit() {
		return totNrOfDeposit;
	}

	public void setTotNrOfDeposit(double totNrOfDeposit) {
		this.totNrOfDeposit = totNrOfDeposit;
	}

	public double getTotNrOfPayments() {
		return totNrOfPayments;
	}

	public void setTotNrOfPayments(double totNrOfPayments) {
		this.totNrOfPayments = totNrOfPayments;
	}

	public ArrayList<String> getFileContents() {
		return fileContents;
	}

	public void setFileContents(ArrayList<String> fileContents) {
		this.fileContents = fileContents;
	}

	
	
	public double getAvgAvgTransfer() {
		return avgAvgTransfer;
	}

	public void setAvgAvgTransfer(double avgAvgTransfer) {
		this.avgAvgTransfer = avgAvgTransfer;
	}

	public double getAvgAvgDebit() {
		return avgAvgDebit;
	}

	public void setAvgAvgDebit(double avgAvgDebit) {
		this.avgAvgDebit = avgAvgDebit;
	}

	public double getAvgAvgCashIn() {
		return avgAvgCashIn;
	}

	public void setAvgAvgCashIn(double avgAvgCashIn) {
		this.avgAvgCashIn = avgAvgCashIn;
	}

	public double getAvgAvgCashOut() {
		return avgAvgCashOut;
	}

	public void setAvgAvgCashOut(double avgAvgCashOut) {
		this.avgAvgCashOut = avgAvgCashOut;
	}

	public double getAvgAvgDeposit() {
		return avgAvgDeposit;
	}

	public void setAvgAvgDeposit(double avgAvgDeposit) {
		this.avgAvgDeposit = avgAvgDeposit;
	}

	public double getAvgAvgPayments() {
		return avgAvgPayments;
	}

	public void setAvgAvgPayments(double avgAvgPayments) {
		this.avgAvgPayments = avgAvgPayments;
	}

	public double getAvgStdTransfer() {
		return avgStdTransfer;
	}

	public void setAvgStdTransfer(double avgStdTransfer) {
		this.avgStdTransfer = avgStdTransfer;
	}

	public double getAvgStdDebit() {
		return avgStdDebit;
	}

	public void setAvgStdDebit(double avgStdDebit) {
		this.avgStdDebit = avgStdDebit;
	}

	public double getAvgStdCashIn() {
		return avgStdCashIn;
	}

	public void setAvgStdCashIn(double avgStdCashIn) {
		this.avgStdCashIn = avgStdCashIn;
	}

	public double getAvgStdCashOut() {
		return avgStdCashOut;
	}

	public void setAvgStdCashOut(double avgStdCashOut) {
		this.avgStdCashOut = avgStdCashOut;
	}

	public double getAvgStdDeposit() {
		return avgStdDeposit;
	}

	public void setAvgStdDeposit(double avgStdDeposit) {
		this.avgStdDeposit = avgStdDeposit;
	}

	public double getAvgStdPayment() {
		return avgStdPayment;
	}

	public void setAvgStdPayment(double avgStdPayment) {
		this.avgStdPayment = avgStdPayment;
	}

	@Override
	public String toString() {
		return "AggregateDumpAnalyzer [totNrOfTransfer=" + totNrOfTransfer
				+ ", totNrOfDebit=" + totNrOfDebit + ", totNrOfCashIn="
				+ totNrOfCashIn + ", totNrOfCashOut=" + totNrOfCashOut
				+ ", totNrOfDeposit=" + totNrOfDeposit + ", totNrOfPayments="
				+ totNrOfPayments + ", avgAvgTransfer=" + avgAvgTransfer
				+ ", avgAvgDebit=" + avgAvgDebit + ", avgAvgCashIn="
				+ avgAvgCashIn + ", avgAvgCashOut=" + avgAvgCashOut
				+ ", avgAvgDeposit=" + avgAvgDeposit + ", avgAvgPayments="
				+ avgAvgPayments + ", avgStdTransfer=" + avgStdTransfer
				+ ", avgStdDebit=" + avgStdDebit + ", avgStdCashIn="
				+ avgStdCashIn + ", avgStdCashOut=" + avgStdCashOut
				+ ", avgStdDeposit=" + avgStdDeposit + ", avgStdPayment="
				+ avgStdPayment + "]";
	}


	



}























































