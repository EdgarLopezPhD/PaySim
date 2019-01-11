package org.paysim.paysim.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVReader {
    private static final String CSV_SEPARATOR = ",";

    public static ArrayList<String[]> read(String csvFile) {
        ArrayList<String[]> csvContent = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                csvContent.add(line.split(CSV_SEPARATOR));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvContent;
    }
}
