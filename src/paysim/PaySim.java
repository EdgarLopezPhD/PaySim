package paysim;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import paysim.actors.Client;
import paysim.actors.Fraudster;
import paysim.actors.Merchant;

import paysim.aggregation.AggregateParamFileCreator;
import paysim.aggregation.AggregateTransactionRecord;
import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;

public class PaySim extends SimState {
    public static final double PAYSIM_VERSION = 1.0;
    public static long seed = 0;
    String propertiesFile = "";

    public ArrayList<AggregateTransactionRecord> aggrTransRecordList = new ArrayList<>();
    public AggregateParamFileCreator aggregateCreator = new AggregateParamFileCreator();
    ProbabilityContainerHandler probabilityContainerHandler = new ProbabilityContainerHandler();

    ArrayList<Transaction> trans = new ArrayList<>();
    ArrayList<Merchant> merchants = new ArrayList<>();
    ArrayList<Fraudster> fraudsters = new ArrayList<>();
    public ArrayList<Client> clients = new ArrayList<>();

    ArrayList<String> actionTypes = new ArrayList<>();
    String logFileName = "";

    InitBalanceHandler balanceHandler;
    CurrentStepHandler stepHandler;

    public long startTime = 0;
    double totalTransactionsMade = 0;

    public static String simulatorName = "";
    private ArrayList<String> paramFile = new ArrayList<>();

    public RepetitionFreqHandler getRepFreqHandler() {
        return repFreqHandler;
    }

    RepetitionFreqHandler repFreqHandler = new RepetitionFreqHandler();

    public PaySim() {
        super(0);
    }

    public static void main(String args[]) {
        args = new String[] { "", "-file", "PaySim.properties", "1"};
        int nrOfTimesRepeat = Integer.parseInt(args[3]);
        for (int i = 0; i < nrOfTimesRepeat; i++) {
            PaySim p = new PaySim();
            for (int x = 0; x < args.length - 1; x++) {
                if (args[x].equals("-file")) {
                    p.setPropertiesFile(args[x + 1]);
                    //Gets the number of steps
                }
            }
            p.initParameters();
            p.runSimulation();
        }
    }

    public void initParameters(){
        Parameters.loadPropertiesFile(propertiesFile);
        initSimulatorName();
        loadAggregatedFile();
        logFileName = System.getProperty("user.dir") + "//outputs//" + simulatorName
                + "//" + simulatorName + "_log.csv";
        createLogFile(logFileName);
        setActionTypes(TransactionParameters.getTypes());
    }

