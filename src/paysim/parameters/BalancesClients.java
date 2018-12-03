package paysim.parameters;

import java.util.ArrayList;

import ec.util.MersenneTwisterFast;

import paysim.utils.CSVReader;
import paysim.utils.RandomCollection;

public class BalancesClients {
    private static final int COLUMN_LOW = 0, COLUMN_HIGH = 1, COLUMN_PROB = 2;

    private static RandomCollection<ArrayList<Double>> balanceRangePicker;

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

    public static double getNextBalance(MersenneTwisterFast random) {
        ArrayList<Double> balanceRange = balanceRangePicker.next();
        double rangeSize = balanceRange.get(COLUMN_HIGH) - balanceRange.get(COLUMN_LOW);

        return balanceRange.get(COLUMN_LOW) + random.nextDouble() * rangeSize;
    }

    public static void setRandom(MersenneTwisterFast random) {
        balanceRangePicker.setRandom(random);
    }
}
