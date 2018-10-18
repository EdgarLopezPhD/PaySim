package paysim.aggregation;

import static java.lang.Math.abs;
import static paysim.utils.Output.formatDouble;

public class AggregateDumpHandler {
    private double totalErrorRate;
    private String resultDump;

    public AggregateDumpHandler() {
        totalErrorRate = 0;
        resultDump = "";
    }

    public String checkDelta(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated) {
        String separator = "---------------------------------------------------------------------------------------------------------";
        resultDump += separator + "\n";
        resultDump += "|\tIndicator\t|\tOrig\t|\tSynth\t|\tError Rate\t|\n";
        resultDump += separator + "\n";
        resultDump += "|\tNR OF TRANS\t|\t\t|\t\t|\t\t\t|\n";

        addResultOfTotalTransaction(orig, generated);
        resultDump += separator + "\n";
        resultDump += "|\tAVG TRANS SIZE\t|\t\t\t|\t\t\t|\t\t\t\t|\n";
        addResultOfAvgTransaction(orig, generated);
        resultDump += separator + "\n";
        resultDump += "|\tTOT ERR RATE\t|\t\t\t|\t\t\t|\t" + formatDouble(3, totalErrorRate) + "\t\t\t|\n";
        resultDump += separator + "\n";

        System.out.println(resultDump + "\n");

        return resultDump;
    }

    private void addResultOfTotalTransaction(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated) {
        formatResult(orig.getTotNbCashIn(), generated.getTotNbCashIn(), "CASH_IN");
        formatResult(orig.getTotNbCashOut(), generated.getTotNbCashOut(), "CASH_OUT");
        formatResult(orig.getTotNbTransfer(), generated.getTotNbTransfer(), "TRANS");
        formatResult(orig.getTotNbPayments(), generated.getTotNbPayments(), "PAYM");
        formatResult(orig.getTotNbDebit(), generated.getTotNbDebit(), "DEB");
    }

    private void addResultOfAvgTransaction(AggregateDumpAnalyzer orig, AggregateDumpAnalyzer generated) {
        formatResult(orig.getAvgAvgCashIn(), generated.getAvgAvgCashIn(), "CASH_IN");
        formatResult(orig.getAvgAvgCashOut(), generated.getAvgAvgCashOut(), "CASH_OUT");
        formatResult(orig.getAvgAvgTransfer(), generated.getAvgAvgTransfer(), "TRANS");
        formatResult(orig.getAvgAvgPayments(), generated.getAvgAvgPayments(), "PAYM");
        formatResult(orig.getAvgAvgDebit(), generated.getAvgAvgDebit(), "DEB");
    }

    private void formatResult(double valueOrig, double valueGenerated, String action) {
        double errorRate = abs((valueOrig - valueGenerated) / valueOrig);
        totalErrorRate += errorRate;
        resultDump += "|\t" + action + "\t\t|\t" + formatDouble(3, valueOrig) + "\t|\t" + formatDouble(3, valueGenerated) +
                "\t|\t" + formatDouble(3, errorRate) + "\t\t\t|" + "\n";
    }

    public String getTotalErrorRate() {
        return formatDouble(3, totalErrorRate);
    }
}