package paysim.parameters;

import paysim.base.ActionProbability;
import paysim.utils.CSVReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static paysim.parameters.TransactionParameters.isValidAction;

public class StepParameters {
    private static final int COLUMN_ACTION = 0, COLUMN_MONTH = 1, COLUMN_DAY = 2, COLUMN_HOUR = 3, COLUMN_COUNT = 4,
            COLUMN_SUM = 5, COLUMN_AVERAGE = 6, COLUMN_STD = 7, COLUMN_STEP = 8;
    private static ArrayList<Map<String, ActionProbability>> probabilitiesPerStep;
    private static ArrayList<Integer> stepMaxCount = new ArrayList<>();

    public static void initRecordList(String filename, double multiplier, int nbSteps) {
        ArrayList<String[]> parameters = CSVReader.read(filename);

        probabilitiesPerStep = new ArrayList<>(Collections.nCopies(nbSteps, new HashMap<>()));
        stepMaxCount = new ArrayList<>(Collections.nCopies(nbSteps, 0));

        for (String[] line : parameters) {
            if (isValidAction(line[COLUMN_ACTION])) {
                int step = Integer.parseInt(line[COLUMN_STEP]);
                int count = Integer.parseInt(line[COLUMN_COUNT]);
                ActionProbability probability = new ActionProbability(line[COLUMN_ACTION],
                        Integer.parseInt(line[COLUMN_MONTH]),
                        Integer.parseInt(line[COLUMN_DAY]),
                        Integer.parseInt(line[COLUMN_HOUR]),
                        count,
                        Double.parseDouble(line[COLUMN_SUM]),
                        Double.parseDouble(line[COLUMN_AVERAGE]),
                        Double.parseDouble(line[COLUMN_STD]));
                probabilitiesPerStep.get(step).put(line[COLUMN_ACTION], probability);
                stepMaxCount.set(step, stepMaxCount.get(step) + count);
            }
        }
        modifyWithMultiplier(multiplier);
    }

    public static Map<String, ActionProbability> get(int step) {
        return probabilitiesPerStep.get(step);
    }

    private static void modifyWithMultiplier(double multiplier) {
        for (int step = 0; step < stepMaxCount.size(); step++) {
            int newMaxCount = Math.toIntExact(Math.round(stepMaxCount.get(step) * multiplier));
            stepMaxCount.set(step, newMaxCount);
        }
    }

    public static int getMaxCount(int step) {
        return stepMaxCount.get(step);
    }
}