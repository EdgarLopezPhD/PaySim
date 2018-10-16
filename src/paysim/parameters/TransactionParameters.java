package paysim.parameters;

import paysim.utils.CSVReader;

import java.util.ArrayList;

public class TransactionParameters {
    private static ArrayList<String> types = new ArrayList<>();
    private static ArrayList<Double> probabilities = new ArrayList<>();
    private static ArrayList<Integer> maxOccurrences = new ArrayList<>();
    private static int COLUMN_TYPE = 0, COLUMN_PROB = 1, COLUMN_OCCURENCES = 1;

    public static void loadTransferFreq4ModInit(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);
        for (String[] paramLine : parameters) {
            types.add(paramLine[COLUMN_TYPE]);
            probabilities.add(Double.parseDouble(paramLine[COLUMN_PROB]));
        }
        maxOccurrences = new ArrayList<>(types.size());
        while(maxOccurrences.size() < types.size()) maxOccurrences.add(0);
    }

    public static void loadTransferMax(String filename){
        ArrayList<String[]> parameters = CSVReader.read(filename);
        int loaded = 0;
        for (String[] paramLine : parameters) {
            if (isValidType(paramLine[COLUMN_TYPE])) {
                maxOccurrences.set(indexOf(paramLine[COLUMN_TYPE]), Integer.parseInt(paramLine[COLUMN_OCCURENCES]));
                loaded++;
            }
        }
        if (loaded != types.size()){
            System.out.println("Warning : Missing type of transactions in " + filename);
        }
    }

    public static int getMaxOccurenceGivenType(String type){
        return maxOccurrences.get(indexOf(type));
    }
    public static boolean isValidType(String name) {
        return types.contains(name);
    }

    public static int indexOf(String type){
        return types.indexOf(type);
    }

    public static String getType(int id){
        return types.get(id);
    }

    public static ArrayList<String> getTypes() {
        return types;
    }

    public static ArrayList<Double> getProbabilities() {
        return probabilities;
    }

    public static ArrayList<Integer> getMaxOccurrences() {
        return maxOccurrences;
    }
}
