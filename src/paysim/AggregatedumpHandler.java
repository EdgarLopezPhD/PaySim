package paysim;

import java.text.DecimalFormat;

public class AggregatedumpHandler {

	double totalErrorRate = 0;
	public double getTotalErrorRate() {
		return totalErrorRate;
	}

	public void setTotalErrorRate(double totalErrorRate) {
		this.totalErrorRate = totalErrorRate;
	}

	public String getResultDump() {
		return resultDump;
	}

	public void setResultDump(String resultDump) {
		this.resultDump = resultDump;
	}

	String resultDump = "";

	public AggregatedumpHandler(){
		
	}
	
	public String checkDelta(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated){
		String results ="";
		DecimalFormat format = new DecimalFormat("#.###");
		String separator = "---------------------------------------------------------------------------------------------------------";
		this.resultDump += separator + "\n";
		this.resultDump += "|\tIndicator\t|\tOrig\t|\tSynth\t|\tError Rate\t|\n";
		this.resultDump += separator + "\n";
		this.resultDump += "|\tNR OF TRANS\t|\t\t|\t\t|\t\t\t|\n";
		this.resultDump += "|";
		
		getResultOfTotalTransaction(orig, generated);
		this.resultDump += separator + "\n";
		this.resultDump += "|\tAVG TRANS SIZE\t|\t\t\t|\t\t\t|\t\t\t\t|\n";
		getResultOfAvgTransaction(orig, generated);
		this.resultDump += separator + "\n";
		this.resultDump += "|\tTOT ERR RATE\t|\t\t\t|\t\t\t|\t" + format.format(this.totalErrorRate) + "\t\t\t|\n";
		this.resultDump += separator + "\n";
		System.out.println(this.resultDump + "\n");
		//System.out.println(results + "\n");
		
		return this.resultDump;
	}
	
	private String getResultOfTotalTransaction(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated){
		String results ="";
		DecimalFormat format = new DecimalFormat("#.###");
		DecimalFormat formatPayment = new DecimalFormat("##########.###");
		double errorRate = 0;
		
		errorRate = (orig.getTotNrOfCashIn() - generated.getTotNrOfCashIn())/orig.getTotNrOfCashIn();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="\tCASH_IN\t\t" + orig.getTotNrOfCashIn()  + "\t" + generated.getTotNrOfCashIn() + "\t" + 
				format.format(errorRate) + "\t\t"  + "\n";

		
		errorRate = (orig.getTotNrOfCashOut() - generated.getTotNrOfCashOut())/orig.getTotNrOfCashOut();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tCASH_OT\t\t\t" + orig.getTotNrOfCashOut()  + "\t\t" + generated.getTotNrOfCashOut() + "\t\t" + 
				format.format(errorRate) + "\t\t\t"  + "\n";
		
		
		errorRate = (orig.getTotNrOfTransfer() - generated.getTotNrOfTransfer())/orig.getTotNrOfTransfer();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tTRANS\t\t|\t" + orig.getTotNrOfTransfer()  + "\t\t" + generated.getTotNrOfTransfer() + "\t\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";
		
		
		errorRate = (orig.getTotNrOfPayments() - generated.getTotNrOfPayments())/orig.getTotNrOfPayments();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tPAYM\t\t|\t" + orig.getTotNrOfPayments()  + "\t|\t" + formatPayment.format(generated.getTotNrOfPayments()) + "\t|\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";
	
		
		
		errorRate = (orig.getTotNrOfDebit() - generated.getTotNrOfDebit())/orig.getTotNrOfDebit();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tDEB\t\t|\t" + orig.getTotNrOfDebit()  + "\t|\t" + generated.getTotNrOfDebit() + "\t\t|\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";
		
		return results;
	}

	private String getResultOfAvgTransaction(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated){
		String results ="";
		DecimalFormat format = new DecimalFormat("#.###");
		double errorRate = 0;

		errorRate = (orig.getAvgAvgCashIn() - generated.getAvgAvgCashIn())/orig.getAvgAvgCashIn();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tCASH_IN\t\t|\t" + format.format(orig.getAvgAvgCashIn())  + "\t|\t" + format.format(generated.getAvgAvgCashIn()) + "\t|\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";

		
		errorRate = (orig.getAvgAvgCashOut() - generated.getAvgAvgCashOut())/orig.getAvgAvgCashOut();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tCASH_OT\t\t|\t" + format.format(orig.getAvgAvgCashOut())  + "\t|\t" + format.format(generated.getAvgAvgCashOut()) + "\t|\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";
		
		
		errorRate = (orig.getAvgAvgTransfer() - generated.getAvgAvgTransfer())/orig.getAvgAvgTransfer();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tTRANS\t\t|\t" + format.format(orig.getAvgAvgTransfer())  + "\t|\t" + format.format(generated.getAvgAvgTransfer()) + "\t|\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";
		
		
		errorRate = (orig.getAvgAvgPayments() - generated.getAvgAvgPayments())/orig.getAvgAvgPayments();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tPAYM\t\t|\t" + format.format(orig.getAvgAvgPayments())  + "\t|\t" + format.format(generated.getAvgAvgPayments()) + "\t|\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";
		
		
		errorRate = (orig.getAvgAvgDebit() - generated.getAvgAvgDebit())/orig.getAvgAvgDebit();
		if(errorRate <0){
			errorRate *=-1;
		}
		totalErrorRate += errorRate;
		this.resultDump +="|\tDEB\t\t|\t" + format.format(orig.getAvgAvgDebit())  + "\t|\t" + format.format(generated.getAvgAvgDebit()) + "\t|\t" + 
				format.format(errorRate) + "\t\t\t|"  + "\n";
		
		return results;
	}
	
}























