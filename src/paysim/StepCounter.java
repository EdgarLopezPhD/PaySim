package paysim;

public class StepCounter {
    private int currentStep, maxCount, countAssigned;

    StepCounter(int currentStep) {
        this.currentStep = currentStep;
        this.maxCount = 0;
        this.countAssigned = 0;
    }

    public boolean canBeAssigned() {
        return countAssigned < maxCount;
    }

    public void increment() {
        countAssigned++;
    }

    @Override
    public String toString() {
        return "StepCounter [currentStep=" + currentStep
                + ", maxCount=" + maxCount + ", countAssigned=" + countAssigned + "]";
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