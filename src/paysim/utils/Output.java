package paysim.utils;

import paysim.PaySim;
import paysim.base.ActionProbability;
import paysim.base.Repetition;
import paysim.base.Transaction;
import paysim.actors.Fraudster;
import paysim.aggregation.AggregateDumpAnalyzer;
import paysim.aggregation.AggregateDumpHandler;
import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Map;

import static paysim.aggregation.AggregateParamFileCreator.generateAggregateParamFile;

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
                                "," + t.getAction() +
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
        String header = "name,nbVictims,profit\n";
        try {
            File f = new File(filenameFraudsters);
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(writer);

            bufWriter.write(header);

            for (Fraudster fraud : fraudsters) {
                String row = fraud.getName() + "," + fraud.getClientsAffected() + "," + fraud.getProfit() + "\n";
                bufWriter.write(row);
            }
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeAggregateStep(String filenameOutputAggregate, int step, ArrayList<Transaction> transactions) {
        Map<String, ActionProbability> stepRecord = generateAggregateParamFile(step, transactions);
        try {
            BufferedWriter paramDump = new BufferedWriter(new FileWriter(new File(filenameOutputAggregate), true));
            if (step == 0) {
                paramDump.write("action,month,day,hour,count,sum,avg,std,step\n");
            }
            for (ActionProbability actionRecord : stepRecord.values()) {
                paramDump.write(actionRecord.getAction() + "," +
                        actionRecord.getMonth() + "," +
                        actionRecord.getDay() + "," +
                        actionRecord.getHour() + "," +
                        actionRecord.getNbTransactions() + "," +
                        formatDouble(PRECISION_OUTPUT, actionRecord.getTotalSum()) + "," +
                        formatDouble(PRECISION_OUTPUT, actionRecord.getAverage()) + "," +
                        formatDouble(PRECISION_OUTPUT, actionRecord.getStd()) + "," +
                        step + "\n"
                );
            }
            paramDump.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeParamfileHistory(String filenameHistory, long seed) {
        try {
            File f = new File(filenameHistory);
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(writer);

            String toWrite = "seed=" + seed + "\n" +
                    "nbSteps=" + Parameters.nbSteps + "\n" +
                    "multiplier=" + Parameters.multiplier + "\n" +
                    "nbFraudsters=" + Parameters.nbFraudsters + "\n" +
                    "nbMerchants=" + Parameters.nbMerchants + "\n" +
                    "fraudProbability=" + Parameters.fraudProbability + "\n" +
                    "transferLimit=" + Parameters.transferLimit + "\n" +
                    "aggregateTransactionsParams=" + Parameters.aggregateTransactionsParams + "\n" +
                    "transferMaxPath=" + Parameters.transferMaxPath + "\n" +
                    "balanceHandler=" + Parameters.balanceHandlerFilePath + "\n" +
                    "transferFreqMod=" + Parameters.transferFreqMod + "\n" +
                    "transferFreqModInit=" + Parameters.transferFreqModInit + "\n" +
                    "outputPath=" + Parameters.outputPath + "\n" +
                    "saveToDB=" + Parameters.saveToDB + "\n" +
                    "dbUrl=" + Parameters.dbUrl + "\n" +
                    "dbUser=" + Parameters.dbUser + "\n" +
                    "dbPassword=" + Parameters.dbPassword + "\n";
            bufWriter.write(toWrite);
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String writeErrorTable(String filenameParamAggregate, String filenameGeneratedAggregate, String filenameErrorTable) {
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

    public static void dumpRepetitionFreq(String filename, Map<String, Integer> countPerAction,
                                          Map<Repetition, Integer> countPerRepetition) {
        File f = new File(filename);
        try {
            FileWriter fWriter = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(fWriter);
            bufWriter.write("action,high,low,total,freq" + "\n");

            for (Map.Entry<Repetition, Integer> counterRep : countPerRepetition.entrySet()) {
                Repetition repetition = counterRep.getKey();
                String action = repetition.getAction();
                int count = counterRep.getValue();
                int totalAction = countPerAction.get(action);
                double probability = totalAction != 0 ? ((double) count) / totalAction : 0;

                bufWriter.write(action + "," + repetition.getLow() + "," + repetition.getHigh() + ","
                        + count + "," + formatDouble(5, probability)
                        + "\n");
            }
            bufWriter.close();
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSummaryFile(String filenameParams, String filenameGeneratedAggregate, String filenameSummary, PaySim simulation) {
        ArrayList<String[]> fileContentsOrig = CSVReader.read(filenameParams);
        ArrayList<String[]> fileContentsSynth = CSVReader.read(filenameGeneratedAggregate);

        String spaceBegin = "                     ";
        String result = spaceBegin + "Orig" + spaceBegin + "\tSynthetic" + spaceBegin + "\n";
        result += spaceBegin + "Sum" + spaceBegin + "\tSum" + "\n";

        for (String action : TransactionParameters.getActions()) {
            double sumOrig = getCumulative(action, 5, fileContentsOrig);
            double sumSynth = getCumulative(action, 5, fileContentsSynth);
            result += action + spaceBegin.substring(action.length())
                    + formatDouble(PRECISION_OUTPUT, sumOrig) + spaceBegin.substring(String.valueOf(sumOrig).length())
                    + "\t\t" + formatDouble(PRECISION_OUTPUT, sumSynth) + "\n";
        }

        result += "-----------------------------------------------------------------------------\n";
        result += spaceBegin + "Count" + spaceBegin + "\tCount" + spaceBegin + "\n";

        for (String action : TransactionParameters.getActions()) {
            int countOrig = (int) getCumulative(action, 4, fileContentsOrig);
            int countSynth = (int) getCumulative(action, 4, fileContentsSynth);

            simulation.updateTotalTransactionsMade(countSynth);
            result += action + spaceBegin.substring(action.length())
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

    private static double getCumulative(String action, int rowIndex, ArrayList<String[]> lines) {
        double aggr = 0;
        for (String[] line : lines) {
            if (line[0].equals(action)) {
                aggr += Double.parseDouble(line[rowIndex]);
            }
        }
        return aggr;
    }

    public static void appendSimulationSummary(String filenameOutput, String summary) {
        String header = "name,steps,nbTransactions,nbClients,totalError\n";
        File f = new File(filenameOutput);
        ArrayList<String> newLines = new ArrayList<>();

        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            newLines.add(header);
        }

        newLines.add(summary);
        try {
            FileWriter fWriter = new FileWriter(f, true);
            BufferedWriter bufWriter = new BufferedWriter(fWriter);
            for (String line : newLines) {
                bufWriter.append(line);
            }
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void writeDatabaseLog(String dbUrl, String dbUser, String dbPassword,
                                        ArrayList<Transaction> transactions, String simulatorName) {
        DatabaseHandler handler = new DatabaseHandler(dbUrl, dbUser, dbPassword);
        for (Transaction t : transactions) {
            handler.insert(simulatorName, t);
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

    public static void createLogFile(String logFileName) {
        try {
            FileWriter writer = new FileWriter(new File(logFileName));
            BufferedWriter bufWriter = new BufferedWriter(writer);
            bufWriter.write("step,action,amount,nameOrig,oldbalanceOrg,newbalanceOrig,nameDest,oldbalanceDest,newbalanceDest,isFraud,isFlaggedFraud\n");
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
