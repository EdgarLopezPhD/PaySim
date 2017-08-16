package paysim;

import java.util.ArrayList;

public class CurrentStepHandler {
	
	private ArrayList<CurrentStepContainer> stepHandler = new ArrayList<>();
	private ArrayList<CurrentStepContainer> stepCountCombination = new ArrayList<>();
	private ArrayList<String> fileContents = new ArrayList<String>();
	private ArrayList<Integer> stepsThatAreNotAllowed = new ArrayList<Integer>();
	private ArrayList<AggregateTransactionRecord> aggrRecordList = new ArrayList<AggregateTransactionRecord>();
	private double multiplier = 0;
	
	public CurrentStepHandler(ArrayList<String> fileContents, double mult){
		setFileContents(fileContents);
		for(int i=0; i<744; i++){
			CurrentStepContainer container = new CurrentStepContainer();
			container.setCounter(0);
			container.setCurrentStep(i);
			stepHandler.add(container);
		}
		//initStepsNotAllowed();
		initStepCountCombination();
		modifyWithMultiplier(mult);
		this.initAggregateRecordList();
	}
	
	private void initAggregateRecordList(){
		for(String s: this.fileContents){
			String splitted[] = s.split(",");
			AggregateTransactionRecord record = new AggregateTransactionRecord();
			record.setType(splitted[0]);
			record.setMonth(splitted[1]);
			record.settDay(splitted[2]);
			record.settHour(splitted[3]);
			record.settCount(splitted[4]);
			record.settSum(splitted[5]);
			record.settAvg(splitted[6]);
			record.settStd(splitted[7]);			
			this.aggrRecordList.add(record);
		}
	}
	
	public AggregateTransactionRecord getRecord(String type, int day, int hour){
		for(AggregateTransactionRecord t: this.aggrRecordList){
			if(t.getType().equals(type) &&
					t.gettDay().equals(String.valueOf(day)) &&
					t.gettHour().equals(String.valueOf(hour))){
				return t;
			}
		}
		return null;
	}
	
	private void modifyWithMultiplier(double mult){
		for(int i=0; i<this.stepHandler.size(); i++){
			long newCounter = this.stepHandler.get(i).getCounter();
			newCounter = (long) (newCounter * mult);
			this.stepHandler.get(i).setCounter(newCounter);
		}
	}
	
	private void initStepCountCombination(){		
		for(int i=0; i<stepHandler.size(); i++){
			long count = 0;
			long currDay= i/24 + 1;
			long currHour = (int)  (i - ((currDay - 1) * 24));
			CurrentStepContainer currentContainer = stepHandler.get(i);
			boolean isInParamFile = this.isInParamFile(currDay, currHour);
			if(isInParamFile){
				int j=0; 
				for(j=1; j<this.fileContents.size(); j++){
					String line[] = this.fileContents.get(j).split(",");
					try {
						if(Long.parseLong(line[2]) == currDay &&
								Long.parseLong(line[3]) == currHour
								&& isAction(line[0])){
							count += Long.parseLong(line[4]);
						}
					} catch (Exception e) {
						//sytemout.println("continuing" + j  + "\tSize:\t" + this.fileContents.size() + "\n");
						continue;
					}	
				}
				currentContainer.setCounter(count);
				j=0;
			}			
		}
		
		//Removing steps at which there are no transactions
		for(int i=0; i<this.stepHandler.size(); i++){
			if(this.stepHandler.get(i).getCounter() == 0){
				this.stepHandler.remove(i);
				i--;
			}
		}
		
		for(CurrentStepContainer cont: stepHandler){
			//sytemout.println(cont.toString() + "\n");
		}
	}
	
	private void initStepsNotAllowed(){
		int j=0; 
		for(int i=0; i<744; i++){
			long currDay= i/24 + 1;
			long currHour = (int)  (i - ((currDay - 1) * 24));
			boolean isInParamFile = this.isInParamFile(currDay, currHour);
			
			if(!isInParamFile){
				this.stepsThatAreNotAllowed.add(i);
			}
			
		}
	}
	
	public long getNrOfTimesToReduce(long stepNumber){
		for(int i=0; i<this.stepHandler.size(); i++){
			if(this.stepHandler.get(i).getCurrentStep() == stepNumber){
				return this.stepHandler.get(i).getNrReduced();
			}
		}
		return -1;
	}

	public void increaseReduction(long stepNumber){
		for(int i=0; i<744; i++){
			if(this.stepHandler.get(i).getCurrentStep() == stepNumber){
				this.stepHandler.get(i).increment();
			}
		}
	}
	
	public void printInfo(){
		long cumulative = 0;
		for(int i=0; i<this.stepHandler.size(); i++){
			CurrentStepContainer cont = this.stepHandler.get(i);
			cumulative += cont.getCounter();
		}
		//sytemout.println("CUMULATIVE\t" + cumulative + "\n");
	}
		
