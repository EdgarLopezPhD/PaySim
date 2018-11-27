package paysim.parameters;

import ec.util.MersenneTwisterFast;
import paysim.base.ClientActionProfile;
import paysim.utils.CSVReader;
import paysim.utils.RandomCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collection;


public class TransactionParameters {
    private static final int COLUMN_ACTION = 0, COLUMN_PROB = 1, COLUMN_OCCURRENCES = 1;
    private static final int COLUMN_LOW = 1, COLUMN_HIGH = 2, COLUMN_AVG = 3, COLUMN_STD = 4, COLUMN_FREQ = 5;

    private static Set<String> actions = new TreeSet<>();
    private static RandomCollection<String> actionPicker;
    private static Map<String, Integer> maxOccurrencesPerAction = new HashMap<>();
    private static Map<String, RandomCollection<ClientActionProfile>> profilePickerPerAction = new HashMap<>();

    public static void loadTransferFreqModInit(String filename, MersenneTwisterFast random) {
        ArrayList<String[]> parameters = CSVReader.read(filename);

        actionPicker = new RandomCollection<>(random);
        for (String[] paramLine : parameters) {
            String action = paramLine[COLUMN_ACTION];
            double probability = Double.parseDouble(paramLine[COLUMN_PROB]);
            if (probability > 0) {
                actions.add(action);
                actionPicker.add(Double.parseDouble(paramLine[COLUMN_PROB]), action);
            }
        }

        for (String action : actions) {
            profilePickerPerAction.put(action, new RandomCollection<>(random));
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
        for (String[] profileString : parameters) {
            if (isValidAction(profileString[COLUMN_ACTION])) {
                RandomCollection<ClientActionProfile> profileGetter = profilePickerPerAction.get(profileString[COLUMN_ACTION]);
                ClientActionProfile clientActionProfile = new ClientActionProfile(profileString[COLUMN_ACTION],
                        Integer.parseInt(profileString[COLUMN_LOW]),
                        Integer.parseInt(profileString[COLUMN_HIGH]),
                        Double.parseDouble(profileString[COLUMN_AVG]),
                        Double.parseDouble(profileString[COLUMN_STD]));
                profileGetter.add(Double.parseDouble(profileString[COLUMN_FREQ]), clientActionProfile);
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

    public static Collection<ClientActionProfile> getProfilesFromAction(String action) {
        return profilePickerPerAction.get(action).getCollection();
    }

    public static ClientActionProfile pickNextProfile(String action) {
        return profilePickerPerAction.get(action).next();
    }
}
