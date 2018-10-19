package paysim.parameters;

import paysim.base.StepCounter;
import paysim.base.ActionProbability;
import paysim.utils.CSVReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static paysim.parameters.TransactionParameters.isValidAction;

public class StepParameters {
    private static final int COLUMN_ACTION = 0, COLUMN_MONTH = 1, COLUMN_DAY = 2, COLUMN_HOUR = 3, COLUMN_COUNT = 4,
            COLUMN_SUM = 5, COLUMN_AVERAGE = 6, COLUMN_STD = 7, COLUMN_STEP = 8;
    private static ArrayList<Map<String, ActionProbability>> probabilitiesPerStep;
    private static ArrayList<StepCounter> stepHandler = new ArrayList<>();

    public static void initRecordList(String filename, double multiplier, int nbSteps) {
        initSteps(nbSteps);
        ArrayList<String[]> parameters = CSVReader.read(filename);
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
                stepHandler.get(step).addCount(count);
            }
        }
        modifyWithMultiplier(multiplier);
    }

    private static void initSteps(int nbSteps) {
        probabilitiesPerStep = new ArrayList<>(nbSteps);
        while (probabilitiesPerStep.size() < nbSteps) {
            probabilitiesPerStep.add(new HashMap<>());
        }

        for (int i = 0; i < nbSteps; i++) {
            StepCounter container = new StepCounter(i);
            stepHandler.add(container);
        }
    }

    public static Map<String, ActionProbability> get(int step) {
        return probabilitiesPerStep.get(step);
    }

    private static void modifyWithMultiplier(double multiplier) {
        for (StepCounter stepContainer : stepHandler) {
            int newMaxCount = stepContainer.getMaxCount();
            newMaxCount = Math.toIntExact(Math.round(newMaxCount * multiplier));
            stepContainer.setMaxCount(newMaxCount);
        }
    }

    //TODO : MOVE EVERYTHING UNDER THIS LINE
    public static int getCountAssigned(int step) {
        StepCounter stepCounter = stepHandler.get(step);
        return stepCounter.getCountAssigned();
    }

    public static ArrayList<Integer> getSteps(int currentStep, int nbSteps) {
        ArrayList<Integer> stepsToBeRepeated = new ArrayList<>();
        int stepsGathered = 0;
        int index = 0;
        while (stepsGathered < nbSteps) {
            index = index % stepHandler.size();
            if (index == 0 && isFull(currentStep)) {
                return null;
            }
            StepCounter step = stepHandler.get(index);
            if (step.canBeAssigned() && step.getCurrentStep() >= currentStep) {
                stepHandler.get(index).increment();
                stepsToBeRepeated.add(step.getCurrentStep());
                stepsGathered++;
            }
            index++;
        }
        return stepsToBeRepeated;

    }

    private static boolean isFull(int currentStep) {
        for (StepCounter stepContainer : stepHandler) {
            if (stepContainer.getCurrentStep() >= currentStep &&
                    stepContainer.canBeAssigned()) {
                return false;
            }
        }
        return true;
    }
}