	public boolean isTransactionsAtStep(long step, PaySim paysim){
		////sytemout.println("Step:\t" + step + "\n");
		for(int i=0; i<this.stepsThatAreNotAllowed.size(); i++){
			if(step == this.stepsThatAreNotAllowed.get(i)){
				
				return false;
			}
		}
		return true	;
	}
	
	public boolean nrOfRepGreaterThanCount(long step, PaySim paysim){
		//Get the original count for that step
		long origCount = 0;
		long nrOfRep = 0;
		
		for(int i=0; i<this.stepCountCombination.size(); i++){
			if(this.stepCountCombination.get(i).getCurrentStep() == step){
				origCount = this.stepCountCombination.get(i).getCounter();
				break;
			}
		}
		
		//Get the nr of rep for that step
		for(int i=0; i<this.stepHandler.size(); i++){
			if(this.stepHandler.get(i).getCurrentStep() == step){
				nrOfRep = this.stepHandler.get(i).getCounter();
				break;
			}
		}
		
		////sytemout.println("NrOfRep:\t" + nrOfRep + "\tCountWithMultiplier:\t" + (paysim.multiplier*count) + "\n");
		origCount *= paysim.getMultiplier();
		////sytemout.println("NrOfRep:\t" + nrOfRep + "\tOrigCount:\t" + origCount + "\n");
		if(nrOfRep > origCount){			
			stepsThatAreNotAllowed.add((int) step);
			////sytemout.println("Size of steps not allowed:\t" + stepsThatAreNotAllowed.size() + "Adding: " + step + "\n");
			return true;
		}else{
			return false;
		}
		
	}
	
	public void setFileContents(ArrayList<String> contents){
		this.fileContents = contents;
	}
	
	private boolean isInParamFile(long day, long hour){
		for(int i=1; i<this.fileContents.size(); i++){
			String s = this.fileContents.get(i);
			String splitted[] = s.split(",");
			if(Long.parseLong(splitted[2]) == day &&
					Long.parseLong(splitted[3]) == hour)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isAction(String token){
		if(token.equals("CASH_IN") ||
				token.equals("CASH_OUT") ||
				token.equals("TRANSFER") ||
				token.equals("PAYMENT") ||
				token.equals("DEBIT") ||
				token.equals("DEPOSIT"))
		{
			return true;
		}
		return false;
	}
		
	public ArrayList<Long> getSteps(long currentStep, long nrOfSteps){
		ArrayList<Long> stepsToBeRepeated = new ArrayList<Long>();
		int stepsGathered = 0;
		long index = 0;
		while(stepsGathered < nrOfSteps){
			index = index % this.stepHandler.size();
			if(index == 0){
				if(this.isFull(currentStep)){
					return null;
				}
			}
			////sytemout.println(index + "\n");
			CurrentStepContainer gottenOne = this.stepHandler.get((int) index);
			if(gottenOne.canBeReduced() 
					&& gottenOne.getCurrentStep()>= currentStep)
			{
				this.stepHandler.get((int) index).increment();
				stepsToBeRepeated.add(gottenOne.getCurrentStep());
				stepsGathered++;
			}
			index++;
		}
		return stepsToBeRepeated;
		
	}
	
	//Modify is full
	public boolean isFull(long currentStep){
		for(int i=0; i<this.stepHandler.size(); i++){
			if(this.stepHandler.get(i).getCurrentStep() >= currentStep){
				if(this.stepHandler.get(i).canBeReduced()){
					////sytemout.println("False because:\t" + this.stepHandler.get(i).toString() + "\n");
					return false;
				}else{
					////sytemout.println("Cant be reduced because:\t" + this.stepHandler.get(i).toString() + "\n");
				}
			}else{
				////sytemout.println("CurrentStepNotGreater:\t" + "CurrStep:\t" + currentStep + "\t" + this.stepHandler.get(i).getCurrentStep() + "\n");
			}
		}
		return true;
	}
	
	//New functionality
	
	public void printInfo2(){
		long totalCount = 0;
		for(int i=0; i<this.stepHandler.size(); i++){
			//sytemout.println(this.stepHandler.get(i).toString() + "\n");
			totalCount += this.stepHandler.get(i).getCounter();
		}
		//sytemout.println("Counter:\t" + totalCount + "\n");
	}
	
	public void convertDeconvert(){
		int currentStep = 50;
		
		for(CurrentStepContainer cont: this.stepHandler){
			int currDay = (int)(cont.getCurrentStep()/24) + 1;
			int currHour = (int)  (cont.getCurrentStep() - (( currDay - 1) * 24));
			
			//sytemout.println("Converting:\t" + cont.getCurrentStep() + "\tDay:\t" + currDay + "\tHour:\t" + currHour + "\n");
		}
		
		 
		
	}
}















