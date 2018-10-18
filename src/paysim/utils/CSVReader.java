package paysim.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVReader {
    private static final String SPLIT_BY = ",";

    public static ArrayList<String[]> read(String csvFile) {
        ArrayList<String[]> csvContent = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                csvContent.add(line.split(SPLIT_BY));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvContent;
    }
}
