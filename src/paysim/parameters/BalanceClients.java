package paysim.parameters;

import paysim.PaySim;
import paysim.base.BalanceGenerator;
import paysim.utils.CSVReader;
import paysim.utils.RandomCollection;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

public class BalanceClients {
    private static final int COLUMN_LOW = 0, COLUMN_HIGH = 1, COLUMN_PROB = 2;

    private static RandomCollection<BalanceGenerator> balanceGeneratorPicker;

    public static void initBalanceClients(String filename) {
        // TODO : check what type of Random management do we want
        balanceGeneratorPicker = new RandomCollection<>(new Random(Parameters.getSeed()));
        ArrayList<String[]> parameters = CSVReader.read(filename);
        for (String[] paramLine : parameters) {
            BalanceGenerator balanceGenerator = new BalanceGenerator(Double.parseDouble(paramLine[COLUMN_LOW]),
                    Double.parseDouble(paramLine[COLUMN_HIGH]));
            balanceGeneratorPicker.add(Double.parseDouble(paramLine[COLUMN_PROB]), balanceGenerator);
        }
    }

    public static double getBalance(PaySim paySim) {
        BalanceGenerator balanceGenerator = balanceGeneratorPicker.next();
        double diff = balanceGenerator.getHigh() - balanceGenerator.getLow();
        double randed = abs(paySim.random.nextLong() % diff);

        return balanceGenerator.getLow() + randed;
    }
}
