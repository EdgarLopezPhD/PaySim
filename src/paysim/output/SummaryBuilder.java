package paysim.output;

import java.util.ArrayList;
import java.util.Map;
import static java.lang.Math.abs;

class SummaryBuilder {
    private static final String separator = "---------------------------------------------------------------------";

    public static double buildSummary(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer simulation, StringBuilder summaryStrBuilder) {
        double totalErrorRate = 0;

        summaryStrBuilder.append(separator);
        summaryStrBuilder.append(System.lineSeparator());

        summaryStrBuilder.append("|\tIndicator\t|\tOrig\t|\tSynth\t|\tError Rate\t|");
        summaryStrBuilder.append(System.lineSeparator());


        summaryStrBuilder.append(separator);
        summaryStrBuilder.append(System.lineSeparator());

        summaryStrBuilder.append(String.format("| %-15s", "NB TRANSAC"));
        summaryStrBuilder.append(System.lineSeparator());

        Map<String, Double> origCounts = orig.computeCounts();
        Map<String, Double> simulationCounts = simulation.computeCounts();

        totalErrorRate += buildErrorSummary(origCounts, simulationCounts, summaryStrBuilder);


        summaryStrBuilder.append(separator);
        summaryStrBuilder.append(System.lineSeparator());

        summaryStrBuilder.append(String.format("| %-15s", "AVG AMOUNT"));
        summaryStrBuilder.append(System.lineSeparator());

        Map<String, Double> origAvgs = orig.computeAvgAvg();
        Map<String, Double> simulationAvgs = simulation.computeAvgAvg();

        totalErrorRate += buildErrorSummary(origAvgs, simulationAvgs, summaryStrBuilder);


        summaryStrBuilder.append(separator);
        summaryStrBuilder.append(System.lineSeparator());

        summaryStrBuilder.append((String.format("|TOT ERR RATE %-60s",  Output.fastFormatDouble(2, totalErrorRate))));
        summaryStrBuilder.append(System.lineSeparator());


        summaryStrBuilder.append(separator);
        summaryStrBuilder.append(System.lineSeparator());
        summaryStrBuilder.append(System.lineSeparator());

        System.out.println(summaryStrBuilder);

        return totalErrorRate;
    }

    private static double buildErrorSummary(Map<String, Double> orig, Map<String, Double> simulation, StringBuilder summaryBuilder) {
        double totalErrorRate = 0;
        for (Map.Entry<String, Double> origActionCount : orig.entrySet()) {
            ArrayList<String> summaryLine = new ArrayList<>();

            String action = origActionCount.getKey();
            double origValue = origActionCount.getValue();
            double simulationValue = simulation.get(action);
            double errorRate = computeRelativeError(origValue, simulationValue);

            summaryLine.add(action);
            summaryLine.add(Output.fastFormatDouble(2, origValue));
            summaryLine.add(Output.fastFormatDouble(2, simulationValue));
            summaryLine.add(Output.fastFormatDouble(2, errorRate));

            for (String s : summaryLine) {
                summaryBuilder.append(String.format("| %-15s", s));
            }

            summaryBuilder.append("|");
            summaryBuilder.append(System.lineSeparator());
            totalErrorRate += errorRate;
        }
        return totalErrorRate;
    }

    private static double computeRelativeError(double valueOrig, double valueSimulation){
        return abs((valueOrig - valueSimulation) / valueOrig);
    }
}