package paysim.utils;

import paysim.PaySim;
import paysim.base.Repetition;
import paysim.Transaction;
import paysim.actors.Fraudster;
import paysim.aggregation.AggregateDumpAnalyzer;
import paysim.aggregation.AggregateDumpHandler;
import paysim.aggregation.AggregateTransactionRecord;
import paysim.parameters.TransactionParameters;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Map;

import static paysim.PaySim.getCumulative;
import static paysim.parameters.TransactionParameters.getCountCallAction;
import static paysim.parameters.TransactionParameters.getCountCallRepetition;

public class Output {
    private static final int PRECISION_OUTPUT = 2;

    public static void writeLog(String logFilename, ArrayList<Transaction> trans) {
        try {
            FileWriter writer1 = new FileWriter(new File(logFilename), true);
            BufferedWriter bufWriter = new BufferedWriter(writer1);
            //Append the logs to the file
            for (Transaction t : trans) {
                bufWriter.write(
                        t.getStep() +
                                "," + t.getType() +
                                "," + formatDouble(PRECISION_OUTPUT, t.getAmount()) +
                                "," + t.getNameOrig() +
                                "," + formatDouble(PRECISION_OUTPUT, t.getOldBalanceOrig()) +
                                "," + formatDouble(PRECISION_OUTPUT, t.getNewBalanceOrig()) +
                                "," + t.getNameDest() +
                                "," + formatDouble(PRECISION_OUTPUT, t.getOldBalanceDest()) +
                                "," + formatDouble(PRECISION_OUTPUT, t.getNewBalanceDest()) +
                                "," + (t.isFraud() ? 1 : 0) +
                                "," + (t.isFlaggedFraud() ? 1 : 0) + "\n");
            }
            bufWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFraudsters(String filenameFraudsters, ArrayList<Fraudster> fraudsters) {
        String header = "fname,numVictims,profit\n";
        try {
            File f = new File(filenameFraudsters);
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(writer);

            bufWriter.write(header);

            for (Fraudster fraud : fraudsters) {
                String row = fraud.getName() + "," + fraud.clientsAffected + "," + fraud.profit + "\n";
                bufWriter.write(row);
            }
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeAggregateParamFile(String filenameOutputAggregate, PaySim simulation) {
        try {
            BufferedWriter paramDump = new BufferedWriter(new FileWriter(new File(filenameOutputAggregate)));
            paramDump.write("type,tmonth,tday,thour,tcount,tsum,tavg,tstd,step\n");

            java.util.Collections.sort(simulation.aggrTransRecordList);

            ArrayList<AggregateTransactionRecord> reformatted = simulation.aggregateCreator.reformat(simulation.aggrTransRecordList);
            java.util.Collections.sort(reformatted);

            for (AggregateTransactionRecord record : reformatted) {
                paramDump.write(record.getType() + "," +
                        record.getMonth() + "," +
                        record.getDay() + "," +
                        record.getHour() + "," +
                        record.getCount() + "," +
                        formatDouble(PRECISION_OUTPUT, Double.parseDouble(record.getSum())) + "," +
                        formatDouble(PRECISION_OUTPUT, Double.parseDouble(record.getAvg())) + "," +
                        formatDouble(PRECISION_OUTPUT, Double.parseDouble(record.getStd())) + "," +
                        record.getStep() + "\n"
                );
            }
            paramDump.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeParamfileHistory(String filenameHistory, PaySim simulation) {
        //TODO : rewrite as a toString of Parameters class
        long totalTime = System.currentTimeMillis() - simulation.startTime;

        try {
            File f = new File(filenameHistory);
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(writer);

            String toWrite = ""; /**
             "nrOfMerchants=" + simulation.nrOfMerchants + "\n" +
             "seed=" + simulation.seed + "\n" +
             "multiplier=" + simulation.getMultiplier() + "\n" +
             "parameterFilePath=" + simulation.parameterFilePath.replace(System.getProperty("user.dir"), "") + "\n" +
             "aggregateParameterFilePath=" + simulation.aggregateParameterPath.replace(System.getProperty("user.dir"), "") + "\n" +
             "transferMaxPath=" + simulation.transferMaxPath.replace(System.getProperty("user.dir"), "") + "\n" +
             "logPath=" + simulation.logPath.replace(System.getProperty("user.dir"), "") + "\n" +
             "balanceHandler=" + simulation.balanceHandlerFilePath.replace(System.getProperty("user.dir"), "") + "\n" +
             "transferFreqMod=" + simulation.transferFreqMod.replace(System.getProperty("user.dir"), "") + "\n" +
             "dbUrl=" + simulation.dbUrl + "\n" +
             "dbUser=" + simulation.dbUser + "\n" +
             "dbPassword=" + simulation.dbPassword + "\n" +
             "fraudProbability=" + simulation.fraudProbability + "\n" +
             "transferLimit=" + simulation.transferLimit + "\n" +
             "numFraudsters=" + simulation.numFraudsters + "\n" +
             Parameters.getFlags();**/
            bufWriter.write(toWrite);
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double writeErrorTable(String filenameParamAggregate, String filenameGeneratedAggregate, String filenameErrorTable) {
        AggregateDumpHandler aggrHandler = new AggregateDumpHandler();
        AggregateDumpAnalyzer analyzerOrig = new AggregateDumpAnalyzer(filenameParamAggregate);
        AggregateDumpAnalyzer analyzerSynth = new AggregateDumpAnalyzer(filenameGeneratedAggregate);
        analyzerOrig.analyze();
        analyzerSynth.analyze();
        String resultDump = aggrHandler.checkDelta(analyzerOrig, analyzerSynth);

        try {
            File f = new File(filenameErrorTable);
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(writer);
            bufWriter.write(resultDump);
            bufWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return aggrHandler.getTotalErrorRate();
    }

    public static void dumpRepetitionFreq(String filename) {
        Map<String, Integer> countPerAction = getCountCallAction();
        Map<Repetition, Integer> countPerRepetition = getCountCallRepetition();

        File f = new File(filename);
        try {
            FileWriter fWriter = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(fWriter);
            bufWriter.write("type,high,low,total,freq" + "\n");

            for (Map.Entry<Repetition, Integer> counterRep : countPerRepetition.entrySet()) {
                Repetition repetition = counterRep.getKey();
                String type = repetition.getType();
                int count = counterRep.getValue();
                int totalAction = countPerAction.get(type);
                double probability = totalAction != 0 ? ((double) count) / totalAction : 0;

                bufWriter.write(type + "," + repetition.getLow() + "," + repetition.getHigh() + ","
                        + count + "," + formatDouble(3, probability)
                        + "\n");
            }
            bufWriter.close();
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSummaryFile(String filenameParams, String filenameGeneratedAggregate, String filenameSummary, PaySim simulation) {
        ArrayList<String> fileContentsOrig = new ArrayList<>();
        try {
            File f = new File(filenameParams);
            FileReader fReader = new FileReader(f);
            BufferedReader reader = new BufferedReader(fReader);
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                fileContentsOrig.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> fileContentsSynth = new ArrayList<>();
        try {
            File f = new File(filenameGeneratedAggregate);
            FileReader fReader = new FileReader(f);
            BufferedReader reader = new BufferedReader(fReader);
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                fileContentsSynth.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        String spaceBegin = "                     ";
        String result = spaceBegin + "Orig" + spaceBegin + "\tSynthetic" + spaceBegin + "\n";
        result += spaceBegin + "Sum" + spaceBegin + "\tSum" + "\n";

        for (String type : TransactionParameters.getActions()) {
            double sumOrig = getCumulative(type, 5, fileContentsOrig);
            double sumSynth = getCumulative(type, 5, fileContentsSynth);
            result += type + spaceBegin.substring(type.length())
                    + formatDouble(PRECISION_OUTPUT, sumOrig) + spaceBegin.substring(String.valueOf(sumOrig).length())
                    + "\t\t" + formatDouble(PRECISION_OUTPUT, sumSynth) + "\n";
        }

        result += "-----------------------------------------------------------------------------\n";
        result += spaceBegin + "Count" + spaceBegin + "\tCount" + spaceBegin + "\n";

        for (String type : TransactionParameters.getActions()) {
            int countOrig = (int) getCumulative(type, 4, fileContentsOrig);
            int countSynth = (int) getCumulative(type, 4, fileContentsSynth);

            simulation.updateTotalTransactionsMade(countSynth);
            result += type + spaceBegin.substring(type.length())
                    + String.valueOf(countOrig) + spaceBegin.substring(String.valueOf(countOrig).length())
                    + "\t\t\t" + String.valueOf(countSynth) + "\n";
        }


        try {
            File f = new File(filenameSummary);
            FileWriter fWriter = new FileWriter(f);
            BufferedWriter writer = new BufferedWriter(fWriter);
            writer.append(result);
            writer.close();
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);

    }

    public static void appendSimulationSummary(String filenameOutput, String summary) {
        //SimulationLogName, NumSteps, totalTransactions, totalError
        String header = "name,steps,totNrOfTransactions,totNrOfClients,totError\n";
        File f = new File(filenameOutput);
        ArrayList<String> allContents = new ArrayList<>();

        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            FileReader fWriter = new FileReader(f);
            BufferedReader bufReader = new BufferedReader(fWriter);
            String line;
            allContents.add(header);
            while ((line = bufReader.readLine()) != null) {
                allContents.add(line + "\n");
            }
            bufReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        allContents.add(summary);
        try {
            FileWriter fWriter = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(fWriter);
            for (String line : allContents) {
                bufWriter.append(line);
            }
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void writeDatabaseLog(String dbUrl, String dbUser, String dbPassword, ArrayList<Transaction> transactions) {
        DatabaseHandler handler = new DatabaseHandler(dbUrl, dbUser, dbPassword);
        for (Transaction t : transactions) {
            handler.insert(t);
        }
    }

    public static String formatDouble(int precision, double d) {
        // See https://stackoverflow.com/questions/2255500/can-i-multiply-strings-in-java-to-repeat-sequences
        // & https://stackoverflow.com/questions/8742645/decimalformat-depending-on-system-settings
        DecimalFormat format = new DecimalFormat("0." + new String(new char[precision]).replace("\0", "0"));
        DecimalFormatSymbols ensureDot = new DecimalFormatSymbols();
        ensureDot.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(ensureDot);
        return format.format(d);
    }
}
