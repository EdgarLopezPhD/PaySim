package paysim.parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Parameters {
    private static String seedString;
    public static int nbMerchants = 0, nbFraudsters = 0, nbSteps = 0;
    public static double multiplier = 0, fraudProbability = 0, transferLimit = 0;
    public static String aggregateTransactionsParams = "", transferMaxPath = "",
            balanceHandlerFilePath = "", transferFreqMod = "", transferFreqModInit = "";
    public static String outputPath = "";
    public static boolean saveToDB = false;
    public static String dbUrl = "", dbUser = "", dbPassword = "";

    public static String filenameOutputAggregate, filenameLog, filenameFraudsters, filenameHistory,
            filenameErrorTable, filenameSummary, filenameFreqOutput, filenameGlobalSummary;

    public static void initParameters(String propertiesFile) {
        loadPropertiesFile(propertiesFile);

        BalanceClients.initBalanceClients(Parameters.balanceHandlerFilePath);
        TransactionParameters.loadTransferFreqModInit(Parameters.transferFreqModInit);
        TransactionParameters.loadTransferFreqMod(Parameters.transferFreqMod);
        StepParameters.initRecordList(Parameters.aggregateTransactionsParams, Parameters.multiplier, Parameters.nbSteps);
        TransactionParameters.loadTransferMax(Parameters.transferMaxPath);
    }

    private static void loadPropertiesFile(String propertiesFile) {
        try {
            Properties parameters = new Properties();
            InputStream inputStream = new FileInputStream(new File(propertiesFile));
            parameters.load(inputStream);

            seedString = String.valueOf(parameters.getProperty("seed"));
            nbSteps = Integer.parseInt(parameters.getProperty("nbSteps"));
            multiplier = Double.parseDouble(parameters.getProperty("multiplier"));

            nbFraudsters = Integer.parseInt(parameters.getProperty("nbFraudsters"));
            nbMerchants = Integer.parseInt(parameters.getProperty("nbMerchants"));
            fraudProbability = Double.parseDouble(parameters.getProperty("fraudProbability"));
            transferLimit = Double.parseDouble(parameters.getProperty("transferLimit"));

            aggregateTransactionsParams = parameters.getProperty("aggregateTransactionsParams");
            transferMaxPath = parameters.getProperty("transferMaxPath");
            balanceHandlerFilePath = parameters.getProperty("balanceHandler");
            transferFreqMod = parameters.getProperty("transferFreqMod");
            transferFreqModInit = parameters.getProperty("transferFreqModInit");

            outputPath = parameters.getProperty("outputPath");

            saveToDB = parameters.getProperty("saveToDB").equals("1");
            dbUrl = parameters.getProperty("dbUrl");
            dbUser = parameters.getProperty("dbUser");
            dbPassword = parameters.getProperty("dbPassword");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSeed() {
        // /!\ MASON seed is using an int internally
        // https://github.com/eclab/mason/blob/66d38fa58fae3e250b89cf6f31bcfa9d124ffd41/mason/sim/engine/SimState.java#L45
        if (seedString.equals("time")) {
            return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        } else {
            return Integer.parseInt(seedString);
        }
    }

    public static void initOutputFilenames(String simulatorName) {
        String outputBaseString = Parameters.outputPath + simulatorName + "//" + simulatorName;
        filenameGlobalSummary = Parameters.outputPath + "summary.csv";

        filenameLog = outputBaseString + "_log.csv";
        filenameOutputAggregate = outputBaseString + "_AggregateParamDump.csv";
        filenameFreqOutput = outputBaseString + "_repetitionFrequency.csv";
        filenameFraudsters = outputBaseString + "_Fraudsters.csv";
        filenameHistory = outputBaseString + "_ParamHistory.txt";
        filenameErrorTable = outputBaseString + "_ErrorTable.txt";
        filenameSummary = outputBaseString + "_Summary.csv";
    }
}
