package paysim;

public class RepetitionContainer {
	
	private String type="";
	private double low;
	private double high;
	@Override
	public String toString() {
		return "RepetitionContainer [type=" + type + ", low=" + low + ", high="
				+ high + ", count=" + count + ", avg=" + avg + ", std=" + std
				+ "]" ;
	}

	private double count;
	private double avg;
	private double std;
	
	public RepetitionContainer(){
		this.type ="";
		this.low = 0;
		this.high = 0;
		this.count = 0;
		this.avg = 0;
		this.std = 0;
	}

	
	public boolean equals(RepetitionContainer cont){
		if(this.type.equals(cont.getType()) &&
				this.low == cont.getLow() &&
				this.high == cont.getHigh())
		{
			return true;
		}
		return false;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getCount() {
		return count;
	}

	public void setCount(double count) {
		this.count = count;
	}

	public double getAvg() {
		return avg;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}

	public double getStd() {
		return std;
	}

	public void setStd(double std) {
		this.std = std;
	}

}
