package paysim;

import java.util.ArrayList;

public class ProbabilityRecordContainer {
	
	private int step;
	private ArrayList<ActionProbability> probList;
	
	public ProbabilityRecordContainer(){
		this.step = 0;
		this.probList = new ArrayList<ActionProbability>();
	}
	
	public ProbabilityRecordContainer(int step, ArrayList<ActionProbability> probList){
		this.step = step;
		this.probList = probList;
	}
	
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	@Override
	public String toString() {
		String toReturn = "For step:\t" + this.step + "\n";
		for(ActionProbability p: this.probList){
			toReturn += p.toString() + "\n";
		}
		return toReturn;
	}
	public ArrayList<ActionProbability> getProbList() {
		return probList;
	}
	public void setProbList(ArrayList<ActionProbability> probList) {
		this.probList = probList;
	}

}
