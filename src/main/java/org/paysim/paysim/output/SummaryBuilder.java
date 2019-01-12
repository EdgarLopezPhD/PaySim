package org.paysim.paysim.output;

import java.util.*;
import java.util.function.Function;

import org.paysim.paysim.base.StepActionProfile;
import org.paysim.paysim.parameters.ActionTypes;
import org.paysim.paysim.parameters.StepsProfiles;

class SummaryBuilder {
    private static final String SEPARATOR = "----------------------------------------------------";
    private static final String FORMAT_CELL = "| %-15s";
    private static final List<String> HEADER = Arrays.asList("Estimator", "Action", "Error rate");

    public static double buildSummary(StepsProfiles targetStepsProfiles, StepsProfiles simulationStepsProfiles, StringBuilder summaryStrBuilder) {
        double totalErrorRate = 0;

        summaryStrBuilder.append(SEPARATOR);
        summaryStrBuilder.append(Output.EOL_CHAR);

        buildLineTable(HEADER, summaryStrBuilder);

        totalErrorRate += objectiveFunctionSteps(targetStepsProfiles, simulationStepsProfiles, summaryStrBuilder);

        summaryStrBuilder.append(Output.EOL_CHAR);

        System.out.println(summaryStrBuilder);

        return totalErrorRate;
    }


    private static double computeNRMSE(ArrayList<Double> targetDistribution, ArrayList<Double> simulatedDistribution) {
        if (targetDistribution.size() != simulatedDistribution.size()) {
            throw new IllegalArgumentException("Target & Simulated distributions must be of the same size.");
        }

        int nbDataPoint = targetDistribution.size();
        Double minTarget = targetDistribution
                .stream()
                .min(Double::compareTo)
                .get();
        Double maxTarget = targetDistribution
                .stream()
                .max(Double::compareTo)
                .get();
        double normalizationCoefficient = maxTarget - minTarget;

        double RMSE = 0;
        for (int i = 0; i < nbDataPoint; i++) {
            RMSE += Math.pow(simulatedDistribution.get(i) - targetDistribution.get(i), 2);
        }
        RMSE = Math.sqrt(RMSE / nbDataPoint);

        return RMSE / normalizationCoefficient;
    }

    private static double objectiveFunctionSteps(StepsProfiles targetStepsProfiles, StepsProfiles simulationStepsProfiles, StringBuilder summaryBuilder) {
        Map<String, Function<StepActionProfile, Double>> statExtractor = new HashMap<>();
        Function<StepActionProfile, Integer>  getCount = StepActionProfile::getCount;
        Function<StepActionProfile, Double> getCountDouble = getCount.andThen(Integer::doubleValue);

        statExtractor.put("Average amount", StepActionProfile::getAvgAmount);
        statExtractor.put("Std amount", StepActionProfile::getStdAmount);
        statExtractor.put("Count", getCountDouble);

        double totalNRMSE = 0;
        summaryBuilder.append(SEPARATOR);
        summaryBuilder.append(Output.EOL_CHAR);
        for (String estimator: statExtractor.keySet()) {
            Map<String, ArrayList<Double>> targetSeries = targetStepsProfiles.computeSeries(statExtractor.get(estimator));
            Map<String, ArrayList<Double>> simulationSeries = simulationStepsProfiles.computeSeries(statExtractor.get(estimator));
            for (String action : ActionTypes.getActions()) {
                ArrayList<Double> unitTarget = targetSeries.get(action);
                ArrayList<Double> unitSimulation = simulationSeries.get(action);
                double NRMSE = computeNRMSE(unitTarget, unitSimulation);

                ArrayList<String> errorLine = new ArrayList<>();
                errorLine.add(estimator);
                errorLine.add(action);
                errorLine.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, NRMSE));

                buildLineTable(errorLine, summaryBuilder);

                totalNRMSE += NRMSE;
            }
            summaryBuilder.append(SEPARATOR);
            summaryBuilder.append(Output.EOL_CHAR);
        }

        return totalNRMSE;
    }

    private static void buildLineTable(List<String> line, StringBuilder sb){
        for (String cellContent : line) {
            sb.append(String.format(FORMAT_CELL, cellContent));
        }

        sb.append(FORMAT_CELL.charAt(0));
        sb.append(Output.EOL_CHAR);
    }
}