package org.paysim.paysim.actors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static java.lang.Math.max;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.distribution.Binomial;

import org.paysim.paysim.PaySim;

import org.paysim.paysim.base.ClientActionProfile;
import org.paysim.paysim.base.ClientProfile;
import org.paysim.paysim.base.StepActionProfile;
import org.paysim.paysim.base.Transaction;

import org.paysim.paysim.parameters.ActionTypes;
import org.paysim.paysim.parameters.Parameters;
import org.paysim.paysim.parameters.BalancesClients;

import org.paysim.paysim.utils.RandomCollection;


public class Client extends SuperActor implements Steppable {
    private static final String CLIENT_IDENTIFIER = "C";
    private static final int MIN_NB_TRANSFER_FOR_FRAUD = 3;
    private static final String CASH_IN = "CASH_IN", CASH_OUT = "CASH_OUT", DEBIT = "DEBIT",
            PAYMENT = "PAYMENT", TRANSFER = "TRANSFER", DEPOSIT = "DEPOSIT";
    private final Bank bank;
    private ClientProfile clientProfile;
    private double clientWeight;
    private double balanceMax = 0;
    private int countTransferTransactions = 0;
    private double expectedAvgTransaction = 0;
    private double initialBalance;

    Client(String name, Bank bank) {
        super(CLIENT_IDENTIFIER + name);
        this.bank = bank;
    }

    public Client(PaySim paySim) {
        super(CLIENT_IDENTIFIER + paySim.generateId());
        this.bank = paySim.pickRandomBank();
        this.clientProfile = new ClientProfile(paySim.pickNextClientProfile(), paySim.random);
        this.clientWeight = ((double) clientProfile.getClientTargetCount()) /  Parameters.stepsProfiles.getTotalTargetCount();
        this.initialBalance = BalancesClients.pickNextBalance(paySim.random);
        this.balance = initialBalance;
        this.overdraftLimit = pickOverdraftLimit(paySim.random);
    }

    @Override
    public void step(SimState state) {
        PaySim paySim = (PaySim) state;
        int stepTargetCount = paySim.getStepTargetCount();
        if (stepTargetCount > 0) {
            MersenneTwisterFast random = paySim.random;
            int step = (int) state.schedule.getSteps();
            Map<String, Double> stepActionProfile = paySim.getStepProbabilities();

            int count = pickCount(random, stepTargetCount);

            for (int t = 0; t < count; t++) {
                String action = pickAction(random, stepActionProfile);
                StepActionProfile stepAmountProfile = paySim.getStepAction(action);
                double amount = pickAmount(random, action, stepAmountProfile);

                makeTransaction(paySim, step, action, amount);
            }
        }
    }

    private int pickCount(MersenneTwisterFast random, int targetStepCount) {
        // B(n,p): n = targetStepCount & p = clientWeight
        Binomial transactionNb = new Binomial(targetStepCount, clientWeight, random);
        return transactionNb.nextInt();
    }

    private String pickAction(MersenneTwisterFast random, Map<String, Double> stepActionProb) {
        Map<String, Double> clientProbabilities = clientProfile.getActionProbability();
        Map<String, Double> rawProbabilities = new HashMap<>();
        RandomCollection<String> actionPicker = new RandomCollection<>(random);

        // Pick the compromise between the Step distribution and the Client distribution
        for (Map.Entry<String, Double> clientEntry : clientProbabilities.entrySet()) {
            String action = clientEntry.getKey();
            double clientProbability = clientEntry.getValue();
            double rawProbability;

            if (stepActionProb.containsKey(action)) {
                double stepProbability = stepActionProb.get(action);

                rawProbability = (clientProbability + stepProbability) / 2;
            } else {
                rawProbability = clientProbability;
            }
            rawProbabilities.put(action, rawProbability);
        }

        // Correct the distribution so the balance of the account do not diverge too much
        double probInflow = 0;
        for (Map.Entry<String, Double> rawEntry : rawProbabilities.entrySet()) {
            String action = rawEntry.getKey();
            if (isInflow(action)) {
                probInflow += rawEntry.getValue();
            }
        }
        double probOutflow = 1 - probInflow;
        double newProbInflow = computeProbWithSpring(probInflow, probOutflow, balance);
        double newProbOutflow = 1 - newProbInflow;

        for (Map.Entry<String, Double> rawEntry : rawProbabilities.entrySet()) {
            String action = rawEntry.getKey();
            double rawProbability = rawEntry.getValue();
            double finalProbability;

            if (isInflow(action)) {
                finalProbability = rawProbability * newProbInflow / probInflow;
            } else {
                finalProbability = rawProbability * newProbOutflow / probOutflow;
            }
            actionPicker.add(finalProbability, action);
        }

        return actionPicker.next();
    }

