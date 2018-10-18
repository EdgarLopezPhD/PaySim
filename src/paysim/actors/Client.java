package paysim.actors;

import java.util.ArrayList;
import java.util.Map;

import paysim.*;
import paysim.base.ActionProbability;
import paysim.base.Transaction;
import paysim.parameters.Parameters;
import paysim.parameters.StepParameters;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Client extends SuperActor implements Steppable {
    private static final String CLIENT_IDENTIFIER = "C";
    private double[] probabilityArr;
    private Map<String, ActionProbability> actionProbabilities;
    private ArrayList<Integer> stepsToRepeat = new ArrayList<>();
    private double balanceFlag = 0; // steps before and balance
    private int countFlag = 0;

    public Client(String name) {
        super(CLIENT_IDENTIFIER + name);
    }

    @Override
    public void step(SimState state) {
        handleAction(state);

        // If the action is to be repeated, then make the repetitions
        if (cont != null) {
            handleRepetition(state);
        }
    }

    private void handleAction(SimState state) {
        PaySim paysim = (PaySim) state;
        int action = -1;

        while (action == -1) {
            action = chooseAction(paysim, probabilityArr);
        }

        ActionProbability prob;
        switch (action) {
            case 1:
                handleCashIn(paysim, paysim.getRandomMerchant());
                break;
            case 2:
                prob = actionProbabilities.get("CASH_OUT");
                if (prob != null)
                    handleCashOut(paysim, paysim.getRandomMerchant(),
                            this.getAmount(prob, paysim));
                break;
            case 3:
                handleDebit(paysim);
                break;
            case 4:
                handlePayment(paysim);
                break;
            case 5:
                prob = actionProbabilities.get("TRANSFER");

                if (prob != null) {
                    double amount = this.getAmount(prob, paysim);
                    double reducedAmount = amount;
                    int loops = (int) Math.ceil(amount / Parameters.transferLimit);
                    Client c = paysim.getRandomClient();
                    for (int i = 0; i < loops; i++) {
                        if (reducedAmount > Parameters.transferLimit) {
                            handleTransfer(paysim, c,
                                    Parameters.transferLimit);
                            reducedAmount -= Parameters.transferLimit;
                        } else {
                            handleTransfer(paysim, c,
                                    reducedAmount);
                        }
                    }
                }
                break;
            case 6:
                handleDeposit(paysim);
                break;
        }
    }

    private void handleRepetition(SimState state) {
        PaySim paysim = (PaySim) state;
        for (int currentStep : stepsToRepeat) {
            step = currentStep;

            switch (cont.getAction()) {
                case "CASH_IN":
                    handleCashInRepetition(paysim);
                    break;
                case "CASH_OUT":
                    handleCashOutRepetition(paysim);
                    break;
                case "DEBIT":
                    handleDebitRepetition(paysim);
                    break;
                case "PAYMENT":
                    handlePaymentRepetition(paysim);
                    break;
                case "TRANSFER":
                    handleTransferRepetition(paysim);
                    break;
                case "DEPOSIT":
                    handleDepositRepetition(paysim);
                    break;
            }
        }

    }

    private void handleCashIn(PaySim paysim, Merchant merchantTo) {
        String action = "CASH_IN";
        ActionProbability prob = actionProbabilities.get(action);

        if (prob != null) {
            String nameOrig = this.getName();
            String nameDest = merchantTo.getName();
            double oldBalanceOrig = this.getBalance();
            double oldBalanceDest = merchantTo.getBalance();

            double amount = this.getAmount(prob, paysim);
            this.deposit(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = merchantTo.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);
            paysim.getTransactions().add(t);
        }
    }

    void handleCashOut(PaySim paysim, Merchant merchantTo, double amount) {
        String action = "CASH_OUT";

        String nameOrig = this.getName();
        String nameDest = merchantTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);
        t.setFraud(this.isFraud());
        paysim.getTransactions().add(t);
    }

    private void handleDebit(PaySim paysim) {
        String action = "DEBIT";
        ActionProbability prob = actionProbabilities.get(action);

        if (prob != null) {
            String nameOrig = this.getName();
            String nameDest = this.getName();
            double oldBalanceOrig = this.getBalance();
            double oldBalanceDest = this.getBalance();

            double amount = this.getAmount(prob, paysim);
            this.withdraw(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = this.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            paysim.getTransactions().add(t);
        }
    }

    private void handlePayment(PaySim paysim) {
        String action = "PAYMENT";
        ActionProbability prob = actionProbabilities.get(action);
        if (prob != null) {
            Merchant merchantPayment = paysim.getRandomMerchant();

            String nameOrig = this.getName();
            String nameDest = merchantPayment.getName();
            double oldBalanceOrig = this.getBalance();
            double oldBalanceDest = merchantPayment.getBalance();

            double amount = this.getAmount(prob, paysim);
            this.withdraw(amount);
            merchantPayment.deposit(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = merchantPayment.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            paysim.getTransactions().add(t);
        }
    }

    void handleTransfer(PaySim paysim, Client clientTo, double amount) {
        String action = "TRANSFER";
        String nameOrig = this.getName();
        String nameDest = clientTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = clientTo.getBalance();

        if (!this.checkBalanceDropping(Parameters.transferLimit, amount)) {

            this.withdraw(amount);
            clientTo.deposit(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientTo.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            t.setFraud(this.isFraud());
            paysim.getTransactions().add(t);
        } else { // create the transaction but don't move any money
            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientTo.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            t.setFlaggedFraud(true);
            t.setFraud(this.isFraud());
            paysim.getTransactions().add(t);
        }
    }

    private void handleDeposit(PaySim paysim) {
        String action = "DEPOSIT";
        ActionProbability prob = actionProbabilities.get(action);

        if (prob != null) {
            double amount = this.getAmount(prob, paysim);
            Client clientToTransfer = getRandomClient(amount, paysim);

            String nameOrig = this.getName();
            String nameDest = clientToTransfer.getName();
            double oldBalanceOrig = this.getBalance();
            double oldBalanceDest = clientToTransfer.getBalance();


            clientToTransfer.withdraw(amount);
            this.deposit(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientToTransfer.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            paysim.getTransactions().add(t);
        }
    }

    private void handleCashInRepetition(PaySim paysim) {
        String action = "CASH_IN";
        double amount = getAmountRepetition(action, step, paysim);
        if (amount == -1) {
            return;
        }
        Merchant merchantHandlingTransac = paysim.getRandomMerchant();

        String nameOrig = this.getName();
        String nameDest = merchantHandlingTransac.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantHandlingTransac.getBalance();

        this.deposit(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantHandlingTransac.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTransactions().add(t);
    }

    private void handleCashOutRepetition(PaySim paysim) {
        String action = "CASH_OUT";
        double amount = getAmountRepetition(action, step, paysim);
        if (amount == -1) {
            return;
        }
        Merchant merchantHandlingTransac = paysim.getRandomMerchant();

        String nameOrig = this.getName();
        String nameDest = merchantHandlingTransac.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantHandlingTransac.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantHandlingTransac.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        t.setFraud(this.isFraud());
        paysim.getTransactions().add(t);
    }

    private void handleDebitRepetition(PaySim paysim) {
        String action = "DEBIT";
        double amount = getAmountRepetition(action, step, paysim);
        if (amount == -1) {
            return;
        }
        String nameOrig = this.getName();
        String nameDest = this.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = this.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = this.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTransactions().add(t);
    }

    private void handlePaymentRepetition(PaySim paysim) {
        String action = "PAYMENT";
        double amount = getAmountRepetition(action, step, paysim);
        if (amount == -1) {
            return;
        }
        Merchant merchantPayment = paysim.getRandomMerchant();

        String nameOrig = this.getName();
        String nameDest = merchantPayment.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantPayment.getBalance();

        this.withdraw(amount);
        merchantPayment.deposit(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantPayment.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTransactions().add(t);
    }

    private void handleTransferRepetition(PaySim paysim) {
        String action = "TRANSFER";
        double amount = getAmountRepetition(action, step, paysim);
        if (amount == -1) {
            return;
        }
        double reducedAmount = amount;
        int loops = (int) Math.ceil(amount / Parameters.transferLimit);

        Client clientDest = this.getRandomClient(amount, paysim);

        for (int i = 0; i < loops; i++) {
            if (reducedAmount > Parameters.transferLimit) {
                handleTransfer(paysim, clientDest,
                        Parameters.transferLimit);
                reducedAmount -= Parameters.transferLimit;
            } else {
                handleTransfer(paysim, clientDest,
                        reducedAmount);
            }
        }
    }

    private void handleDepositRepetition(PaySim paysim) {
        String action = "DEPOSIT";
        double amount = getAmountRepetition(action, step, paysim);
        if (amount == -1) {
            return;
        }
        Client clientToTransfer = getRandomClient(amount, paysim);

        String nameOrig = this.getName();
        String nameDest = clientToTransfer.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = clientToTransfer.getBalance();

        clientToTransfer.withdraw(amount);
        this.deposit(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = clientToTransfer.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), action, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTransactions().add(t);
    }

    public void setProbabilityArr(double[] probabilityArr) {
        this.probabilityArr = probabilityArr;
    }

    private boolean checkBalanceDropping(double transLimit, double amount) {
        boolean flag = false;
        if (this.countFlag >= 3) { // check for fraud
            if (this.balanceFlag - this.balance - amount > transLimit * 2.5) {
                flag = true;
            }
        } else {
            this.countFlag++;
            if (this.balanceFlag == 0) {
                this.balanceFlag = this.balance;
            }
            if (this.balanceFlag < this.balance) {
                this.balanceFlag = this.balance;
            }
        }
        return flag;
    }

    // Handler functions for repetition

    private Client getRandomClient(double amount, PaySim paysim) {
        Client clientToTransfer;
        int counter = 0;
        do {
            clientToTransfer = paysim.getClients().get(
                    paysim.random.nextInt(paysim.getClients().size()));
            counter++;
            if (counter > 50000) {
                break;
            }
        } while (clientToTransfer.getBalance() < amount);

        return clientToTransfer;
    }

    public void setActionProbabilities(Map<String, ActionProbability> actionProbabilities) {
        this.actionProbabilities = actionProbabilities;
    }

    private double getAmount(ActionProbability prob, PaySim paysim) {
        double amount = -1;

        while (amount <= 0) {
            amount = paysim.random.nextGaussian() * prob.getStd()
                    + prob.getAverage();
        }

        return amount;
    }

    private double getAmountRepetition(String action, int step, PaySim paysim) {
        ActionProbability actionProbability = StepParameters.get(step).get(action);
        if (actionProbability == null) {
            return -1;
        }
        double amount = -1;
        while (amount <= 0) {
            amount = paysim.random.nextGaussian()
                    * actionProbability.getStd()
                    + actionProbability.getAverage();
        }

        return amount;
    }

    public ArrayList<Integer> getStepsToRepeat() {
        return stepsToRepeat;
    }

    public void setStepsToRepeat(ArrayList<Integer> stepsToRepeat) {
        this.stepsToRepeat = stepsToRepeat;
    }
}
