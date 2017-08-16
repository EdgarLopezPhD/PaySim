package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ProbabilityContainerHandler {
	
	ArrayList<ProbabilityRecordContainer> list;
	
	public ArrayList<ProbabilityRecordContainer> getList() {
		return list;
	}

	public void setList(ArrayList<ProbabilityRecordContainer> list) {
		this.list = list;
	}

	public static void main(String args[]){
		ProbabilityContainerHandler handler = new ProbabilityContainerHandler();
		
		String fileName = "C://Users//ahmad//Desktop//EclipseProjs//git//projects//ahmad//paysim//paramFiles//AggregateTransaction.csv";
		ArrayList<String> fileContentsOrig = new ArrayList<String>();
		try {
			File f = new File(fileName);
			FileReader fReader = new FileReader(f);
			BufferedReader reader = new BufferedReader(fReader);
			String line = reader.readLine();
			while((line = reader.readLine()) != null){
				fileContentsOrig.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		handler.initRecordList(fileContentsOrig);
		//handler.printRecordList();
		
		ProbabilityRecordContainer cont = handler.getList().get(20-1);
		System.out.println(cont.toString() + "\n");
	}
	
	public void initRecordList(ArrayList<String> fileContents){
		
		ArrayList<String> actionTypes = new ArrayList<String>();
		ArrayList<ActionProbability> aProbListTemp = new ArrayList<ActionProbability>();
		actionTypes.add("CASH_IN");
		actionTypes.add("CASH_OUT");
		actionTypes.add("DEBIT");
		actionTypes.add("PAYMENT");
		actionTypes.add("TRANSFER");
		int j=0;
		
		for(int i=1; i<=744; i++){
			for(j=0; j<actionTypes.size(); j++){
				ActionProbability probTemp = getActionProbabilityFromStep(actionTypes.get(j), i, fileContents);
				aProbListTemp.add(probTemp);
			}
			ProbabilityRecordContainer record = new ProbabilityRecordContainer(i, aProbListTemp);
			this.list.add(record);
			j=0;
			aProbListTemp = new ArrayList<ActionProbability>();
		}
		
	}
	
	private ActionProbability getActionProbabilityFromStep(String type, int step, ArrayList<String> paramFile){
		ActionProbability probToReturn = new ActionProbability();
		for(String s: paramFile){
			String splitted[] = s.split(",");
			if(String.valueOf(step).equals(splitted[8]) &&
					splitted[0].equals(type))
			{
				probToReturn = getActionProb(s);
			}
		}
		return probToReturn;
	}
	
	private ActionProbability getActionProb(String line){
 		String tokens[] = line.split(",");
 		ActionProbability prob = new ActionProbability();
 		
 		prob.setType(tokens[0]);
 		prob.setMonth(Double.parseDouble(tokens[1]));
 		prob.setDay(Double.parseDouble(tokens[2]));
 		prob.setHour(Double.parseDouble(tokens[3]));
 		prob.setNrOfTransactions(Double.parseDouble(tokens[4]));
 		prob.setTotalSum(Double.parseDouble(tokens[5]));
 		prob.setAverage(Double.parseDouble(tokens[6]));
 		prob.setStd(Double.parseDouble(tokens[7]));
 		
 		return prob;
 	}
	
	public ProbabilityContainerHandler(){
		this.list = new ArrayList<ProbabilityRecordContainer>();
	}
	
	public void addRecord(ProbabilityRecordContainer c){
		this.list.add(c);
	}
	
	public void printRecordList(){
		for(int i=0; i<200; i++){
			System.out.println(this.list.get(i).toString() + "\n");
		}
	}
	

	

}













































