package paysim.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import paysim.utils.CSVReader;
import static paysim.parameters.ActionTypes.isValidAction;

import paysim.base.StepActionProfile;


public class StepsProfiles {
    private static final int COLUMN_ACTION = 0, COLUMN_MONTH = 1, COLUMN_DAY = 2, COLUMN_HOUR = 3, COLUMN_COUNT = 4,
            COLUMN_SUM = 5, COLUMN_AVERAGE = 6, COLUMN_STD = 7, COLUMN_STEP = 8;
    private static ArrayList<Map<String, StepActionProfile>> profilePerStep;
    private static ArrayList<Map<String, Double>> probabilitiesPerStep = new ArrayList<>();
    private static ArrayList<Integer> stepTargetCount = new ArrayList<>();
    private static int totalTargetCount;

    public static void initStepsProfiles(String filename, double multiplier, int nbSteps) {
        ArrayList<String[]> parameters = CSVReader.read(filename);

        profilePerStep = new ArrayList<>(Collections.nCopies(nbSteps, new HashMap<>()));
        stepTargetCount = new ArrayList<>(Collections.nCopies(nbSteps, 0));

        for (String[] line : parameters) {
            if (isValidAction(line[COLUMN_ACTION])) {
                int step = Integer.parseInt(line[COLUMN_STEP]);
                int count = Integer.parseInt(line[COLUMN_COUNT]);

                if (step < nbSteps) {
                    StepActionProfile actionProfile = new StepActionProfile(step,
                            line[COLUMN_ACTION],
                            Integer.parseInt(line[COLUMN_MONTH]),
                            Integer.parseInt(line[COLUMN_DAY]),
                            Integer.parseInt(line[COLUMN_HOUR]),
                            count,
                            Double.parseDouble(line[COLUMN_SUM]),
                            Double.parseDouble(line[COLUMN_AVERAGE]),
                            Double.parseDouble(line[COLUMN_STD]));

                    profilePerStep.get(step).put(line[COLUMN_ACTION], actionProfile);
                    stepTargetCount.set(step, stepTargetCount.get(step) + count);
                }
            }
        }
        modifyWithMultiplier(multiplier);
        computeProbabilitiesPerStep();
    }

    private static void modifyWithMultiplier(double multiplier) {
        for (int step = 0; step < stepTargetCount.size(); step++) {
            int newMaxCount = Math.toIntExact(Math.round(stepTargetCount.get(step) * multiplier));
            stepTargetCount.set(step, newMaxCount);
        }
        totalTargetCount = stepTargetCount.stream()
                .mapToInt(c -> c)
                .sum();
    }

    private static void computeProbabilitiesPerStep() {
        for (int i = 0; i < profilePerStep.size(); i++) {
            Map<String, StepActionProfile> stepProfile = profilePerStep.get(i);
            int stepCount = stepTargetCount.get(i);

            Map<String, Double> stepProbabilities = stepProfile.entrySet()
                    .stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            c -> ((double) c.getValue().getCount()) / stepCount)
                    );
            probabilitiesPerStep.add(stepProbabilities);
        }
    }

    public static int getTargetCount(int step) {
        return stepTargetCount.get(step);
    }

    public static Map<String, Double> getProbabilitiesPerStep(int step) {
        return probabilitiesPerStep.get(step);
    }

    public static int getTotalTargetCount() {
        return totalTargetCount;
    }

    public static StepActionProfile getActionForStep(int step, String action) {
        return profilePerStep.get(step).get(action);
    }
}