package paysim.parameters;

import paysim.PaySim;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Parameters {
    public static long seed = 0;
    public static int nbClients = 0, nbMerchants = 0, nbFraudsters = 0, nbSteps = 0;
    public static double multiplier = 0, fraudProbability = 0, transferLimit = 0;
    public static String aggregateTransactionsParams = "", transferMaxPath = "",
            balanceHandlerFilePath = "", transferFreqMod = "", transferFreqModInit = "";
    public static String outputPath = "";
    public static boolean saveNetwork = false;
    public static String outputFolderNetwork = "";
    public static boolean saveToDB = false;
    public static String dbUrl = "", dbUser = "", dbPassword = "";

    public static void loadPropertiesFile(String propertiesFile) {
        try {
            Properties parameters = new Properties();
            InputStream inputStream = new FileInputStream(new File(propertiesFile));
            parameters.load(inputStream);

            initSeed(String.valueOf(parameters.getProperty("seed")));
            nbSteps = Integer.parseInt(parameters.getProperty("nbSteps"));
            multiplier = Double.parseDouble(parameters.getProperty("multiplier"));

            nbClients = Integer.parseInt(parameters.getProperty("nbClients"));
            nbFraudsters = Integer.parseInt(parameters.getProperty("nbFraudsters"));
            nbMerchants = Integer.parseInt(parameters.getProperty("nbMerchants"));
            fraudProbability = Double.parseDouble(parameters.getProperty("fraudProbability"));
            transferLimit = Double.parseDouble(parameters.getProperty("transferLimit"));

            String baseDir = System.getProperty("user.dir");
            aggregateTransactionsParams = baseDir + parameters.getProperty("aggregateTransactionsParams");
            transferMaxPath = baseDir + parameters.getProperty("transferMaxPath");
            balanceHandlerFilePath = baseDir + parameters.getProperty("balanceHandler");
            transferFreqMod = baseDir + parameters.getProperty("transferFreqMod");
            transferFreqModInit = baseDir + parameters.getProperty("transferFreqModInit");

            outputPath = parameters.getProperty("outputPath");

            saveNetwork = parameters.getProperty("saveNetwork").equals("1");
            outputFolderNetwork = parameters.getProperty("outputFolderNetwork");

            saveToDB = parameters.getProperty("saveToDB").equals("1");
            dbUrl = parameters.getProperty("dbUrl");
            dbUser = parameters.getProperty("dbUser");
            dbPassword = parameters.getProperty("DBPassword");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void initSeed(String seedString) {
        if (seedString.equals("time")) {
            seed = System.currentTimeMillis();
        } else {
            seed = Long.parseLong(seedString);
        }
        PaySim.seed = seed;
    }


    public String getFlags() {
        String flagRepresentation = "";

        if (Parameters.saveToDB) {
            flagRepresentation += "saveToDB=1\n";
        } else {
            flagRepresentation += "saveToDB=0\n";
        }

        if (Parameters.saveNetwork) {
            flagRepresentation += "saveNetwork=1\n";
        } else {
            flagRepresentation += "saveNetwork=0\n";
        }
        return flagRepresentation;
    }
}
