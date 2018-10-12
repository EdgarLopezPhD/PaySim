package paysim;

public class CurrentStepContainer {
    private int currentStep, counter, nrReduced;

    CurrentStepContainer(int currentStep) {
        this.currentStep = currentStep;
        this.counter = 0;
        this.nrReduced = 0;
    }

    public boolean canBeReduced() {
        return nrReduced < counter;
    }

    @Override
    public String toString() {
        return "CurrentStepContainer [currentStep=" + currentStep
                + ", counter=" + counter + ", nrReduced=" + nrReduced + "]";
    }

    public void increment() {
        this.nrReduced++;
    }

    public int getNrReduced() {
        return nrReduced;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}