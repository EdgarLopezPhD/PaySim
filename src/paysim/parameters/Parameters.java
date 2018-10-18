package paysim.parameters;

import paysim.PaySim;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import static paysim.PaySim.simulatorName;

public class Parameters {
    public static long seed = 0;
    public static int nbMerchants = 0, nbFraudsters = 0, nbSteps = 0;
    public static double multiplier = 0, fraudProbability = 0, transferLimit = 0;
    public static String aggregateTransactionsParams = "", transferMaxPath = "",
            balanceHandlerFilePath = "", transferFreqMod = "", transferFreqModInit = "";
    public static String outputPath = "";
    public static boolean saveToDB = false;
    public static String dbUrl = "", dbUser = "", dbPassword = "";

    private static String outputBaseString;
    public static String filenameOutputAggregate, filenameLog, filenameFraudsters, filenameHistory,
            filenameErrorTable, filenameSummary, filenameFreqOutput, filenameGlobalSummary;

    public static void loadPropertiesFile(String propertiesFile) {
        try {
            Properties parameters = new Properties();
            InputStream inputStream = new FileInputStream(new File(propertiesFile));
            parameters.load(inputStream);

            initSeed(String.valueOf(parameters.getProperty("seed")));
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
        initSimulatorName();
        initOutputFilenames();
    }

    private static void initSeed(String seedString) {
        if (seedString.equals("time")) {
            seed = System.currentTimeMillis();
        } else {
            seed = Long.parseLong(seedString);
        }
        PaySim.seed = seed;
    }

    private static void initSimulatorName() {
        Date d = new Date();
        simulatorName = "PS_" + (d.getYear() + 1900) + (d.getMonth() + 1) + d.getDate() + d.getHours() + d.getMinutes()
                + d.getSeconds() + "_" + seed;
        File f = new File(Parameters.outputPath + simulatorName);
        f.mkdirs();
    }

    private static void initOutputFilenames() {
        outputBaseString = Parameters.outputPath + simulatorName + "//" + simulatorName;
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
