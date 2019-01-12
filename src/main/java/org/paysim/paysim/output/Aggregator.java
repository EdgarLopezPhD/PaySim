package org.paysim.paysim.output;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.paysim.paysim.parameters.ActionTypes;

import org.paysim.paysim.base.Transaction;
import org.paysim.paysim.base.StepActionProfile;

class Aggregator {
    private static final int DOUBLE_PRECISION = 2;
    private static final int HOURS_IN_DAY = 24, DAYS_IN_MONTH = 30;

    public static Map<String, StepActionProfile> generateStepAggregate(int step, ArrayList<Transaction> transactionList) {
        Map<String, StepActionProfile> stepRecord = new HashMap<>();
        for (String action : ActionTypes.getActions()) {
            StepActionProfile actionRecord = getAggregatedRecord(action, step, transactionList);
            if (actionRecord != null) {
                stepRecord.put(action, actionRecord);
            }
        }
        return stepRecord;
    }

    private static StepActionProfile getAggregatedRecord(String action, int step, ArrayList<Transaction> transactionsList) {
        ArrayList<Transaction> actionTransactionsList = transactionsList.stream()
                .filter(t -> t.getAction().equals(action))
                .filter(t -> !t.isFailedTransaction())
                .collect(Collectors.toCollection(ArrayList::new));

        if (actionTransactionsList.size() > 0) {
            double sum = computeTotalAmount(actionTransactionsList);
            int count = actionTransactionsList.size();
            double average = getTruncatedDouble(sum / (double) count);
            double std = getTruncatedDouble(computeStd(actionTransactionsList, average));

            int month = step / (DAYS_IN_MONTH * HOURS_IN_DAY);
            int day = (step % (DAYS_IN_MONTH * HOURS_IN_DAY)) / HOURS_IN_DAY;
            int hour = step % HOURS_IN_DAY;

            return new StepActionProfile(step,
                    action,
                    month,
                    day,
                    hour,
                    count,
                    sum,
                    average,
                    std);
        } else {
            return null;
        }

    }

    private static double computeStd(ArrayList<Transaction> list, double average) {
        // Bessel corrected deviation https://en.wikipedia.org/wiki/Bessel%27s_correction
        return Math.sqrt(list.stream()
                .map(Transaction::getAmount)
                .map(val -> val - average)
                .mapToDouble(val -> Math.pow(val, 2))
                .sum())
                / (list.size() - 1);
    }

    private static double computeTotalAmount(ArrayList<Transaction> transactionList) {
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