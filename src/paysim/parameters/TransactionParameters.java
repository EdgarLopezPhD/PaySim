package paysim.parameters;

import paysim.base.Repetition;
import paysim.utils.CSVReader;
import paysim.utils.RandomCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collection;


public class TransactionParameters {
    private static final int COLUMN_ACTION = 0, COLUMN_PROB = 1, COLUMN_OCCURRENCES = 1;
    private static final int COLUMN_LOW = 1, COLUMN_HIGH = 2, COLUMN_AVG = 3, COLUMN_STD = 4, COLUMN_FREQ = 5;

    private static Set<String> actions = new TreeSet<>();
    private static RandomCollection<String> actionPicker;
    private static Map<String, Integer> maxOccurrencesPerAction = new HashMap<>();
    private static Map<String, RandomCollection<Repetition>> repetitionPickerPerAction = new HashMap<>();

    public static void loadTransferFreqModInit(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);
        // TODO : check what type of Random management do we want
        actionPicker = new RandomCollection<>(new Random(Parameters.getSeed()));
        for (String[] paramLine : parameters) {
            String action = paramLine[COLUMN_ACTION];
            actions.add(action);
            actionPicker.add(Double.parseDouble(paramLine[COLUMN_PROB]), action);
        }

        for (String action : actions) {
            // TODO : check what type of Random management do we want
            repetitionPickerPerAction.put(action, new RandomCollection<>(new Random(Parameters.getSeed())));
        }
    }

    public static void loadTransferMax(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);
        int loaded = 0;
        for (String[] paramLine : parameters) {
            if (isValidAction(paramLine[COLUMN_ACTION])) {
                maxOccurrencesPerAction.put(paramLine[COLUMN_ACTION], Integer.parseInt(paramLine[COLUMN_OCCURRENCES]));
                loaded++;
            }
        }
        if (loaded != actions.size()) {
            System.out.println("Warning : Missing action in " + filename);
        }
    }

    public static void loadTransferFreqMod(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);
        for (String[] repetitionString : parameters) {
            if (isValidAction(repetitionString[COLUMN_ACTION])) {
                RandomCollection<Repetition> repetitionGetter = repetitionPickerPerAction.get(repetitionString[COLUMN_ACTION]);
                Repetition repetition = new Repetition(repetitionString[COLUMN_ACTION],
                        Integer.parseInt(repetitionString[COLUMN_LOW]),
                        Integer.parseInt(repetitionString[COLUMN_HIGH]),
                        Double.parseDouble(repetitionString[COLUMN_AVG]),
                        Double.parseDouble(repetitionString[COLUMN_STD]));
                repetitionGetter.add(Double.parseDouble(repetitionString[COLUMN_FREQ]), repetition);
            }
        }
    }

    public static int getMaxOccurrenceGivenAction(String action) {
        return maxOccurrencesPerAction.get(action);
    }

    public static boolean isValidAction(String name) {
        return actions.contains(name);
    }

    public static Set<String> getActions() {
        return actions;
    }

    public static Collection<Repetition> getRepetitionsFromAction(String action){
        return repetitionPickerPerAction.get(action).getCollection();
    }

    public static Repetition pickNextRepetition(String action) {
        return repetitionPickerPerAction.get(action).next();
    }

    public static String pickNextAction() {
        return actionPicker.next();
    }
}
