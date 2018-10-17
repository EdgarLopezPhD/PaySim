package paysim;

public class CurrentStepContainer {
    private int currentStep, maxCount, countAssigned;

    CurrentStepContainer(int currentStep) {
        this.currentStep = currentStep;
        this.maxCount = 0;
        this.countAssigned = 0;
    }

    public boolean canBeAssigned() {
        return countAssigned < maxCount;
    }

    @Override
    public String toString() {
        return "CurrentStepContainer [currentStep=" + currentStep
                + ", maxCount=" + maxCount + ", countAssigned=" + countAssigned + "]";
    }

    public void increment() {
        countAssigned++;
    }

    public int getCountAssigned() {
        return countAssigned;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
}