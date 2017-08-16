package paysim;

public class ActionProbability {
	
	private String type;
	private double month;
	private double day;
	private double hour;
	private double nrOfTransactions;
	private double totalSum;
	private double average;
	private double std;
	
	public ActionProbability(){
		this.type = "";
		this.month = 0;
		this.day = 0;
		this.hour = 0;
		this.nrOfTransactions = 0;
		this.totalSum = 0;
		this.average = 0;
		this.std = 0;
	}
	
	@Override
	public String toString(){
		String toString = "";
		toString = "Type: " + this.type + "\nMonth: " + this.month + "\nDay: " + this.day + "\nHour: " + this.hour 
				+ "\nNrOfTransactions: " + this.nrOfTransactions + "\nTotalSum: " + this.totalSum + "\nAverage: " + this.average + "\nStd: " + this.std + "\n";
		return toString;
	}
	

	//Setters and getters
	
	public String getType() {
			return type;
		}
		
	public void setType(String type) {
			this.type = type;
		}
		
	public double getMonth() {
			return month;
		}
		
	public void setMonth(double month) {
			this.month = month;
		}
		
	public double getDay() {
			return day;
		}
		
	public void setDay(double day) {
			this.day = day;
		}
		
	public double getHour() {
			return hour;
		}
		
	public void setHour(double hour) {
			this.hour = hour;
		}
		
	public double getNrOfTransactions() {
			return nrOfTransactions;
		}
		
	public void setNrOfTransactions(double nrOfTransactions) {
			this.nrOfTransactions = nrOfTransactions;
		}
		
	public double getTotalSum() {
			return totalSum;
		}
		
	public void setTotalSum(double totalSum) {
			this.totalSum = totalSum;
		}
		
	public double getAverage() {
			return average;
		}
		
	public void setAverage(double average) {
			this.average = average;
		}
		
	public double getStd() {
			return std;
		}
		
	public void setStd(double std) {
			this.std = std;
		}
		
	
	
	
}
