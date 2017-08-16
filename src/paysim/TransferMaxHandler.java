package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class TransferMaxHandler {
	
	ArrayList<String> fileContents = new ArrayList<String>();
	PaySim paysim;
	
	//Counters
	int transferCounter = 0;
	int paymentCounter = 0;
	int cashInCounter = 0;
	int cashOutCounter = 0;
	int debitCounter = 0;
	int depositCounter = 0;
	
	//The max limits for each type
	int transferMax = 0;
	int paymentMax = 0;
	int cashInMax = 0;
	int cashOutMax = 0;
	int debitMax = 0;
	int depositMax = 0;
	
	
	double multiplier = 1;
	
	private void initMax(ArrayList<String> fileContents){
		transferMax = Integer.parseInt(fileContents.get(6).split(",")[1]);
		paymentMax = Integer.parseInt(fileContents.get(4).split(",")[1]);
		cashInMax = Integer.parseInt(fileContents.get(0).split(",")[1]);
		cashOutMax = Integer.parseInt(fileContents.get(1).split(",")[1]);
		debitMax = Integer.parseInt(fileContents.get(2).split(",")[1]);
		depositMax = Integer.parseInt(fileContents.get(7).split(",")[1]);
		

		
	}
	
	@Override
	public String toString() {
		return "TransferMaxHandler [transferMax=" + transferMax
				+ ", paymentMax=" + paymentMax + ", cashInMax=" + cashInMax
				+ ", cashOutMax=" + cashOutMax + ", debitMax=" + debitMax
				+ ", depositMax=" + depositMax + "]";
	}

	
	public boolean repetitionFinished(){
		if(transferCounter >= transferMax &&
				paymentCounter >= paymentMax &&
				cashInCounter >= cashInMax &&
				cashOutCounter >= cashOutMax &&
				//&& debitCounter >= debitMax 
				 depositCounter >= depositMax
				)
		{
			return true;
		}
		return false;
		
	}
	
	public boolean canRepeat(String action){
		
		switch(action){
		
		case "CASH_IN":
			this.cashInCounter++;
			//////system.out.println("CashInCounter:\t" + this.cashInCounter + "\tMax:\t" + (this.multiplier * this.getMax(action)) + "\n");
			if(this.cashInCounter > (this.multiplier *cashInMax)){
				////system.out.println("CASH_IN:\tCAN NOOT REPEAT BECAUSE:\t" + this.cashInCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return false;
			}else{
				////system.out.println("CASH_IN:\tCAN REPEAT BECAUSE:\t" + this.cashInCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return true;
			}
		
		case "TRANSFER":			
			this.transferCounter++;
			if(this.transferCounter > (this.multiplier * transferMax)){
				////system.out.println("TRANSFER:\tCAN NOOT REPEAT BECAUSE:\t" + this.transferCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return false;
			}else{
				////system.out.println("TRANSFER:\tCAN REPEAT BECAUSE:\t" + this.transferCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return true;
			}
			
		case "CASH_OUT":			
			this.cashOutCounter++;
			if(this.cashOutCounter > (this.multiplier * cashOutMax)){
				////system.out.println("CASH_OUT:\tCAN NOOT REPEAT BECAUSE:\t" + this.cashOutCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return false;
			}else{
				////system.out.println("CASH_OUT:\tCAN REPEAT BECAUSE:\t" + this.cashOutCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return true;
			}
			
		case "DEBIT":			
			this.debitCounter++;
			if(this.debitCounter > (this.multiplier * debitMax)){
				////system.out.println("DEBIT:\tCAN NOOT REPEAT BECAUSE:\t" + this.debitCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return false;
			}else{
				////system.out.println("DEBIT:\tCAN REPEAT BECAUSE:\t" + this.debitCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return true;
			}
			
			
		case "DEPOSIT":			
			this.depositCounter++;
			if(this.depositCounter > (this.multiplier * depositMax)){
				////system.out.println("DEPOSIT:\tCAN NOOT REPEAT BECAUSE:\t" + this.depositCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return false;
			}else{
				////system.out.println("DEPOSIT:\tCAN REPEAT BECAUSE:\t" + this.depositCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return true;
			}
			
			
		case "PAYMENT":			
			this.paymentCounter++;
			if(this.paymentCounter > (this.multiplier * paymentMax)){
				////system.out.println("PAYMENT:\tCAN NOOT REPEAT BECAUSE:\t" + this.paymentCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return false;
			}else{
				////system.out.println("PAYMENT:\tCAN REPEAT BECAUSE:\t" + this.paymentCounter + " > " + (this.multiplier * this.getMax(action)) + "\n");
				return true;
			}
			
			default:
				
				return false;
			
		}
		
	}
	
	private int getMax(String action){
		for(String s: this.fileContents){
			String splitted[] = s.split(",");
			if(action.equals(splitted[0])){
				return Integer.parseInt(splitted[1]);
			}
			
		}
		return -1;
	}
	
	public TransferMaxHandler(PaySim paysim){
		init(paysim);
	}
	
	public TransferMaxHandler(String path){
		init(path);
	}
	
	private void init(String path){
		try {
			File f = new File(path);
			FileReader reader = new FileReader(f);
			BufferedReader bufReader = new BufferedReader(reader);
			String line = "";
			bufReader.readLine();
			while((line = bufReader.readLine()) != null){
				this.fileContents.add(line);
			}
			bufReader.close();
			for(String s: this.fileContents){
				//system.out.println(s + "\n");
			}
			
			initMax(fileContents);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init(PaySim paysim){
		this.paysim = paysim;
		try {
			File f = new File(this.paysim.getTransferMaxPath());
			FileReader reader = new FileReader(f);
			BufferedReader bufReader = new BufferedReader(reader);
			String line = "";
			
			while((line = bufReader.readLine()) != null){
				this.fileContents.add(line);
			}
			bufReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initMax(this.fileContents);
		this.multiplier = this.paysim.getMultiplier();
		
	}
	
	public double getMaxGivenType(String type){
		switch(type){
		case "TRANSFER":
			return this.transferMax;
			
		case "CASH_IN":
			return this.cashInMax;
			
		case "CASH_OUT":
			return this.cashOutMax;
			
		case "DEBIT":
			return this.debitMax;
			
		case "DEPOSIT":
			return this.depositMax;
			
		case "PAYMENT":
			return this.paymentMax;
			
		default: return -1.0;
		
		}
	}
	
	
}













