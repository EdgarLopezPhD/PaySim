package paysim;

public class AggregateTransactionRecord implements Comparable<AggregateTransactionRecord>{

	String type ="";
	String month ="";
	String tDay ="";
	String tHour = "";
	String tCount = "";
	String tSum = "";
	String tAvg = "";
	String tStd = "";
	String tStep = "";
	
	
	
	

	@Override
	public String toString() {
		return "AggregateTransactionRecord [type=" + type + ", month=" + month
				+ ", tDay=" + tDay + ", tHour=" + tHour + ", tCount=" + tCount
				+ ", tSum=" + tSum + ", tAvg=" + tAvg + ", tStd=" + tStd
				+ ", tStep=" + tStep + "]";
	}

	public boolean equals(AggregateTransactionRecord rec){
		if(this.type.equals(rec.getType()) &&
				this.tHour.equals(rec.gettHour()) &&
				this.tDay.equals(rec.gettDay())){
			return true;
		}else {
			return false;
		}
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String gettDay() {
		return tDay;
	}
	public void settDay(String tDay) {
		this.tDay = tDay;
	}
	public String gettHour() {
		return tHour;
	}
	public void settHour(String tHour) {
		this.tHour = tHour;
	}
	public String gettCount() {
		return tCount;
	}
	public void settCount(String tCount) {
		this.tCount = tCount;
	}
	public String gettSum() {
		return tSum;
	}
	public void settSum(String tSum) {
		this.tSum = tSum;
	}
	public String gettAvg() {
		return tAvg;
	}
	public void settAvg(String tAvg) {
		this.tAvg = tAvg;
	}
	public String gettStd() {
		return tStd;
	}
	public void settStd(String tStd) {
		this.tStd = tStd;
	}
	
	public String gettStep() {
		return tStep;
	}

	public void settStep(String tStep) {
		this.tStep = tStep;
	}

	@Override
	public int compareTo(AggregateTransactionRecord record) {
		
		double inDay = Double.parseDouble(this.tDay);
		double inHour = Double.parseDouble(this.tHour);
		double inType = Double.parseDouble(this.type);
		double inputDay = Double.parseDouble(record.gettDay());
		double inputHour = Double.parseDouble(record.gettHour());
		double inputType = Double.parseDouble(record.getType());
		
		double valIn =  ((inType * 1000000) + (10000 * inDay) + (10 * inHour));
		double valInput = ((inputType * 1000000) + (10000 * inputDay) + (10 * inputHour));
		
		if(valIn > valInput){
			return 1;
		}else if(valIn < valInput){
			return -1;
		}
		return 0;
	}
	
}
