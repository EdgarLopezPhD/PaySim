package paysim;

public class RepetitionFreqContainer implements Comparable<RepetitionFreqContainer>{
	
	RepetitionContainer cont;
	double typeNr = 0;
	int freq;
	
	public RepetitionFreqContainer(){
		this.freq = 0;
		this.cont = new RepetitionContainer();
	}
	
	public String toString(){
		return this.cont.getType() + "," + ((int)this.cont.getLow())  + ","+ ((int)this.cont.getHigh()) + "," + this.freq;
	}
	
	public RepetitionContainer getCont() {
		return cont;
	}
	public void setCont(RepetitionContainer cont) {
		this.cont = cont;
		switch(cont.getType()){
		case "CASH_IN":
			this.typeNr = 1000000000;
			break;
			
		case "CASH_OUT":
			this.typeNr = 9000000;
			break;
			
		case "DEBIT":
			this.typeNr = 300000;
			break;
			
		case "PAYMENT":
			this.typeNr = 100;
			break;
			
		case "TRANSFER":
			this.typeNr = -1;
			break;
		}
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}

	public void incrementFrequency(){
		this.freq++;
	}

	@Override
	public int compareTo(RepetitionFreqContainer r) {
		if((this.typeNr ) > 
			(r.typeNr )){
			return -1;
			
		}else if((this.typeNr) < 
		(r.typeNr )){
			return 1;
		}
		return 0;
	}
}
