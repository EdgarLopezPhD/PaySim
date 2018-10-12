package paysim.aggregation;

import java.text.DecimalFormat;

import static java.lang.Math.abs;

public class AggregateDumpHandler {
    private double totalErrorRate;
    private String resultDump;

    public AggregateDumpHandler() {
        totalErrorRate = 0;
        resultDump = "";
    }

    public String checkDelta(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated) {
        DecimalFormat format = new DecimalFormat("#.###");
        String separator = "---------------------------------------------------------------------------------------------------------";
        resultDump += separator + "\n";
        resultDump += "|\tIndicator\t|\tOrig\t|\tSynth\t|\tError Rate\t|\n";
        resultDump += separator + "\n";
        resultDump += "|\tNR OF TRANS\t|\t\t|\t\t|\t\t\t|\n";
        resultDump += "|";

        addResultOfTotalTransaction(orig, generated);
        resultDump += separator + "\n";
        resultDump += "|\tAVG TRANS SIZE\t|\t\t\t|\t\t\t|\t\t\t\t|\n";
        addResultOfAvgTransaction(orig, generated);
        resultDump += separator + "\n";
        resultDump += "|\tTOT ERR RATE\t|\t\t\t|\t\t\t|\t" + format.format(totalErrorRate) + "\t\t\t|\n";
        resultDump += separator + "\n";

        System.out.println(resultDump + "\n");

        return resultDump;
    }

    private void addResultOfTotalTransaction(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated) {
        formatResult(orig.getTotNrOfCashIn(), generated.getTotNrOfCashIn(), "CASH_IN");
        formatResult(orig.getTotNrOfCashOut(), generated.getTotNrOfCashOut(), "CASH_OUT");
        formatResult(orig.getTotNrOfTransfer(), generated.getTotNrOfTransfer(), "TRANS");
        formatResult(orig.getTotNrOfPayments(), generated.getTotNrOfPayments(), "PAYM");
        formatResult(orig.getTotNrOfDebit(), generated.getTotNrOfDebit(), "DEB");
    }

    private void addResultOfAvgTransaction(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated) {
        formatResult(orig.getAvgAvgCashIn(), generated.getAvgAvgCashIn(), "CASH_IN");
        formatResult(orig.getAvgAvgCashOut(), generated.getAvgAvgCashOut(), "CASH_OUT");
        formatResult(orig.getAvgAvgTransfer(), generated.getAvgAvgTransfer(), "TRANS");
        formatResult(orig.getAvgAvgPayments(), generated.getAvgAvgPayments(), "PAYM");
        formatResult(orig.getAvgAvgDebit(), generated.getAvgAvgDebit(), "DEB");
    }

    private void formatResult(double valueOrig, double valueGenerated, String type) {
        DecimalFormat format = new DecimalFormat("#.###");
        double errorRate = abs((valueOrig - valueGenerated) / valueOrig);
        totalErrorRate += errorRate;
        resultDump += "|\t" + type + "\t\t|\t" + format.format(valueOrig) + "\t|\t" + format.format(valueGenerated) +
                "\t|\t" + format.format(errorRate) + "\t\t\t|" + "\n";
    }

    public double getTotalErrorRate() {
        return totalErrorRate;
    }
}