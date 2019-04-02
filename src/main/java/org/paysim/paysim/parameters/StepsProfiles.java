package org.paysim.paysim.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.paysim.paysim.utils.CSVReader;

import org.paysim.paysim.base.StepActionProfile;


public class StepsProfiles {
    private static final int COLUMN_ACTION = 0, COLUMN_MONTH = 1, COLUMN_DAY = 2, COLUMN_HOUR = 3, COLUMN_COUNT = 4,
            COLUMN_SUM = 5, COLUMN_AVERAGE = 6, COLUMN_STD = 7, COLUMN_STEP = 8;
    private ArrayList<HashMap<String, StepActionProfile>> profilePerStep;
    private ArrayList<Map<String, Double>> probabilitiesPerStep = new ArrayList<>();
    private ArrayList<Integer> stepTargetCount;
    private int totalTargetCount;

    public StepsProfiles(String filename, double multiplier, int nbSteps) {
        ArrayList<String[]> parameters = CSVReader.read(filename);

        profilePerStep = new ArrayList<>();
        for (int i = 0; i < nbSteps; i++) {
            profilePerStep.add(new HashMap<>());
        }

        stepTargetCount = new ArrayList<>(Collections.nCopies(nbSteps, 0));

        for (String[] line : parameters) {
            if (ActionTypes.isValidAction(line[COLUMN_ACTION])) {
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


        computeProbabilitiesPerStep();
        modifyWithMultiplier(multiplier);
    }

    private void modifyWithMultiplier(double multiplier) {
        for (int step = 0; step < stepTargetCount.size(); step++) {
            int newMaxCount = Math.toIntExact(Math.round(stepTargetCount.get(step) * multiplier));
            stepTargetCount.set(step, newMaxCount);
        }
        totalTargetCount = stepTargetCount.stream()
                .mapToInt(c -> c)
                .sum();
    }

    private void computeProbabilitiesPerStep() {
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

    public int getTargetCount(int step) {
        return stepTargetCount.get(step);
    }

    public Map<String, Double> getProbabilitiesPerStep(int step) {
        return probabilitiesPerStep.get(step);
    }

    public int getTotalTargetCount() {
        return totalTargetCount;
    }

    public StepActionProfile getActionForStep(int step, String action) {
        return profilePerStep.get(step).get(action);
    }

    public Map<String, ArrayList<Double>> computeSeries(Function<StepActionProfile, Double> getter) {
        Map<String, ArrayList<Double>> series = new HashMap<>();
        for (String action : ActionTypes.getActions()) {
            series.put(action, new ArrayList<>());
        }

        for (Map<String, StepActionProfile> profileStep : profilePerStep) {
            for (String action : ActionTypes.getActions()) {
                if (profileStep.containsKey(action)) {
                    series.get(action).add(getter.apply(profileStep.get(action)));
                } else {
                    series.get(action).add(0d);
                }
            }
        }
        return series;
    }
}