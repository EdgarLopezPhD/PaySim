package paysim.parameters;

import ec.util.MersenneTwisterFast;
import paysim.base.BalanceGenerator;
import paysim.utils.CSVReader;
import paysim.utils.RandomCollection;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class BalanceClients {
    private static final int COLUMN_LOW = 0, COLUMN_HIGH = 1, COLUMN_PROB = 2;

    private static RandomCollection<BalanceGenerator> balanceGeneratorPicker;

    public static void initBalanceClients(String filename, MersenneTwisterFast random) {
        balanceGeneratorPicker = new RandomCollection<>(random);
        ArrayList<String[]> parameters = CSVReader.read(filename);
        for (String[] paramLine : parameters) {
            BalanceGenerator balanceGenerator = new BalanceGenerator(Double.parseDouble(paramLine[COLUMN_LOW]),
                    Double.parseDouble(paramLine[COLUMN_HIGH]));
            balanceGeneratorPicker.add(Double.parseDouble(paramLine[COLUMN_PROB]), balanceGenerator);
        }
    }

    public static double getNextBalance(MersenneTwisterFast random) {
        BalanceGenerator balanceGenerator = balanceGeneratorPicker.next();
        double diff = balanceGenerator.getHigh() - balanceGenerator.getLow();
        double randed = abs(random.nextLong() % diff);

        return balanceGenerator.getLow() + randed;
    }
}
