package paysim.aggregation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import paysim.base.Transaction;
import paysim.base.ActionProbability;

import paysim.parameters.TransactionParameters;

public class AggregateParamFileCreator {
    private static final int DOUBLE_PRECISION = 2;
    private static final int HOURS_IN_DAY = 24, DAYS_IN_MONTH = 30;

    public static Map<String, ActionProbability> generateAggregateParamFile(int step, ArrayList<Transaction> transactionList) {
        Map<String, ActionProbability> stepRecord = new HashMap<>();
        for (String action : TransactionParameters.getActions()) {
            ActionProbability actionRecord = getAggregatedRecord(action, step, transactionList);
            if (actionRecord != null) {
                stepRecord.put(action, actionRecord);
            }
        }
        return stepRecord;
    }

    private static ActionProbability getAggregatedRecord(String action, int step, ArrayList<Transaction> transactionList) {
        ArrayList<Transaction> subsetTransList = transactionList.stream()
                .filter(t -> t.getAction().equals(action))
                .collect(Collectors.toCollection(ArrayList::new));

        if (subsetTransList.size() > 0) {
            double sum = getTotalAmount(subsetTransList);
            int count = subsetTransList.size();
            double average = getTruncatedDouble(sum / (double) count);
            double std = getTruncatedDouble(getStd(subsetTransList, average));

            int month = step / (DAYS_IN_MONTH * HOURS_IN_DAY);
            int day = (step % (DAYS_IN_MONTH * HOURS_IN_DAY)) / HOURS_IN_DAY;
            int hour = step % HOURS_IN_DAY;

            ActionProbability recordToReturn = new ActionProbability(action,
                    month,
                    day,
                    hour,
                    count,
                    sum,
                    average,
                    std);
            return recordToReturn;
        } else {
            return null;
        }

    }

    private static double getStd(ArrayList<Transaction> list, double average) {
        // Bessel corrected deviation https://en.wikipedia.org/wiki/Bessel%27s_correction
        return Math.sqrt(list.stream()
                .map(Transaction::getAmount)
                .map(val -> val - average)
                .mapToDouble(val -> Math.pow(val, 2))
                .sum())
                / (list.size() - 1);
    }

    private static double getTotalAmount(ArrayList<Transaction> transactionList) {
        return transactionList.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private static double getTruncatedDouble(double d) {
        try {
            return new BigDecimal(d)
                    .setScale(DOUBLE_PRECISION, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }
}