    /**
     *  The Biased Bernoulli Walk we were doing can go far to the equilibrium of an account
     *  To avoid this we conceptually add a spring that would be attached to the equilibrium position of the account
     */
    private double computeProbWithSpring(double probUp, double probDown, double currentBalance){
        double equilibrium = 40 * expectedAvgTransaction; // Could also be the initial balance in other models
        double correctionStrength = 3 * Math.pow(10, -5); // In a physical model it would be 1 / 2 * kB * T
        double characteristicLengthSpring = equilibrium;
        double k = 1 / characteristicLengthSpring;
        double springForce = k * (equilibrium - currentBalance);
        double newProbUp = 0.5d * ( 1d + (expectedAvgTransaction * correctionStrength) * springForce + (probUp - probDown));

        if (newProbUp > 1){
           newProbUp = 1;
        } else if (newProbUp < 0){
            newProbUp = 0;
        }
        return newProbUp;

    }

    private boolean isInflow(String action){
        String[] inflowActions = {CASH_IN, DEPOSIT};
        return Arrays.stream(inflowActions)
                .anyMatch(action::equals);
    }

    private double pickAmount(MersenneTwisterFast random, String action, StepActionProfile stepAmountProfile) {
        ClientActionProfile clientAmountProfile = clientProfile.getProfilePerAction(action);

        double average, std;
        if (stepAmountProfile != null) {
            // We take the mean between the two distributions
            average = (clientAmountProfile.getAvgAmount() + stepAmountProfile.getAvgAmount()) / 2;
            std = Math.sqrt((Math.pow(clientAmountProfile.getStdAmount(), 2) + Math.pow(stepAmountProfile.getStdAmount(), 2))) / 2;
        } else {
            average = clientAmountProfile.getAvgAmount();
            std = clientAmountProfile.getStdAmount();
        }

        double amount = -1;
        while (amount <= 0) {
            amount = random.nextGaussian() * std + average;
        }

        return amount;
    }

    private void makeTransaction(PaySim state, int step, String action, double amount) {
        switch (action) {
            case CASH_IN:
                handleCashIn(state, step, amount);
                break;
            case CASH_OUT:
                handleCashOut(state, step, amount);
                break;
            case DEBIT:
                handleDebit(state, step, amount);
                break;
            case PAYMENT:
                handlePayment(state, step, amount);
                break;
            // For transfer transaction there is a limit so we have to split big transactions in smaller chunks
            case TRANSFER:
                Client clientTo = state.pickRandomClient(getName());
                double reducedAmount = amount;
                boolean lastTransferFailed = false;
                while (reducedAmount > Parameters.transferLimit && !lastTransferFailed) {
                    lastTransferFailed = !handleTransfer(state, step, Parameters.transferLimit, clientTo);
                    reducedAmount -= Parameters.transferLimit;
                }
                if (reducedAmount > 0 && !lastTransferFailed) {
                    handleTransfer(state, step, reducedAmount, clientTo);
                }
                break;
            case DEPOSIT:
                handleDeposit(state, step, amount);
                break;
            default:
                throw new UnsupportedOperationException("Action not implemented in Client");
        }
    }