    public void runSimulation() {
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
            if (elapsedSteps % 100 == 0 && elapsedSteps != 0) {
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

    public void initSimulation() {
        System.out.println("Init\nNbMerchants:\t" + Parameters.nbMerchants + "\nSeed:\t" + seed + "\n");
        setSeed(seed);
        balanceHandler = new InitBalanceHandler(Parameters.balanceHandlerFilePath);
        System.out.println("Inputting this paramfile:\n");
        TransactionParameters.loadTransferFreq4ModInit(Parameters.transferFreqModInit);
        stepHandler = new CurrentStepHandler(paramFile, Parameters.multiplier);

        balanceHandler.setPaysim(this);
        probabilityContainerHandler.initRecordList(paramFile);
        Manager manager = new Manager();
        manager.setBalanceHandler(balanceHandler);
        manager.setStepHandler(stepHandler);
        manager.setProbabilityHandler(probabilityContainerHandler);
        manager.setRepFreqHandler(repFreqHandler);
        TransactionParameters.loadTransferMax(Parameters.transferMaxPath);

        //Add the merchants
        System.out.println("NbMerchants:\t" + Parameters.nbMerchants * Parameters.multiplier + "\n");
        for (int i = 0; i < Parameters.nbMerchants * Parameters.multiplier; i++) {
            Merchant m = new Merchant();
            merchants.add(m);
        }

        //Add the fraudsters
        System.out.println("NrOfFraudsters:\t" + Parameters.nbFraudsters * Parameters.multiplier + "\n");
        for (int i = 0; i < Parameters.nbFraudsters * Parameters.multiplier; i++) {
            Fraudster f = new Fraudster();
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
        String networkFilename = outputBaseString + "_networkdump.txt";
        String filenameErrorTable = outputBaseString + "_ErrorTable.txt";
        String filenameParamAggregate = System.getProperty("user.dir") + "/paramFiles/AggregateTransaction.csv";
        String filenameSummary = outputBaseString + "_Summary.csv";
        String filenameFreqOutput = outputBaseString + "_repetitionFrequency.csv";
        String filenameSummary2 = System.getProperty("user.dir") + Parameters.outputPath + "summary.csv";

        Output.writeLog(logFilename, trans);
        Output.writeFraudsters(filenameFraudsters, fraudsters);
        Output.writeAggregateParamFile(filenameOutputAggregate, this);

        Output.writeParamfileHistory(filenameHistory, this);
        if (Parameters.saveNetwork) {
            Output.writeNetworkResults(logFilename, networkFilename);
        }
        double totalErrorRate = Output.writeErrorTable(filenameParamAggregate, filenameOutputAggregate, filenameErrorTable);

        Output.writeSummaryFile(filenameParamAggregate, filenameOutputAggregate, filenameSummary, this);
        String summary = simulatorName + "," + Parameters.nbSteps + "," + totalTransactionsMade + "," + clients.size() + "," + totalErrorRate + "\n";
        Output.appendSimulationSummary(filenameSummary2, summary);
        Output.dumpRepetitionFreq(filenameFreqOutput, this);
        System.out.println("NrOfTrueClients:\t" + (Manager.trueNrOfClients * Parameters.multiplier) + "\n"
                + "NrOfDaysParticipated\t" + Manager.nrOfDaysParticipated + "\n");

    }

    public Merchant getRandomMerchant() {
        return getMerchants().get(random.nextInt(getMerchants().size()));
    }

    public Client getRandomClient() {
        if (getClients().size() > 0) {
            Client c = getClients().get(random.nextInt(getClients().size()));
            return c;
        }
        return null;
    }

    public double getTotalFromType(String type, ArrayList<RepetitionFreqContainer> contList) {
        double total = 0;
        for (RepetitionFreqContainer s : contList) {
            if (s.getCont().getType().equals(type)) {
                total += s.getFreq();
            }
        }
        return total;
    }

    public ArrayList<String> listOfProbs() {
        ArrayList<String> allContents = new ArrayList<String>();
        File f = new File(Parameters.transferFreqMod);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = reader.readLine()) != null) {
                allContents.add(line);
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return allContents;
    }


    public int generateStep(AggregateTransactionRecord record) {
        int step = 0;
        step = (int) (((Double.parseDouble(record.gettDay()) - 1) * 24) + (Double.parseDouble(record.gettHour()) + 1));
        return step;
    }

    public void resetVariables() {
        trans = new ArrayList<>();
    }

    public void setPropertiesFile(String s) {
        propertiesFile = s;
    }

    private void loadAggregatedFile() {
        paramFile = new ArrayList<>();
        try {
            FileReader reader = new FileReader(new File(Parameters.aggregateTransactionsParams));
            BufferedReader bufReader = new BufferedReader(reader);

            String tempLine = "";

            while ((tempLine = bufReader.readLine()) != null) {
                paramFile.add(tempLine);
            }
            bufReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Parameters the probabilities into the probability array
    public double[] loadProbabilites(ArrayList<ActionProbability> list, int nrOfClients) {
        double ProbabilityArr[] = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            double currProb = (list.get(i).getNrOfTransactions() * Parameters.multiplier) / ((double) nrOfClients);
            ProbabilityArr[i] = currProb;
        }
        return ProbabilityArr;
    }

    public static double getCumulative(String type, int rowIndex, ArrayList<String> fileContents) {
        boolean isInt = false;
        try {
            Integer.parseInt(type);
            isInt = true;
        } catch (Exception e) {

        }

        double aggr = 0;
        for (int i = 0; i < fileContents.size(); i++) {
            String row = fileContents.get(i);
            String split[] = row.split(",");
            String currType = split[0];
            if (isInt) {
                double alterDouble = Double.parseDouble(currType);
                int alterInt = (int) alterDouble;
                currType = String.valueOf(alterInt);
            }
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

    public void setActionTypes(ArrayList<String> actionTypes) {
        this.actionTypes = actionTypes;
    }

    public ArrayList<Merchant> getMerchants() {
        return this.merchants;
    }

    public ArrayList<AggregateTransactionRecord> getAggrTransRecordList() {
        return aggrTransRecordList;
    }

    public AggregateParamFileCreator getAggregateCreator() {
        return aggregateCreator;
    }
}