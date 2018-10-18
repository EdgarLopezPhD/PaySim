package paysim;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import paysim.actors.Client;
import paysim.actors.Fraudster;
import paysim.actors.Merchant;

import paysim.aggregation.AggregateParamFileCreator;
import paysim.aggregation.AggregateTransactionRecord;
import paysim.base.ActionProbability;
import paysim.parameters.BalanceClients;
import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;

import static java.lang.Math.abs;

public class PaySim extends SimState {
    public static final double PAYSIM_VERSION = 1.0;
    public static long seed = 0;
    private String propertiesFile = "";
    private static final String[] DEFAULT_ARGS = new String[]{"", "-file", "PaySim.properties", "1"};

    public ArrayList<AggregateTransactionRecord> aggrTransRecordList = new ArrayList<>();
    public AggregateParamFileCreator aggregateCreator = new AggregateParamFileCreator();
    private ProbabilityContainerHandler probabilityContainerHandler = new ProbabilityContainerHandler();

    ArrayList<Transaction> trans = new ArrayList<>();
    private ArrayList<Merchant> merchants = new ArrayList<>();
    private ArrayList<Fraudster> fraudsters = new ArrayList<>();
    public ArrayList<Client> clients = new ArrayList<>();

    String logFileName = "";

    private CurrentStepHandler stepHandler;

    public long startTime = 0;
    private double totalTransactionsMade = 0;

    public static String simulatorName = "";
    private ArrayList<String> paramFile = new ArrayList<>();

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
        loadAggregatedFile();
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
            bufWriter.write("step,type,amount,nameOrig,oldbalanceOrg,newbalanceOrig,nameDest,oldbalanceDest,newbalanceDest,isFraud,isFlaggedFraud\n");
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
        stepHandler = new CurrentStepHandler(paramFile, Parameters.multiplier);

        probabilityContainerHandler.initRecordList(paramFile);
        Manager manager = new Manager();
        manager.setStepHandler(stepHandler);
        manager.setProbabilityHandler(probabilityContainerHandler);
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
        schedule.scheduleRepeating(manager);
    }

    public void finish() {
        String outputBaseString = System.getProperty("user.dir") + Parameters.outputPath + simulatorName + "//" + simulatorName;
        String logFilename =  outputBaseString + "_log.csv";
        String filenameOutputAggregate = outputBaseString + "_AggregateParamDump.csv";
        String filenameFraudsters = outputBaseString + "_Fraudsters.csv";
        String filenameHistory = outputBaseString + "_ParamHistory" + ".txt";
        String filenameErrorTable = outputBaseString + "_ErrorTable.txt";
        String filenameParamAggregate = System.getProperty("user.dir") + "/paramFiles/AggregateTransaction.csv";
        String filenameSummary = outputBaseString + "_Summary.csv";
        String filenameFreqOutput = outputBaseString + "_repetitionFrequency.csv";
        String filenameSummary2 = System.getProperty("user.dir") + Parameters.outputPath + "summary.csv";

        Output.writeLog(logFilename, trans);
        Output.writeFraudsters(filenameFraudsters, fraudsters);
        Output.writeAggregateParamFile(filenameOutputAggregate, this);

        Output.writeParamfileHistory(filenameHistory, this);

        double totalErrorRate = Output.writeErrorTable(filenameParamAggregate, filenameOutputAggregate, filenameErrorTable);

        Output.writeSummaryFile(filenameParamAggregate, filenameOutputAggregate, filenameSummary, this);
        String summary = simulatorName + "," + Parameters.nbSteps + "," + totalTransactionsMade + "," + clients.size() + "," + totalErrorRate + "\n";
        Output.appendSimulationSummary(filenameSummary2, summary);
        Output.dumpRepetitionFreq(filenameFreqOutput);
        System.out.println("NrOfTrueClients:\t" + (Manager.trueNrOfClients * Parameters.multiplier) + "\n"
                + "NrStepParticipated\t" + Manager.nbStepParticipated + "\n");

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
        trans = new ArrayList<>();
    }

    private void setPropertiesFile(String s) {
        propertiesFile = s;
    }

    private void loadAggregatedFile() {
        paramFile = new ArrayList<>();
        try {
            FileReader reader = new FileReader(new File(Parameters.aggregateTransactionsParams));
            BufferedReader bufReader = new BufferedReader(reader);

            String line;
            while ((line = bufReader.readLine()) != null) {
                paramFile.add(line);
            }
            bufReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Parameters the probabilities into the probability array
    double[] loadProbabilities(ArrayList<ActionProbability> list, int nrOfClients) {
        double ProbabilityArr[] = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            double currProb = (list.get(i).getNrOfTransactions() * Parameters.multiplier) / ((double) nrOfClients);
            ProbabilityArr[i] = currProb;
        }
        return ProbabilityArr;
    }

    public static double getCumulative(String type, int rowIndex, ArrayList<String> fileContents) {
        double aggr = 0;
        for (String line: fileContents) {
            String split[] = line.split(",");
            String currType = split[0];

            if (currType.equals(type)) {
                aggr += Double.parseDouble(split[rowIndex]);
            }
        }
        return aggr;
    }

    public void updateTotalTransactionsMade(double cumulative){
        totalTransactionsMade += cumulative;
    }

    public ArrayList<Transaction> getTrans() {
        return trans;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    private ArrayList<Merchant> getMerchants() {
        return this.merchants;
    }

    ArrayList<AggregateTransactionRecord> getAggrTransRecordList() {
        return aggrTransRecordList;
    }

    AggregateParamFileCreator getAggregateCreator() {
        return aggregateCreator;
    }

    public String generateIdentifier(){
        return String.valueOf(abs(String.valueOf(System.currentTimeMillis()).hashCode()));
    }
}