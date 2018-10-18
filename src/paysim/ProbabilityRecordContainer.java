package paysim;

import paysim.base.ActionProbability;

import java.util.ArrayList;

public class ProbabilityRecordContainer {
    private final int step;
    private ArrayList<ActionProbability> probList;

    public ProbabilityRecordContainer(int step, ArrayList<ActionProbability> probList) {
        this.step = step;
        this.probList = probList;
    }

    @Override
    public String toString() {
        String toReturn = "For step:\t" + this.step + "\n";
        for (ActionProbability p : this.probList) {
            toReturn += p.toString() + "\n";
        }
        return toReturn;
    }

    public ArrayList<ActionProbability> getProbList() {
        return probList;
    }
}
