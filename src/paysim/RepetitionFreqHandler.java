package paysim;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class RepetitionFreqHandler {
	
	ArrayList<RepetitionFreqContainer> freqContList = new ArrayList<RepetitionFreqContainer>();
	
	public RepetitionFreqHandler(){
		
	}
	
	public void add(RepetitionContainer cont){
		
		//1) First, check that this type of repetitionContainer exists
		//2) If it does, add the frequencies
		//3) If not, create a new frequencyContainer, and add the freq and add it to the list
		if(!doesExist(cont)){
			RepetitionFreqContainer contNew = new RepetitionFreqContainer();
			contNew.setCont(cont);
			contNew.incrementFrequency();
			this.freqContList.add(contNew);
		
			//If it does not exist, first look for it, increment the frequency
		}else{
			for(RepetitionFreqContainer cToCheck: freqContList){
				if(cToCheck.getCont().equals(cont)){
					cToCheck.incrementFrequency();
				}
			}
		}
		
		
	}
	
	private boolean doesExist(RepetitionContainer temp){
		for(int i=0; i<freqContList.size(); i++){
			if(freqContList.get(i).getCont().equals(temp)){
				return true;
			}
		}
		return false;
	}
	
	private double getTot(){
		double totToReturn = 0;
		for(RepetitionFreqContainer cont: this.freqContList){
			totToReturn += cont.getFreq();
		}
		return totToReturn;
	}
	
	public String getFrequencyList(){
		String toReturn = "";
		double tot = getTot();
		DecimalFormat format = new DecimalFormat("#.######");
		Collections.sort(this.freqContList);
		for(RepetitionFreqContainer f: freqContList){
			toReturn += f.toString() + "," +  format.format(((f.getFreq())/tot)) +  "\n";
		}
		return toReturn;
	}
	
}
































