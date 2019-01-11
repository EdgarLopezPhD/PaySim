package org.paysim.paysim.base;

public class ClientActionProfile {
    private final String action;
    private final int minCount, maxCount;
    private final double avgAmount, stdAmount;

    public ClientActionProfile(String action, int minCount, int maxCount, double avgAmount, double stdAmount) {
        this.action = action;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.avgAmount = avgAmount;
        this.stdAmount = stdAmount;
    }

    public String getAction() {
        return action;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public double getAvgAmount() {
        return avgAmount;
    }

    public double getStdAmount() {
        return stdAmount;
    }
}