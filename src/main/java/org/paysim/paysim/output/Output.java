package org.paysim.paysim.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.paysim.paysim.PaySim;
import org.paysim.paysim.base.StepActionProfile;
import org.paysim.paysim.base.ClientActionProfile;
import org.paysim.paysim.base.Transaction;
import org.paysim.paysim.actors.Fraudster;
import org.paysim.paysim.parameters.Parameters;
import org.paysim.paysim.parameters.StepsProfiles;
import org.paysim.paysim.utils.DatabaseHandler;

public class Output {
    public static final int PRECISION_OUTPUT = 2;
    public static final String OUTPUT_SEPARATOR = ",", EOL_CHAR = System.lineSeparator();
    private static String filenameGlobalSummary, filenameParameters, filenameSummary, filenameRawLog,
            filenameStepAggregate, filenameClientProfiles, filenameFraudsters;

    public static void incrementalWriteRawLog(int step, ArrayList<Transaction> transactions) {
        String rawLogHeader = "step,action,amount,nameOrig,oldBalanceOrig,newBalanceOrig,nameDest,oldBalanceDest,newBalanceDest,isFraud,isFlaggedFraud,isUnauthorizedOverdraft";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameRawLog, true));
            if (step == 0) {
                writer.write(rawLogHeader);
                writer.newLine();
            }
            for (Transaction t : transactions) {
                writer.write(t.toString());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void incrementalWriteStepAggregate(int step, ArrayList<Transaction> transactions) {
        String stepAggregateHeader = "action,month,day,hour,count,sum,avg,std,step";
        Map<String, StepActionProfile> stepRecord = Aggregator.generateStepAggregate(step, transactions);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameStepAggregate, true));
            if (step == 0) {
                writer.write(stepAggregateHeader);
                writer.newLine();
            }
            for (StepActionProfile actionRecord : stepRecord.values()) {
                writer.write(actionRecord.toString());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void writeFraudsters(ArrayList<Fraudster> fraudsters) {
        String fraudsterHeader = "name,nbVictims,profit";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameFraudsters));
            writer.write(fraudsterHeader);
            writer.newLine();
            for (Fraudster f : fraudsters) {
                writer.write(f.toString());
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeParameters(long seed) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameParameters));
            writer.write(Parameters.toString(seed));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeClientsProfiles(Map<ClientActionProfile, Integer> countPerClientActionProfile, int numberClients) {
        String clientsProfilesHeader = "action,high,low,total,freq";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameClientProfiles));
            writer.write(clientsProfilesHeader);
            writer.newLine();

            for (Map.Entry<ClientActionProfile, Integer> counterActionProfile : countPerClientActionProfile.entrySet()) {
                ClientActionProfile clientActionProfile = counterActionProfile.getKey();
                String action = clientActionProfile.getAction();
                int count = counterActionProfile.getValue();

                double probability = ((double) count) / numberClients;

                writer.write(action + "," + clientActionProfile.getMinCount() + "," + clientActionProfile.getMaxCount() + ","
                        + count + "," + fastFormatDouble(5, probability));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSummarySimulation(PaySim paySim) {
        StringBuilder errorSummary = new StringBuilder();
        StepsProfiles simulationStepsProfiles = new StepsProfiles(Output.filenameStepAggregate, 1 / Parameters.multiplier, Parameters.nbSteps);
        double totalErrorRate = SummaryBuilder.buildSummary(Parameters.stepsProfiles, simulationStepsProfiles, errorSummary);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Output.filenameSummary));
            writer.write(errorSummary.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String summary = paySim.simulationName + "," + Parameters.nbSteps + "," + paySim.getTotalTransactions() + "," +
                paySim.getClients().size() + "," + totalErrorRate;
        writeGlobalSummary(summary);

        System.out.println("Nb of clients: " + paySim.getClients().size() + " - Nb of steps with transactions: " + paySim.getStepParticipated());
    }

    private static void writeGlobalSummary(String summary) {
        String header = "name,steps,nbTransactions,nbClients,totalError";
        File f = new File(filenameGlobalSummary);
        boolean fileExists = f.exists();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
            if (!fileExists) {
                writer.write(header);
                writer.newLine();
            }
            writer.write(summary);
            writer.newLine();
            writer.close();
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

    //See https://stackoverflow.com/a/10554128
    private static final int[] POW10 = {1, 10, 100, 1000, 10000, 100000, 1000000};

    public static String fastFormatDouble(int precision, double val) {
        StringBuilder sb = new StringBuilder();
        if (val < 0) {
            sb.append('-');
            val = -val;
        }
        int exp = POW10[precision];
        long lval = (long) (val * exp + 0.5);
        sb.append(lval / exp).append('.');
        long fval = lval % exp;
        for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
            sb.append('0');
        }
        sb.append(fval);
        return sb.toString();
    }

    public static String formatBoolean(boolean bool) {
        return bool ? "1" : "0";
    }

    public static void initOutputFilenames(String simulatorName) {
        String outputBaseString = Parameters.outputPath + simulatorName + "//" + simulatorName;
        filenameGlobalSummary = Parameters.outputPath + "summary.csv";

        filenameParameters = outputBaseString + "_PaySim.properties";
        filenameSummary = outputBaseString + "_Summary.txt";

        filenameRawLog = outputBaseString + "_rawLog.csv";
        filenameStepAggregate = outputBaseString + "_aggregatedTransactions.csv";
        filenameClientProfiles = outputBaseString + "_clientsProfiles.csv";
        filenameFraudsters = outputBaseString + "_fraudsters.csv";
    }
}
