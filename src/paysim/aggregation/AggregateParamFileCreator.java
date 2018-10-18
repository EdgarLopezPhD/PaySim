package paysim.aggregation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

import paysim.Transaction;
import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;

public class AggregateParamFileCreator {
    private static final int DOUBLE_PRECISION = 2;
    private static final int HOURS_IN_DAY = 24, DAYS_IN_MONTH = 30;

    public ArrayList<AggregateTransactionRecord> generateAggregateParamFile(ArrayList<Transaction> transactionList) {
        ArrayList<AggregateTransactionRecord> aggrTransRecord = new ArrayList<>();
        for (String action : TransactionParameters.getActions()) {
            for (int step = 0; step < Parameters.nbSteps; step++) {
                AggregateTransactionRecord partialRecord = getAggregateRecord(action, step, transactionList);
                if (partialRecord != null) {
                    aggrTransRecord.add(partialRecord);
                }
            }
        }
        java.util.Collections.sort(aggrTransRecord);
        return aggrTransRecord;
    }

    /**
     * Using a fast version of getAggregateRecord
     * key property : already sorted Aggregate
     * more appropriate name "aggregate" ?
     */
    public ArrayList<AggregateTransactionRecord> reformat(ArrayList<AggregateTransactionRecord> list) {
        ArrayList<AggregateTransactionRecord> reformedList = new ArrayList<>();
        ArrayList<AggregateTransactionRecord> tempList;

        for (int i = 0; i < list.size(); i++) {
            tempList = new ArrayList<>();
            AggregateTransactionRecord temp = list.get(i);
            tempList.add(temp);
            int counter = i + 1;

            //Get all records looking alike temp
            while (counter < list.size()) {
                AggregateTransactionRecord toCheckRecord = list.get(counter);
                if (temp.equals(toCheckRecord)) {
                    tempList.add(toCheckRecord);
                    counter++;
                } else {
                    break;
                }
            }
            i = counter - 1;
            AggregateTransactionRecord compacted = compactAggrRecord(tempList);
            reformedList.add(compacted);
        }

        return reformedList;
    }

    private AggregateTransactionRecord compactAggrRecord(ArrayList<AggregateTransactionRecord> recordList) {
        String action = recordList.get(0).getAction();
        String month = recordList.get(0).getMonth();
        String day = recordList.get(0).getDay();
        String hour = recordList.get(0).getHour();
        String step = recordList.get(0).getStep();

        double totalCount = recordList.stream()
                .map(AggregateTransactionRecord::getCount)
                .mapToDouble(Double::parseDouble)
                .sum();

        double sum = recordList.stream()
                .map(AggregateTransactionRecord::getSum)
                .mapToDouble(Double::parseDouble)
                .sum();


        double std = recordList.stream()
                .map(AggregateTransactionRecord::getStd)
                .mapToDouble(Double::parseDouble)
                .sum();
        std = std / ((double) recordList.size());
        double avg = sum / totalCount;

        AggregateTransactionRecord compacted =
                new AggregateTransactionRecord(action,
                        month, day, hour,
                        String.valueOf((int) totalCount),
                        String.valueOf(sum),
                        String.valueOf(avg),
                        String.valueOf(std),
                        step);

        return compacted;
    }

    private AggregateTransactionRecord getAggregateRecord(String action, int step, ArrayList<Transaction> transactionList) {
        ArrayList<Transaction> subsetTransList = transactionList.stream()
                .filter(t -> t.getStep() == step && t.getAction().equals(action))
                .collect(Collectors.toCollection(ArrayList::new));

        if (subsetTransList.size() > 0) {
            double sum = getTotalAmount(subsetTransList);
            int count = subsetTransList.size();
            double average = getTruncatedDouble(sum / (double) count);
            double tstd = getTruncatedDouble(getStd(subsetTransList, average));

            int month = step / (DAYS_IN_MONTH * HOURS_IN_DAY);
            int day = (step % (DAYS_IN_MONTH * HOURS_IN_DAY)) / HOURS_IN_DAY;
            int hour = step % HOURS_IN_DAY;

            AggregateTransactionRecord recordToReturn = new AggregateTransactionRecord(String.valueOf(action),
                    String.valueOf(month),
                    String.valueOf(day),
                    String.valueOf(hour),
                    String.valueOf(count),
                    String.valueOf(sum),
                    String.valueOf(average),
                    String.valueOf(tstd),
                    String.valueOf(step));
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

    private double getTotalAmount(ArrayList<Transaction> transactionList) {
        return transactionList.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private double getTruncatedDouble(double d) {
        try {
            return new BigDecimal(d)
                    .setScale(DOUBLE_PRECISION, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }
}