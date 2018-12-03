package paysim.output;

import paysim.parameters.ActionTypes;
import paysim.utils.CSVReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class AggregateDumpAnalyzer {
    private static final int COLUMN_ACTION = 0, COLUMN_COUNT = 4, COLUMN_AVERAGE = 6;
    private ArrayList<String[]> aggregate;

    public AggregateDumpAnalyzer(String filename) {
        aggregate = CSVReader.read(filename);
    }

    public Map<String, Double> computeCounts() {
        Map<String, Double> actionCounts = new HashMap<>();
        for (String action: ActionTypes.getActions()){
            actionCounts.put(action, getCount(action));
        }
        return actionCounts;
    }

    public Map<String, Double> computeAvgAvg() {
        Map<String, Double> actionAvgAvg = new HashMap<>();
        for (String action: ActionTypes.getActions()){
            actionAvgAvg.put(action, getAvgAvg(action));
        }
        return actionAvgAvg;
    }

    private double getCount(String receivedAction) {
        double count = 0;

        for (String[] line : aggregate) {
            String action = line[COLUMN_ACTION];
            if (receivedAction.equals(action)) {
                count += Double.parseDouble(line[COLUMN_COUNT]);
            }
        }

        return count;
    }

    private double getAvgAvg(String receivedAction) {
        double avg = 0, nr = 0;

        for (String[] line : aggregate) {
            String action = line[COLUMN_ACTION];
            if (receivedAction.equals(action)) {
                avg += Double.parseDouble(line[COLUMN_AVERAGE]);
                nr++;
            }
        }
        avg = avg / nr;

        return avg;
    }
}