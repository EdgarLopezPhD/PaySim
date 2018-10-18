package paysim.base;

public class StepCounter {
    private final int currentStep;
    private int maxCount;
    private int countAssigned;

    public StepCounter(int currentStep) {
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

    public void addCount(int count) {
        this.maxCount += count;
    }
}