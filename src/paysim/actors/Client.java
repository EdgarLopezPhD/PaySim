package paysim.actors;

import java.util.ArrayList;

import paysim.*;
import paysim.aggregation.AggregateTransactionRecord;
import paysim.base.ActionProbability;
import paysim.parameters.Parameters;
import sim.engine.SimState;
import sim.engine.Steppable;

import static java.lang.Math.abs;

public class Client extends SuperClient implements Steppable {
    private String name = "";
    private double[] probabilityArr;
    private ArrayList<ActionProbability> probList;
    private ArrayList<Integer> stepsToRepeat = new ArrayList<>();
    String currType = "";
    private CurrentStepHandler stepHandler = null;
    private double balanceFlag = 0; // steps before and balance
    private int countFlag = 0;

    @Override
    public void step(SimState state) {
        handleAction(state);

        // If the action is to be repeated, then make the repetitions
        if (cont != null) {
            handleRepetition(state);
        }
    }

    public void handleAction(SimState state) {
        PaySim paysim = (PaySim) state;
        int action = 0;

        do {
            action = chooseAction(paysim, probabilityArr);
        } while (action == -1);

        ActionProbability prob;
        switch (action) {
            case 1:
                handleCashIn(paysim, paysim.getRandomMerchant());
                break;
            case 2:
                prob = getProb("CASH_OUT");
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
                prob = getProb("TRANSFER");

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

    public void handleRepetition(SimState state) {
        PaySim paysim = (PaySim) state;
        for (int currentStep : stepsToRepeat) {
            currStep = currentStep;

            switch (this.cont.getType()) {
                case "CASH_IN":
                    this.currType = "CASH_IN";
                    handleCashInRepetition(paysim);
                    break;
                case "CASH_OUT":
                    this.currType = "CASH_OUT";
                    handleCashOutRepetition(paysim);
                    break;
                case "DEBIT":
                    this.currType = "DEBIT";
                    handleDebitRepetition(paysim);
                    break;
                case "PAYMENT":
                    this.currType = "PAYMENT";
                    handlePaymentRepetition(paysim);
                    break;
                case "TRANSFER":
                    this.currType = "TRANSFER";
                    handleTransferRepetition(paysim);
                    break;
                case "DEPOSIT":
                    this.currType = "DEPOSIT";
                    handleDepositRepetition(paysim);
                    break;
            }
        }

    }

    public void handleCashIn(PaySim paysim, Merchant merchantTo) {
        String type = "CASH_IN";
        ActionProbability prob = getProb(type);

        if (prob != null) {
            String nameOrig = this.getName();
            String nameDest = merchantTo.getName();
            double oldBalanceOrig = this.getBalance();
            double oldBalanceDest = merchantTo.getBalance();

            double amount = this.getAmount(prob, paysim);
            this.deposit(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = merchantTo.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);
            paysim.getTrans().add(t);
        }
    }

    public void handleCashOut(PaySim paysim, Merchant merchantTo, double amount) {
        String type = "CASH_OUT";

        String nameOrig = this.getName();
        String nameDest = merchantTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);
        t.setFraud(this.isFraud());
        paysim.getTrans().add(t);
    }

    public void handleDebit(PaySim paysim) {
        String type = "DEBIT";
        ActionProbability prob = getProb(type);

        if (prob != null) {
            String nameOrig = this.getName();
            String nameDest = this.getName();
            double oldBalanceOrig = this.getBalance();
            double oldBalanceDest = this.getBalance();

            double amount = this.getAmount(prob, paysim);
            this.withdraw(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = this.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            paysim.getTrans().add(t);
        }
    }

    public void handlePayment(PaySim paysim) {
        String type = "PAYMENT";
        ActionProbability prob = getProb(type);
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

            Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            paysim.getTrans().add(t);
        }
    }

    public void handleTransfer(PaySim paysim, Client clientTo, double amount) {
        String type = "TRANSFER";
        String nameOrig = this.getName();
        String nameDest = clientTo.getName();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = clientTo.getBalance();

        if (!this.checkBalanceDropping(Parameters.transferLimit, amount)) {

            this.withdraw(amount);
            clientTo.deposit(amount);

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientTo.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            t.setFraud(this.isFraud());
            paysim.getTrans().add(t);
        } else { // create the transaction but don't move any money
            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientTo.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            t.setFlaggedFraud(true);
            t.setFraud(this.isFraud());
            paysim.getTrans().add(t);
        }
    }

    public void handleDeposit(PaySim paysim) {
        String type = "DEPOSIT";
        ActionProbability prob = getProb(type);

        if (prob != null) {
            double amount = this.getAmount(prob, paysim);
            Client clientToTransfer = getRandomClient(amount, paysim);

            String nameOrig = this.getName();
            String nameDest = clientToTransfer.getName();
            double oldBalanceOrig = this.getBalance();
            double oldBalanceDest = clientToTransfer.getBalance();


            clientToTransfer.withdraw(amount);
            this.deposit(amount);
            this.numDeposits++;

            double newBalanceOrig = this.getBalance();
            double newBalanceDest = clientToTransfer.getBalance();

            Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                    newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

            paysim.getTrans().add(t);
        }
    }

    public void handleCashInRepetition(PaySim paysim) {
        String type = "CASH_IN";
        double amount = getAmountRepetition(currType, currStep, paysim);
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

        Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTrans().add(t);
    }

    public void handleCashOutRepetition(PaySim paysim) {
        String type = "CASH_OUT";
        double amount = getAmountRepetition(currType, currStep, paysim);
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

        Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        t.setFraud(this.isFraud());
        paysim.getTrans().add(t);
    }

    public void handleDebitRepetition(PaySim paysim) {
        String type = "DEBIT";
        double amount = getAmountRepetition(currType, currStep, paysim);
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

        Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTrans().add(t);
    }

    public void handlePaymentRepetition(PaySim paysim) {
        String type = "PAYMENT";
        double amount = this.getAmountRepetition(currType, currStep, paysim);
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

        Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTrans().add(t);
    }

    public void handleTransferRepetition(PaySim paysim) {
        String type = "TRANSFER";
        double amount = this.getAmountRepetition(currType, currStep, paysim);
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

    public void handleDepositRepetition(PaySim paysim) {
        String type = "DEPOSIT";
        double amount = getAmountRepetition(currType, currStep, paysim);
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
        this.numDeposits++;

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = clientToTransfer.getBalance();

        Transaction t = new Transaction(paysim.schedule.getSteps(), type, amount, nameOrig, oldBalanceOrig,
                newBalanceOrig, nameDest, oldBalanceDest, newBalanceDest);

        paysim.getTrans().add(t);
    }

    public void setProbabilityArr(double[] probabilityArr) {
        this.probabilityArr = probabilityArr;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (this.name.equals(""))
            this.name = this.toString();
        return this.name;
    }

    public String toString() {
        return "C" + Integer.toString(abs(this.hashCode()));
    }

    public boolean checkBalanceDropping(double transLimit, double amount) {
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

    public Client getRandomClient(double amount, PaySim paysim) {
        Client clientToTransfer = new Client();
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

    public void setProbList(ArrayList<ActionProbability> probList) {
        this.probList = probList;
    }

    private double getAmount(ActionProbability prob, PaySim paysim) {
        double amount = 0;

        do {
            amount = paysim.random.nextGaussian() * prob.getStd()
                    + prob.getAverage();
        } while (amount <= 0);

        return amount;
    }

    private double getAmountRepetition(String type, int step, PaySim paysim) {
        AggregateTransactionRecord transRecord = this.stepHandler.getRecord(type, step);
        if (transRecord == null) {
            return -1;
        }
        double amount = 0;
        do {
            amount = paysim.random.nextGaussian()
                    * Double.parseDouble(transRecord.gettStd())
                    + Double.parseDouble(transRecord.gettAvg());
        } while (amount <= 0);

        return amount;
    }

    public ArrayList<Integer> getStepsToRepeat() {
        return stepsToRepeat;
    }

    public void setStepsToRepeat(ArrayList<Integer> stepsToRepeat) {
        this.stepsToRepeat = stepsToRepeat;
    }

    private ActionProbability getProb(String probToGet) {
        for (ActionProbability temp : probList) {
            if (temp.getType().equals(probToGet)) {
                return temp;
            }
        }
        return null;
    }

    public void setStepHandler(CurrentStepHandler stepHandler) {
        this.stepHandler = stepHandler;
    }
}