    protected void handleCashIn(PaySim paysim, int step, double amount) {
        Merchant merchantTo = paysim.pickRandomMerchant();
        String nameOrig = this.getName();
        String nameDest = merchantTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        this.deposit(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(step, CASH_IN, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);
        paysim.getTransactions().add(t);
    }

    protected void handleCashOut(PaySim paysim, int step, double amount) {
        Merchant merchantTo = paysim.pickRandomMerchant();
        String nameOrig = this.getName();
        String nameDest = merchantTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        boolean isUnauthorizedOverdraft = this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(step, CASH_OUT, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        t.setUnauthorizedOverdraft(isUnauthorizedOverdraft);
        t.setFraud(this.isFraud());
        paysim.getTransactions().add(t);
    }

    protected void handleDebit(PaySim paysim, int step, double amount) {
        String nameOrig = this.getName();
        String nameDest = this.bank.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = this.bank.getBalance();

        boolean isUnauthorizedOverdraft = this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = this.bank.getBalance();

        Transaction t = new Transaction(step, DEBIT, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        t.setUnauthorizedOverdraft(isUnauthorizedOverdraft);
        paysim.getTransactions().add(t);
    }

    protected void handlePayment(PaySim paysim, int step, double amount) {
        Merchant merchantTo = paysim.pickRandomMerchant();

        String nameOrig = this.getName();
        String nameDest = merchantTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        boolean isUnauthorizedOverdraft = this.withdraw(amount);
        if (!isUnauthorizedOverdraft) {
            merchantTo.deposit(amount);
        }

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(step, PAYMENT, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        t.setUnauthorizedOverdraft(isUnauthorizedOverdraft);
        paysim.getTransactions().add(t);
    }

    protected boolean handleTransfer(PaySim paysim, int step, double amount, Client clientTo) {
        String nameOrig = this.getName();
        String nameDest = clientTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = clientTo.getBalance();

        boolean transferSuccessful;
        if (!isDetectedAsFraud(amount)) {
            boolean isUnauthorizedOverdraft = this.withdraw(amount);
            transferSuccessful = !isUnauthorizedOverdraft;
            if (transferSuccessful) {
                clientTo.deposit(amount);
            }

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientTo.getBalance();

            Transaction t = new Transaction(step, TRANSFER, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            t.setUnauthorizedOverdraft(isUnauthorizedOverdraft);
            t.setFraud(this.isFraud());
            paysim.getTransactions().add(t);
        } else { // create the transaction but don't move any money as the transaction was detected as fraudulent
            transferSuccessful = false;
            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientTo.getBalance();

            Transaction t = new Transaction(step, TRANSFER, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            t.setFlaggedFraud(true);
            t.setFraud(this.isFraud());
            paysim.getTransactions().add(t);
        }
        return transferSuccessful;
    }

    protected void handleDeposit(PaySim paysim, int step, double amount) {
        String nameOrig = this.getName();
        String nameDest = this.bank.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = this.bank.getBalance();

        this.deposit(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = this.bank.getBalance();

        Transaction t = new Transaction(step, DEPOSIT, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTransactions().add(t);
    }

    private boolean isDetectedAsFraud(double amount) {
        boolean isFraudulentAccount = false;
        if (this.countTransferTransactions >= MIN_NB_TRANSFER_FOR_FRAUD) {
            if (this.balanceMax - this.balance - amount > Parameters.transferLimit * 2.5) {
                isFraudulentAccount = true;
            }
        } else {
            this.countTransferTransactions++;
            this.balanceMax = max(this.balanceMax, this.balance);
        }
        return isFraudulentAccount;
    }

    private double pickOverdraftLimit(MersenneTwisterFast random){
        double stdTransaction = 0;

        for (String action: ActionTypes.getActions()){
            double actionProbability = clientProfile.getActionProbability().get(action);
            ClientActionProfile actionProfile = clientProfile.getProfilePerAction(action);
            expectedAvgTransaction += actionProfile.getAvgAmount() * actionProbability;
            stdTransaction += Math.pow(actionProfile.getStdAmount() * actionProbability, 2);
        }
        stdTransaction = Math.sqrt(stdTransaction);

        double randomizedMeanTransaction = random.nextGaussian() * stdTransaction + expectedAvgTransaction;

        return BalancesClients.getOverdraftLimit(randomizedMeanTransaction);
    }
}
