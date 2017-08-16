package paysim;

public class CurrentStepContainer {
	
	private long currentStep = 0;
	private long counter = 0;
	private long nrReduced = 0;
	
	public long getNrReduced() {
		return nrReduced;
	}
	public void setNrReduced(long nrReduced) {
		this.nrReduced = nrReduced;
	}
	public long getCurrentStep() {
		return currentStep;
	}
	public void setCurrentStep(long currentStep) {
		this.currentStep = currentStep;
	}
	public long getCounter() {
		return counter;
	}
	public void setCounter(long counter) {
		this.counter = counter;
	}
	public boolean canBeReduced(){
		if(nrReduced < (counter)){
			return true;
		}
		return false;
	}
	
	
	@Override
	public String toString() {
		return "CurrentStepContainer [currentStep=" + currentStep
				+ ", counter=" + counter + ", nrReduced=" + nrReduced + "]";
	}
	public void increment(){
		this.nrReduced++;
	}
	
	
}
