package paysim;

import java.io.File;
import java.util.*;

import paysim.actors.Bank;
import paysim.actors.Client;
import paysim.actors.Fraudster;
import paysim.actors.Merchant;

import paysim.base.ClientActionProfile;
import paysim.base.StepActionProfile;
import paysim.base.Transaction;
import paysim.parameters.BalanceClients;
import paysim.parameters.Parameters;
import paysim.parameters.StepProfile;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;

import static java.lang.Math.abs;

public class PaySim extends SimState {
    public static final double PAYSIM_VERSION = 1.0;
    private static final String[] DEFAULT_ARGS = new String[]{"", "-file", "PaySim.properties", "5"};

    public final String simulatorName;
    private int totalTransactionsMade = 0;
    private int stepParticipated = 0;

    private ArrayList<Client> clients = new ArrayList<>();
    private ArrayList<Merchant> merchants = new ArrayList<>();
    private ArrayList<Fraudster> fraudsters = new ArrayList<>();
    private ArrayList<Bank> banks = new ArrayList<>();

    private ArrayList<Transaction> transactions = new ArrayList<>();
    private Map<String, StepActionProfile> stepActionProfile;
    private int stepTargetCount;

    private Map<String, Integer> countCallAction = new HashMap<>();
    private Map<ClientActionProfile, Integer> countProfileAssignement = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 4) {
            args = DEFAULT_ARGS;
        }
        int nbTimesRepeat = Integer.parseInt(args[3]);
        String propertiesFile = "";
        for (int x = 0; x < args.length - 1; x++) {
            if (args[x].equals("-file")) {
                propertiesFile = args[x + 1];
            }
        }
        Parameters.initParameters(propertiesFile);
        for (int i = 0; i < nbTimesRepeat; i++) {
            PaySim p = new PaySim();
            p.runSimulation();
        }
    }

    public PaySim() {
        super(Parameters.getSeed());

        Date d = new Date();
        simulatorName = "PS_" + (d.getYear() + 1900) + (d.getMonth() + 1) + d.getDate() + d.getHours() + d.getMinutes()
                + d.getSeconds() + "_" + seed();
        File f = new File(Parameters.outputPath + simulatorName);
        f.mkdirs();
        Parameters.initOutputFilenames(simulatorName);
        Output.createLogFile(Parameters.filenameLog);
    }

    private void runSimulation() {
        System.out.println("PAYSIM: Financial Simulator v" + PAYSIM_VERSION + " \n");
        long startTime = System.currentTimeMillis();
        super.start();

        initCounters();
        initActors();

        System.out.println("Starting PaySim Running for " + Parameters.nbSteps + " steps.");

        int currentStep;
        while ((currentStep = (int) schedule.getSteps()) < Parameters.nbSteps) {
            stepActionProfile = StepProfile.get(currentStep);
            stepTargetCount = StepProfile.getTargetCount(currentStep);
            if (!schedule.step(this))
                break;

            writeOutputStep(currentStep);
            if (currentStep % 100 == 100 - 1) {
                System.out.println("Step " + currentStep);
            } else {
                System.out.print("*");
            }
        }
        System.out.println("\nFinished running " + currentStep + " steps ");
        finish();

        double total = System.currentTimeMillis() - startTime;
        total = total / 1000 / 60;
        System.out.println("\nIt took:\t" + total + " minutes to execute the simulation\n");
        System.out.println("Simulation name: " + simulatorName);
    }

    private void initCounters() {
        for (String action : TransactionParameters.getActions()) {
            countCallAction.put(action, 0);
            for (ClientActionProfile clientActionProfile : TransactionParameters.getProfilesFromAction(action)) {
                countProfileAssignement.put(clientActionProfile, 0);
            }
        }
    }

    private void initActors() {
        System.out.println("Init - Seed " + seed());

        //Add the merchants
        System.out.println("NbMerchants:\t" + Parameters.nbMerchants * Parameters.multiplier);
        for (int i = 0; i < Parameters.nbMerchants * Parameters.multiplier; i++) {
            Merchant m = new Merchant(generateIdentifier());
            merchants.add(m);
        }

        //Add the fraudsters
        System.out.println("NbFraudsters:\t" + Parameters.nbFraudsters * Parameters.multiplier);
        for (int i = 0; i < Parameters.nbFraudsters * Parameters.multiplier; i++) {
            Fraudster f = new Fraudster(generateIdentifier());
            fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }

        //Add the banks
        System.out.println("NbBanks:\t" + Parameters.nbBanks * Parameters.multiplier);
        for (int i = 0; i < Parameters.nbBanks; i++) {
            Bank b = new Bank(generateIdentifier());
            banks.add(b);
        }

        //Add the clients
        System.out.println("NbClients:\t" + Parameters.nbClients * Parameters.multiplier);
        for (int i = 0; i < Parameters.nbClients * Parameters.multiplier; i++) {
            Client c = new Client(generateIdentifier(),
                    getRandomBank(),
                    getClientActionProfile(),
                    BalanceClients.getNextBalance(random),
                    random);
            clients.add(c);
        }
        for (Client c : clients) {
            schedule.scheduleRepeating(c);
        }
    }

    public Map<String, ClientActionProfile> getClientActionProfile() {
        Map<String, ClientActionProfile> profile = new HashMap<>();
        for (String action : TransactionParameters.getActions()) {
            ClientActionProfile clientActionProfile = TransactionParameters.pickNextProfile(action);
            int count = countProfileAssignement.get(clientActionProfile);
            countProfileAssignement.put(clientActionProfile, count + 1);
            profile.put(action, clientActionProfile);
        }
        return profile;
    }

    public void finish() {
        Output.writeLog(Parameters.filenameLog, transactions);
        Output.writeFraudsters(Parameters.filenameFraudsters, fraudsters);

        Output.writeParamfileHistory(Parameters.filenameHistory, seed());

        String totalErrorRate = Output.writeErrorTable(Parameters.aggregateTransactionsParams,
                Parameters.filenameOutputAggregate, Parameters.filenameErrorTable);

        Output.writeSummaryFile(Parameters.aggregateTransactionsParams, Parameters.filenameOutputAggregate, Parameters.filenameSummary, this);
        String summary = simulatorName + "," + Parameters.nbSteps + "," + totalTransactionsMade + "," + clients.size() + "," + totalErrorRate + "\n";
        Output.appendSimulationSummary(Parameters.filenameGlobalSummary, summary);
        Output.dumpRepetitionFreq(Parameters.filenameFreqOutput, countCallAction, countProfileAssignement);
        System.out.println("Nb of clients:\t" + clients.size() + "\nNb of steps with transactions:\t" + stepParticipated + "\n");
    }

    public Merchant getRandomMerchant() {
        return merchants.get(random.nextInt(merchants.size()));
    }

    public Bank getRandomBank() {
        return banks.get(random.nextInt(banks.size()));
    }

    public Client getRandomClient() {
        if (clients.size() > 0) {
            return clients.get(random.nextInt(clients.size()));
        }
        return null;
    }

    void resetVariables() {
        if (transactions.size() > 0) {
            stepParticipated++;
        }
        transactions = new ArrayList<>();
    }

    private void writeOutputStep(int step) {
        ArrayList<Transaction> transactions = getTransactions();

        Output.writeLog(Parameters.filenameLog, transactions);
        if (Parameters.saveToDB) {
            Output.writeDatabaseLog(Parameters.dbUrl, Parameters.dbUser, Parameters.dbPassword, transactions, simulatorName);
        }

        Output.writeAggregateStep(Parameters.filenameOutputAggregate, step, getTransactions());
        resetVariables();
    }

    public void updateTotalTransactionsMade(int count) {
        totalTransactionsMade += count;
    }

    public String generateIdentifier() {
        return String.valueOf(abs(String.valueOf(System.currentTimeMillis()).hashCode()));
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public Map<String, StepActionProfile> getStepActionProfile() {
        return stepActionProfile;
    }
    public int getStepTargetCount() {
        return stepTargetCount;
    }
}