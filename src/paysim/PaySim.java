package paysim;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import paysim.actors.Client;
import paysim.actors.Fraudster;
import paysim.actors.Merchant;

import paysim.base.ActionProbability;
import paysim.base.Transaction;
import paysim.parameters.BalanceClients;
import paysim.parameters.Parameters;
import paysim.parameters.StepParameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;

import static java.lang.Math.abs;

public class PaySim extends SimState {
    public static final double PAYSIM_VERSION = 1.0;
    private static final String[] DEFAULT_ARGS = new String[]{"", "-file", "PaySim.properties", "1"};

    public static long seed = 0;
    private String propertiesFile = "";
    String logFileName = "";

    public long startTime = 0;
    private double totalTransactionsMade = 0;

    public static String simulatorName = "";

    ArrayList<Transaction> transactions = new ArrayList<>();
    private ArrayList<Merchant> merchants = new ArrayList<>();
    private ArrayList<Fraudster> fraudsters = new ArrayList<>();
    public ArrayList<Client> clients = new ArrayList<>();

    public PaySim() {
        super(0);
    }

    public static void main(String args[]) {
        if (args.length < 4) {
            args = DEFAULT_ARGS;
        }
        int nrOfTimesRepeat = Integer.parseInt(args[3]);
        for (int i = 0; i < nrOfTimesRepeat; i++) {
            PaySim p = new PaySim();
            for (int x = 0; x < args.length - 1; x++) {
                if (args[x].equals("-file")) {
                    p.setPropertiesFile(args[x + 1]);
                }
            }
            p.initParameters();
            p.runSimulation();
        }
    }

    private void initParameters(){
        Parameters.loadPropertiesFile(propertiesFile);
        initSimulatorName();
        logFileName = System.getProperty("user.dir") + "//outputs//" + simulatorName
                + "//" + simulatorName + "_log.csv";
        createLogFile(logFileName);
    }

    private void runSimulation() {
        System.out.println("PAYSIM: Financial Simulator v" + PAYSIM_VERSION + " \n");
        long elapsedSteps;
        double begin;

        startTime = System.currentTimeMillis();
        super.start();
        initSimulation();

        begin = System.currentTimeMillis();
        System.out.println("Starting PaySim Running for " + Parameters.nbSteps + " steps.");

        while ((elapsedSteps = schedule.getSteps()) < Parameters.nbSteps) {
            if (!schedule.step(this))
                break;
            if (elapsedSteps % 100 == 100 - 1) {
                System.out.println("Step " + elapsedSteps);
            } else {
                System.out.print("*");
            }
        }
        System.out.println("\nFinished running " + elapsedSteps + " steps ");
        finish();

        double total = System.currentTimeMillis() - begin;
        total = total / 1000 / 60;
        System.out.println("\nIt took:\t" + total + " minutes to execute the simulation\n");
        System.out.println("Simulation name: " + simulatorName);
    }

