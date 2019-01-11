package org.paysim.paysim.parameters;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.NavigableMap;
import java.util.TreeMap;

import ec.util.MersenneTwisterFast;

import org.paysim.paysim.utils.CSVReader;
import org.paysim.paysim.utils.RandomCollection;

public class BalancesClients {
    private static final int COLUMN_LOW = 0, COLUMN_HIGH = 1, COLUMN_PROB = 2;
    private static final int COLUMN_OVERDRAFT_LIMIT = 2;

    private static RandomCollection<ArrayList<Double>> balanceRangePicker;
    private static final NavigableMap<Double, Double> overdraftLimits = new TreeMap<>();

    public static void initBalanceClients(String filename) {
        balanceRangePicker = new RandomCollection<>();
        ArrayList<String[]> parameters = CSVReader.read(filename);
        for (String[] paramLine : parameters) {
            ArrayList<Double> balanceRange = new ArrayList<>();
            balanceRange.add(Double.parseDouble(paramLine[COLUMN_LOW]));
            balanceRange.add(Double.parseDouble(paramLine[COLUMN_HIGH]));

            balanceRangePicker.add(Double.parseDouble(paramLine[COLUMN_PROB]), balanceRange);
        }
    }

    public static void initOverdraftLimits(String filename){
        ArrayList<String[]> parameters = CSVReader.read(filename);
        double valueLow, valueHigh;
        double lastValueHigh = - Double.MAX_VALUE;

        for (String[] paramLine : parameters) {
            if (paramLine[COLUMN_LOW].length() == 0) {
                valueLow = - Double.MAX_VALUE;
            } else {
                valueLow = Double.parseDouble(paramLine[COLUMN_LOW]);
            }
            if (paramLine[COLUMN_HIGH].length() == 0){
                valueHigh = Double.MAX_VALUE;
            } else {
                valueHigh = Double.parseDouble(paramLine[COLUMN_HIGH]);
            }
            if (valueLow > valueHigh){
                throw new InputMismatchException(String.format("A range should be strictly increasing: %.2f > %.2f", valueLow, valueHigh));
            }
            if (valueLow != lastValueHigh){
                throw new InputMismatchException("Ranges should be a partition of R and provided in increasing lower bound order.");
            }

            overdraftLimits.put(valueLow, Double.parseDouble(paramLine[COLUMN_OVERDRAFT_LIMIT]));
            lastValueHigh = valueHigh;
        }
        if (lastValueHigh != Double.MAX_VALUE){
            throw new InputMismatchException("The last range should not have an upper bound.");
        }
    }

    public static double pickNextBalance(MersenneTwisterFast random) {
        ArrayList<Double> balanceRange = balanceRangePicker.next();
        double rangeSize = balanceRange.get(COLUMN_HIGH) - balanceRange.get(COLUMN_LOW);

        return balanceRange.get(COLUMN_LOW) + random.nextDouble() * rangeSize;
    }

    public static double getOverdraftLimit(double meanTransaction){
        return overdraftLimits.floorEntry(meanTransaction).getValue();
    }

    public static void setRandom(MersenneTwisterFast random) {
        balanceRangePicker.setRandom(random);
    }
}
