package paysim;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.Collections;

import paysim.actors.Client;
import paysim.actors.Fraudster;
import paysim.actors.Merchant;

import paysim.base.Repetition;
import paysim.base.Transaction;
import paysim.parameters.Parameters;
import paysim.parameters.StepParameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;

import static java.lang.Math.abs;

public class PaySim extends SimState {
    public static final double PAYSIM_VERSION = 1.0;
    private static final String[] DEFAULT_ARGS = new String[]{"", "-file", "PaySim.properties", "1"};

    public final String simulatorName;
    private long startTime = 0;
    private int totalTransactionsMade = 0;
    private int stepParticipated = 0;

    private ArrayList<Client> clients = new ArrayList<>();
    private ArrayList<Merchant> merchants = new ArrayList<>();
    private ArrayList<Fraudster> fraudsters = new ArrayList<>();

    private ArrayList<Transaction> transactions = new ArrayList<>();

    private ArrayList<Integer> countAssignedTransactions;
    private Map<String, Integer> countCallAction = new HashMap<>();
    private Map<Repetition, Integer> countCallRepetition = new HashMap<>();

    public static void main(String args[]) {
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
        startTime = System.currentTimeMillis();
        super.start();

        initActors();
        initCounters();

        //Start the manager
        Manager manager = new Manager();
        schedule.scheduleRepeating(manager);

        System.out.println("Starting PaySim Running for " + Parameters.nbSteps + " steps.");

        long elapsedSteps;
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

        double total = System.currentTimeMillis() - startTime;
        total = total / 1000 / 60;
        System.out.println("\nIt took:\t" + total + " minutes to execute the simulation\n");
        System.out.println("Simulation name: " + simulatorName);
    }

    private void initActors() {
        System.out.println("Init\nNbMerchants:\t" + Parameters.nbMerchants + "\nSeed:\t" + seed() + "\n");

        //Add the merchants
        System.out.println("NbMerchants:\t" + Parameters.nbMerchants * Parameters.multiplier + "\n");
        for (int i = 0; i < Parameters.nbMerchants * Parameters.multiplier; i++) {
            Merchant m = new Merchant(generateIdentifier());
            merchants.add(m);
        }

        //Add the fraudsters
        System.out.println("NbFraudsters:\t" + Parameters.nbFraudsters * Parameters.multiplier + "\n");
        for (int i = 0; i < Parameters.nbFraudsters * Parameters.multiplier; i++) {
            Fraudster f = new Fraudster(generateIdentifier());
            fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }
    }

    private void initCounters() {
        countAssignedTransactions = new ArrayList<>(Collections.nCopies(Parameters.nbSteps, 0));
        for (String action : TransactionParameters.getActions()) {
            countCallAction.put(action, 0);
            for (Repetition repetition : TransactionParameters.getRepetitionsFromAction(action)) {
                countCallRepetition.put(repetition, 0);
            }
        }
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
        Output.dumpRepetitionFreq(Parameters.filenameFreqOutput, countCallAction, countCallRepetition);
        System.out.println("Nb of clients:\t" + clients.size() + "\nNb of steps with transactions:\t" + stepParticipated + "\n");

    }

    public int getCountAssigned(int step) {
        return countAssignedTransactions.get(step);
    }

    public ArrayList<Integer> getSteps(int step, int nbSteps) {
        if (isFullAfter(step)) {
            return null;
        }

        ArrayList<Integer> stepsToBeRepeated = new ArrayList<>();
        int stepsGathered = 0;
        int index = 0;
        while (stepsGathered < nbSteps) {
            index = index % countAssignedTransactions.size();
            if (index >= step) {
                int alreadyAssigned = getCountAssigned(index);
                if (alreadyAssigned < StepParameters.getMaxCount(index)) {
                    countAssignedTransactions.set(index, alreadyAssigned + 1);
                    stepsToBeRepeated.add(index);
                    stepsGathered++;
                }
            }
            index++;
        }
        return stepsToBeRepeated;

    }

    public Repetition getRepetition() {
        String action = getNextAction();
        Repetition repetition = TransactionParameters.pickNextRepetition(action);
        int count = countCallRepetition.get(repetition);
        countCallRepetition.put(repetition, count + 1);
        return repetition;
    }

    private String getNextAction() {
        String action = TransactionParameters.pickNextAction();
        int count = countCallAction.get(action);
        countCallAction.put(action, count + 1);
        return action;
    }

    private boolean isFullAfter(int step) {
        return IntStream.range(step, countAssignedTransactions.size())
                .noneMatch(i -> countAssignedTransactions.get(i) < StepParameters.getMaxCount(i));
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
        if (transactions.size() > 0) {
            stepParticipated++;
        }
        transactions = new ArrayList<>();
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

    private ArrayList<Merchant> getMerchants() {
        return this.merchants;
    }
}