    private void createLogFile(String logFileName) {
        try {
            FileWriter writer = new FileWriter(new File(logFileName));
            BufferedWriter bufWriter = new BufferedWriter(writer);
            bufWriter.write("step,action,amount,nameOrig,oldbalanceOrg,newbalanceOrig,nameDest,oldbalanceDest,newbalanceDest,isFraud,isFlaggedFraud\n");
            bufWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSimulatorName() {
        Date d = new Date();
        simulatorName = "PS_" + (d.getYear() + 1900) + (d.getMonth() + 1) + d.getDate() + d.getHours() + d.getMinutes()
                + d.getSeconds() + "_" + seed;
        File f = new File(System.getProperty("user.dir") + "//outputs//" + simulatorName);
        f.mkdirs();
    }

    private void initSimulation() {
        System.out.println("Init\nNbMerchants:\t" + Parameters.nbMerchants + "\nSeed:\t" + seed + "\n");
        setSeed(seed);
        BalanceClients.initBalanceClients(Parameters.balanceHandlerFilePath);
        TransactionParameters.loadTransferFreqModInit(Parameters.transferFreqModInit);
        TransactionParameters.loadTransferFreqMod(Parameters.transferFreqMod);
        StepParameters.initRecordList(Parameters.aggregateTransactionsParams, Parameters.multiplier, Parameters.nbSteps);
        TransactionParameters.loadTransferMax(Parameters.transferMaxPath);

        //Add the merchants
        System.out.println("NbMerchants:\t" + Parameters.nbMerchants * Parameters.multiplier + "\n");
        for (int i = 0; i < Parameters.nbMerchants * Parameters.multiplier; i++) {
            Merchant m = new Merchant(generateIdentifier());
            merchants.add(m);
        }

        //Add the fraudsters
        System.out.println("NrOfFraudsters:\t" + Parameters.nbFraudsters * Parameters.multiplier + "\n");
        for (int i = 0; i < Parameters.nbFraudsters * Parameters.multiplier; i++) {
            Fraudster f = new Fraudster(generateIdentifier());
            fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }

        //Start the manager
        Manager manager = new Manager();
        schedule.scheduleRepeating(manager);
    }

    public void finish() {
        String outputBaseString = System.getProperty("user.dir") + Parameters.outputPath + simulatorName + "//" + simulatorName;
        String filenameOutputAggregate = outputBaseString + "_AggregateParamDump.csv";

        String logFilename =  outputBaseString + "_log.csv";
        String filenameFraudsters = outputBaseString + "_Fraudsters.csv";
        String filenameHistory = outputBaseString + "_ParamHistory" + ".txt";
        String filenameErrorTable = outputBaseString + "_ErrorTable.txt";
        String filenameParamAggregate = System.getProperty("user.dir") + "/paramFiles/AggregateTransaction.csv";
        String filenameSummary = outputBaseString + "_Summary.csv";
        String filenameFreqOutput = outputBaseString + "_repetitionFrequency.csv";
        String filenameSummary2 = System.getProperty("user.dir") + Parameters.outputPath + "summary.csv";

        Output.writeLog(logFilename, transactions);
        Output.writeFraudsters(filenameFraudsters, fraudsters);

        Output.writeParamfileHistory(filenameHistory, this);

        double totalErrorRate = Output.writeErrorTable(filenameParamAggregate, filenameOutputAggregate, filenameErrorTable);

        Output.writeSummaryFile(filenameParamAggregate, filenameOutputAggregate, filenameSummary, this);
        String summary = simulatorName + "," + Parameters.nbSteps + "," + totalTransactionsMade + "," + clients.size() + "," + totalErrorRate + "\n";
        Output.appendSimulationSummary(filenameSummary2, summary);
        Output.dumpRepetitionFreq(filenameFreqOutput);
        System.out.println("Nb of clients:\t" + clients.size() + "\n"
                + "Nb of steps with transactions:\t" + Manager.nbStepParticipated + "\n");

    }

    public Merchant getRandomMerchant() {
        return getMerchants().get(random.nextInt(getMerchants().size()));
    }

    public Client getRandomClient() {
        if (getClients().size() > 0) {
            return getClients().get(random.nextInt(getClients().size()));
        }
        return null;
    }

    void resetVariables() {
        transactions = new ArrayList<>();
    }

    private void setPropertiesFile(String s) {
        propertiesFile = s;
    }

    //Parameters the probabilities into the probability array
    double[] loadProbabilities(Map<String, ActionProbability> actionProbabilities, int nbClients) {
        double coef = Parameters.multiplier / ((double) nbClients);
        return actionProbabilities.values().stream()
                .map(ActionProbability::getNbTransactions)
                .mapToDouble(x -> x * coef)
                .toArray();
    }

    public static double getCumulative(String action, int rowIndex, ArrayList<String> fileContents) {
        double aggr = 0;
        for (String line: fileContents) {
            String split[] = line.split(",");
            String currAction = split[0];

            if (currAction.equals(action)) {
                aggr += Double.parseDouble(split[rowIndex]);
            }
        }
        return aggr;
    }

    public void updateTotalTransactionsMade(double cumulative){
        totalTransactionsMade += cumulative;
    }

    public String generateIdentifier(){
        return String.valueOf(abs(String.valueOf(System.currentTimeMillis()).hashCode()));
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    private ArrayList<Merchant> getMerchants() {
        return this.merchants;
    }
}