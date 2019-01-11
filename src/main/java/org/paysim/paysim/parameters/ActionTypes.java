package org.paysim.paysim.parameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;

import org.paysim.paysim.utils.CSVReader;

public class ActionTypes {
    private static final int COLUMN_ACTION = 0, COLUMN_OCCURRENCES = 1;
    private static Set<String> actions = new TreeSet<>();
    private static Map<String, Integer> maxOccurrencesPerAction = new HashMap<>();

    public static void loadActionTypes(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);

        for (String[] paramLine : parameters) {
            String action = paramLine[COLUMN_ACTION];
            actions.add(action);
        }
    }

    public static void loadMaxOccurrencesPerClient(String filename) {
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

    public static int getMaxOccurrenceGivenAction(String action) {
        return maxOccurrencesPerAction.get(action);
    }

    public static boolean isValidAction(String name) {
        return actions.contains(name);
    }

    public static Set<String> getActions() {
        return actions;
    }

}
