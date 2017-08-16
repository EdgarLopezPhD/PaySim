package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class RepetitionHandler {
	
	ArrayList<String> fileContents;
	private Double[] probabilityArr;
	long seed = 11343;
	Random r ;
	

	private double totNr = 0;
	
	public RepetitionHandler(long seed, PaySim p){
		this.fileContents = new ArrayList<String>();
		init(p);
		this.r = new Random(seed);
	}
	
	public int getAction(){
		int actionGenerated = 0;
		do {
			actionGenerated = this.generateAction();
		} while (actionGenerated == -1);
		
		return actionGenerated;
	}
	
	//Main usage function
	public RepetitionContainer getRepetition(){
		
		RepetitionContainer contToReturn = new RepetitionContainer();

		//Firstly, get the action to be potentially repeated, CASH_IN/CASH_OUT ...etc. 
		int action = this.getAction();
		
		//Get the name of the action
		String actionType = this.fileContents.get(action).split(",")[0];
		
		//Now, given that action, and based on the probabilities for each rep combination for that action, randomize how many
		//times to repeat that action
		ArrayList<String> subList = new ArrayList<String>();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ point to optimize
		for(int i=6; i<this.fileContents.size(); i++){
			if(this.fileContents.get(i).split(",")[0].equals(actionType)){
				subList.add(this.fileContents.get(i));
			}
		}
		
		//Now that all of the records for that action type are separated, make a new probability array and randomize an index from that
		double probArr[] = new double[subList.size()];
		for(int i=0; i<subList.size(); i++){
			probArr[i] = Double.parseDouble(subList.get(i).split(",")[5]);
		}
		
		//And now, get the index based on that probabilities
		int index = getIndex(probArr);
		
		//From that index, parse out a repetition container record
		String record = subList.get(index);
		String splittedRecord[] = record.split(",");
		
		contToReturn.setLow(Double.parseDouble(splittedRecord[1]));
		contToReturn.setHigh(Double.parseDouble(splittedRecord[2]));
		contToReturn.setAvg(Double.parseDouble(splittedRecord[3]));
		contToReturn.setStd(Double.parseDouble(splittedRecord[4]));
		contToReturn.setType(splittedRecord[0]);
		
		
		return contToReturn;
	}
	
	private int getIndex(double probArr[]){
		
		double randNr = r.nextDouble();
		double total = 0;
		
		for(int i=0; i<probArr.length; i++){
			double currProb = probArr[i];
			
			if(randNr >= total && 
					randNr <= (total + currProb)){
				//System.out.println("Returned: " + (i+1) + " Because RandNr (" + randNr + ") is >= " + total + " and <= " + (total+currProb) + "\n\n\n\n");
				return i;
			}else{
				total += currProb;
			}			
		}
		return -1;
	}
	
	private int generateAction(){
		double randNr = r.nextDouble();
		double total = 0;
		
		for(int i=0; i<this.probabilityArr.length; i++){
			double currProb = this.probabilityArr[i];
			
			if(randNr >= total && 
					randNr <= (total + currProb)){
				//System.out.println("Returned: " + (i+1) + " Because RandNr (" + randNr + ") is >= " + total + " and <= " + (total+currProb) + "\n\n\n\n");
				return i;
			}else{
				total += currProb;
			}			
		}
		return -1;
	}
	
	private void init(PaySim paysim){		
		//Read the file contents
		readFileContents(paysim);
		
		//Init the totNr
		//initTotNr();
		
		//Init the probability array
		initProbabilityArr();
	}

	private void initProbabilityArr(){
		//Init the probability array
		this.probabilityArr = new Double[6];
		/*
		 * 1:	CASH_IN
		 * 2:	CASH_OUT
		 * 3:	DEBIT
		 * 4:	DEPOSIT
		 * 5:	PAYMENT
		 * 6:	TRANSFER
		 */
		ArrayList<String> actions = new ArrayList<String>();
		actions.add("CASH_IN");
		actions.add("CASH_OUT");
		actions.add("DEBIT");
		actions.add("DEPOSIT");
		actions.add("PAYMENT");
		actions.add("TRANSFER");
		
		for(int i=0; i<6; i++){
			this.probabilityArr[i] = Double.parseDouble(this.fileContents.get(i).split(",")[1]);
		}
		
//		for(Double d: this.probabilityArr){
//			System.out.println(d + "\n");
//		}
		
	}

	//Based on an action, go through the entire file that contains the probabilities, add the aggregate for the action and 
	//return its freq in %
	private double getProbability(String action){
		double probabilityToReturn = 0;
		double nrAction = 0;
		
		for(String s: this.fileContents){
			String splitted[] = s.split(",");
			if(splitted[0].equals(action)){
				double nr = Double.parseDouble(splitted[3]);
				nrAction += nr;
			}			
		}
		//System.out.println("Action:\t" + action + "\t\t\tNr:\t" + nrAction + "\n");
		probabilityToReturn = nrAction / this.totNr;
		
		return probabilityToReturn;
	}
	
	private void initTotNr(){
		for(String s: this.fileContents){
			String splitted[] = s.split(",");
			double nr = Double.parseDouble(splitted[3]);
			this.totNr += nr;
		}
		System.out.println("Tot\t" + this.totNr + "\n");
	}
	
	private void readFileContents(PaySim paysim){
		File f1 = new File(paysim.transferFreqModInit);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			String line = "";
			while((line = reader.readLine()) != null){
				this.fileContents.add(line);
			}
			reader.close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
		File f = new File(paysim.transferFreqMod);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = "";
			reader.readLine();
			while((line = reader.readLine()) != null){
				this.fileContents.add(line);
			}
			reader.close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}
	
}
























