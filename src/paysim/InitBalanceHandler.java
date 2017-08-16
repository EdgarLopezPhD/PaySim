package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class InitBalanceHandler {

	ArrayList<String> fileContents = new ArrayList<String>();
	private double [] probArr;
	PaySim paysim;
	
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
			this.probArr = new double[this.fileContents.size()];
			for(int i=0; i<this.fileContents.size(); i++){
				probArr[i] = Double.parseDouble(this.fileContents.get(i).split(",")[2]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public double getBalance(){
		double balance = 0;
		int index = 0;
			
		double randNr = this.paysim.random.nextDouble();
		double total = 0;
		
		//Get the index
		for(int i=0; i<probArr.length; i++){
			double currProb = probArr[i];
			
			if(randNr >= total && 
					randNr <= (total + currProb)){
				//System.out.println("Returned: " + (i+1) + " Because RandNr (" + randNr + ") is >= " + total + " and <= " + (total+currProb) + "\n\n\n\n");
				
				//In the special case there are no debits
				index = i;
				break;
			}else{
				total += currProb;
			}			
		}
		
		//With the index, get the high and the low end of the balance to be generated
		String record = this.fileContents.get(index);
		String splitted[] = record.split(",");
		double high = Double.parseDouble(splitted[1]);
		double low = Double.parseDouble(splitted[0]);
		double diff = high - low;
		double randed = this.paysim.random.nextLong() % diff;
		//System.out.println("THe index\t" + index + "\n");
		if(randed<0){
			randed *=-1;
		}
		
		balance = randed + low;
		
		return balance;
	}
	
	
	public ArrayList<String> getFileContents() {
		return fileContents;
	}

	public void setFileContents(ArrayList<String> fileContents) {
		this.fileContents = fileContents;
	}

	public double[] getProbArr() {
		return probArr;
	}

	public void setProbArr(double[] probArr) {
		this.probArr = probArr;
	}

	public PaySim getPaysim() {
		return paysim;
	}

	public void setPaysim(PaySim paysim) {
		this.paysim = paysim;
	}

	public InitBalanceHandler(String fileName){
		init(fileName);
	}
}
