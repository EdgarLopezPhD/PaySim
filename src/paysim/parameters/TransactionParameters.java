package paysim.parameters;

import paysim.base.Repetition;
import paysim.utils.CSVReader;
import paysim.utils.RandomCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TransactionParameters {
    private static final int COLUMN_ACTION = 0, COLUMN_PROB = 1, COLUMN_OCCURRENCES = 1;
    private static final int COLUMN_LOW = 1, COLUMN_HIGH = 2, COLUMN_AVG = 3, COLUMN_STD = 4, COLUMN_FREQ = 5;

    private static ArrayList<String> actions = new ArrayList<>();
    private static RandomCollection<String> actionPicker;
    private static ArrayList<Integer> maxOccurrencesPerAction = new ArrayList<>();
    private static ArrayList<RandomCollection<Repetition>> repetitionPickerPerAction = new ArrayList<>();

    private static Map<String, Integer> countCallAction = new HashMap<>();
    private static Map<Repetition, Integer> countCallRepetition = new HashMap<>();

    public static void loadTransferFreqModInit(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);
        // TODO : check what type of Random management do we want
        actionPicker = new RandomCollection<>(new Random(Parameters.seed));
        for (String[] paramLine : parameters) {
            String action = paramLine[COLUMN_ACTION];
            actions.add(action);
            actionPicker.add(Double.parseDouble(paramLine[COLUMN_PROB]), action);
            countCallAction.put(action, 0);
        }

        // Prepare the other fields accordingly to what has been loaded
        maxOccurrencesPerAction = new ArrayList<>(actions.size());
        while (maxOccurrencesPerAction.size() < actions.size()) {
            maxOccurrencesPerAction.add(0);
        }

        repetitionPickerPerAction = new ArrayList<>(actions.size());
        while (repetitionPickerPerAction.size() < actions.size()) {
            // TODO : check what type of Random management do we want
            repetitionPickerPerAction.add(new RandomCollection<>(new Random(Parameters.seed)));
        }
    }

    public static void loadTransferMax(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);
        int loaded = 0;
        for (String[] paramLine : parameters) {
            if (isValidAction(paramLine[COLUMN_ACTION])) {
                maxOccurrencesPerAction.set(indexOf(paramLine[COLUMN_ACTION]), Integer.parseInt(paramLine[COLUMN_OCCURRENCES]));
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
                RandomCollection<Repetition> repetitionGetter = repetitionPickerPerAction.get(indexOf(repetitionString[COLUMN_ACTION]));
                Repetition repetition = new Repetition(repetitionString[COLUMN_ACTION],
                        Double.parseDouble(repetitionString[COLUMN_LOW]),
                        Double.parseDouble(repetitionString[COLUMN_HIGH]),
                        Double.parseDouble(repetitionString[COLUMN_AVG]),
                        Double.parseDouble(repetitionString[COLUMN_STD]));
                repetitionGetter.add(Double.parseDouble(repetitionString[COLUMN_FREQ]), repetition);
                countCallRepetition.put(repetition, 0);
            }
        }
    }

    public static int getMaxOccurrenceGivenAction(String action) {
        return maxOccurrencesPerAction.get(indexOf(action));
    }

    public static boolean isValidAction(String name) {
        return actions.contains(name);
    }

    public static int indexOf(String action) {
        return actions.indexOf(action);
    }

    public static String getAction(int id) {
        return actions.get(id);
    }

    public static ArrayList<String> getActions() {
        return actions;
    }

    private static String getNextAction() {
        String action = actionPicker.next();
        int count = countCallAction.get(action);
        countCallAction.put(action, count + 1);
        return action;
    }

    private static Repetition getNextRepetition(String action) {
        return repetitionPickerPerAction.get(indexOf(action)).next();
    }

    public static Repetition getRepetition() {
        String action = TransactionParameters.getNextAction();
        Repetition repetition = TransactionParameters.getNextRepetition(action);
        int count = countCallRepetition.get(repetition);
        countCallRepetition.put(repetition, count + 1);
        return repetition;
    }

    public static Map<String, Integer> getCountCallAction() {
        return countCallAction;
    }

    public static Map<Repetition, Integer> getCountCallRepetition() {
        return countCallRepetition;
    }
}
