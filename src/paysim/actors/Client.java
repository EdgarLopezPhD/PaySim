package paysim.actors;

import java.util.ArrayList;

import paysim.*;
import paysim.aggregation.AggregateTransactionRecord;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Client extends SuperClient implements Steppable {

    private String name = "";
    private double[] probabilityArr;
    private ArrayList<ActionProbability> probList;
    private ArrayList<Integer> stepsToRepeat = new ArrayList<>();
    private ArrayList<String> paramFile = new ArrayList<>();
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

        // Based on the calculated probability, an action is chosen
        do {
            action = this.chooseAction(paysim, this.probabilityArr);
            if (action == -1) {
                if (this.probabilityArr.length == 0) {
                    return;
                }
            }
        } while (action == -1);

        ActionProbability prob;
        switch (action) {

            // CASH_IN
            case 1:
                handleCashIn(paysim, paysim.getRandomClient());
                break;

            // CASH_OUT
            case 2:
                prob = getProb("CASH_OUT", paysim);
                if (prob != null)
                    handleCashOut(paysim, paysim.getRandomClient(),
                            this.getAmount(prob, paysim));
                break;

            // DEBIT
            case 3:
                handleDebit(paysim);
                break;

            // PAYMENT
            case 4:
                handlePayment(paysim);
                break;

            // TRANSFER
            case 5:
                prob = getProb("TRANSFER", paysim);

                if (prob != null) {
                    double amount = this.getAmount(prob, paysim);
                    double reducedAmount = amount;
                    int loops = (int) Math.ceil(amount / paysim.transferLimit);
                    Client c = paysim.getRandomClient();
                    for (int i = 0; i < loops; i++) {
                        if (reducedAmount > paysim.transferLimit) {
                            handleTransfer(paysim, c,
                                    paysim.transferLimit);
                            reducedAmount -= paysim.transferLimit;
                        } else {
                            handleTransfer(paysim, c,
                                    reducedAmount);
                        }
                    }
                }
                break;

        }
    }

    public void handleRepetition(SimState state) {
        PaySim paysim = (PaySim) state;

        // Based on the nr of times to repeat, make the repetition
        for (int i = 0; i < this.stepsToRepeat.size(); i++) {
            // System.out.println("Type:\t" + cont.getType() + "\n");
            int currentStep = this.stepsToRepeat.get(i);
            this.currDay = (int) (currentStep / 24) + 1;
            this.currHour = (int) (currentStep - ((this.currDay - 1) * 24));

            switch (this.cont.getType()) {
                // CASH_IN
                case "CASH_IN":
                    this.currType = "CASH_IN";
                    handleCashInRepetition(paysim);
                    break;

                // CASH_OUT
                case "CASH_OUT":
                    this.currType = "CASH_OUT";
                    handleCashOutRepetition(paysim);
                    break;

                // DEBIT
                case "DEBIT":
                    this.currType = "DEBIT";
                    handleDebitRepetition(paysim);
                    break;

                // DEPOSIT
                case "DEPOSIT":
                    this.currType = "DEPOSIT";
                    handleDepositRepetition(paysim);
                    break;

                // PAYMENT
                case "PAYMENT":
                    this.currType = "PAYMENT";
                    handlePaymentRepetition(paysim);
                    break;

                // TRANSFER
                case "TRANSFER":
                    this.currType = "TRANSFER";
                    handleTrasferRepetition(paysim);
                    break;

            }
        }

    }

    public void setClient(Client c) {
        balance = c.getBalance();
        currStep = c.getCurrStep();
        name = c.getName();
        numDeposits = c.getNumDeposits();
        numTransfers = c.getNumTransfers();
        numWithdraws = c.getNumWithdraws();
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
        return "C" + Integer.toString(this.hashCode());
    }

    public void handleCashIn(PaySim paysim, SuperClient clientTo) {
        // Get the probabilities that correspond to that current day
        ActionProbability prob = getProb("CASH_IN", paysim);

        // With the probability for that day gained, get the next random number
        // in that distribution
        if (prob != null) {
            double amount = this.getAmount(prob, paysim);
            Client clientToTransferAfter = (Client) clientTo;
            Client clientToTransferBefore = new Client();
            clientToTransferBefore.setClient(clientToTransferAfter);

            Client before = new Client();
            before.setClient(this);
            clientToTransferAfter.withdraw(amount);
            this.deposit(amount);

            Transaction t2 = new Transaction(paysim.schedule.getSteps(),
                    before, this, 1, amount, "CashIn");
            t2.setClientDestAfter(clientToTransferAfter);
            t2.setClientDestBefore(clientToTransferBefore);
            t2.setDay(this.currDay);
            t2.setHour(this.currHour);
            paysim.getTrans().add(t2);
        } else {
            Manager.nrFailed++;
            printActionProbability();
        }
    }

    public void handleCashOut(PaySim paysim, Client clientTo, double amount) {
        Client clientToTransferAfter = clientTo;
        Client clientToTransferBefore = new Client();
        clientToTransferBefore.setClient(clientToTransferAfter);

        Client before = new Client();
        before.setClient(this);
        this.withdraw(amount);
        clientToTransferAfter.deposit(amount);

        Transaction t2 = new Transaction(paysim.schedule.getSteps(), before,
                this, 2, amount, "CashOut");
        t2.setDay(this.currDay);
        t2.setHour(this.currHour);
        t2.setClientDestAfter(clientToTransferAfter);
        t2.setClientDestBefore(clientToTransferBefore);
        t2.setFraud(this.isFraud());
        paysim.getTrans().add(t2);
    }

    public void handleDeposit(PaySim paysim) {
        // Get the probabilities that correspond to that current day
        // System.out.println("Dumping..\n\n");

        // for(ActionProbability temp: paysim.getaProbList()){
        // System.out.println(temp.toString() + "\n");
        // }
        ActionProbability prob = getProb("DEPOSIT", paysim);

        if (prob != null) {
            // With the probability for that day gained, get the next random
            // number in that distribution
            double amount = this.getAmount(prob, paysim);

            // Store the client before the deposit to keep track of the previous
            // balance
            Client clientToTransferAfter = getRandomClient(amount, paysim);
            Client clientToTransferBefore = new Client();
            clientToTransferBefore.setClient(clientToTransferAfter);

            Client before = new Client();
            before.setClient(this);
            clientToTransferAfter.withdraw(amount);
            this.deposit(amount);
            this.numDeposits++;

            Transaction t2 = new Transaction(paysim.schedule.getSteps(),
                    before, this, 4, amount, "Deposit");
            t2.setClientDestAfter(clientToTransferAfter);
            t2.setClientDestBefore(clientToTransferBefore);
            t2.setDay(this.currDay);
            t2.setHour(this.currHour);
            paysim.getTrans().add(t2);
            // System.out.println("Lenght of prob arr CORRECT:\t" +
            // this.probabilityArr.length + "\n");
            // printActionProbability();
        } else {
            // System.out.println("Lenght of prob arr NOT-CORRECT:\t" +
            // this.probabilityArr.length + "\n");
            // Manager.nrFailed++;
            // System.out.println("Curr Prob Type:\tDEPOSIT" + "\n"
            // + "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
            // );
            printActionProbability();
        }

    }

    public void handlePayment(PaySim paysim) {

        Merchant mAfter = paysim.getRandomMerchant();
        Merchant merchantToTransferBefore = new Merchant();
        merchantToTransferBefore.setMerchant(mAfter);
        ActionProbability prob = getProb("PAYMENT", paysim);

        if (prob != null) {
            double amount = this.getAmount(prob, paysim);

            Client before = new Client();
            before.setClient(this);
            this.withdraw(amount);
            mAfter.deposit(amount);

            Transaction t2 = new Transaction(paysim.schedule.getSteps(),
                    before, this, 5, amount, "Payment");
            t2.setMerchantAfter(mAfter);
            t2.setMerchantBefore(merchantToTransferBefore);
            t2.setDay(this.currDay);
            t2.setHour(this.currHour);
            paysim.getTrans().add(t2);
            // System.out.println("Lenght of prob arr CORRECT:\t" +
            // this.probabilityArr.length + "\n");
            // printActionProbability();
        } else {
            // System.out.println("Lenght of prob arr NOT-CORRECT:\t" +
            // this.probabilityArr.length + "\n");
            // Manager.nrFailed++;
            // System.out.println("Curr Prob Type:\tPAYMENT" + "\n"
            // + "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
            // );
            printActionProbability();
        }

    }

    public void handleTransfer(PaySim paysim, Client clientTo, double amount) {
        // Get the probabilities that correspond to that current day
        if (!this.checkBalanceDropping(paysim.transferLimit, amount)) {
            Client clientToTransferAfter = clientTo;
            Client clientToTransferBefore = new Client();
            clientToTransferBefore.setClient(clientToTransferAfter);

            Client before = new Client();
            before.setClient(this);
            this.withdraw(amount);
            clientToTransferAfter.deposit(amount);

            Transaction t2 = new Transaction(paysim.schedule.getSteps(),
                    before, this, 6, amount, "Transfer");
            t2.setDay(this.currDay);
            t2.setHour(this.currHour);
            t2.setClientDestAfter(clientToTransferAfter);
            t2.setClientDestBefore(clientToTransferBefore);
            t2.setFraud(this.isFraud());
            paysim.getTrans().add(t2);
        } else { // create the transaction but dont move any money
            Client clientToTransferAfter = clientTo;
            Client clientToTransferBefore = new Client();
            clientToTransferBefore.setClient(clientToTransferAfter);

            Client before = new Client();
            before.setClient(this);
            // this.withdraw(amount);
            // clientToTransferAfter.deposit(amount);

            Transaction t2 = new Transaction(paysim.schedule.getSteps(),
                    before, this, 6, amount, "Transfer");
            t2.setDay(this.currDay);
            t2.setHour(this.currHour);
            t2.setClientDestAfter(clientToTransferAfter);
            t2.setClientDestBefore(clientToTransferBefore);
            t2.setFlaggedFraud(true);
            t2.setFraud(this.isFraud());
            paysim.getTrans().add(t2);
        }
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

    public void handleDebit(PaySim paysim) {
        // Get the probabilities that correspond to that current day
        ActionProbability prob = getProb("DEBIT", paysim);

        // With the probability for that day gained, get the next random number
        // in that distribution
        if (prob != null) {
            double amount = this.getAmount(prob, paysim);
            Client clientToTransferAfter = getRandomClient(amount, paysim);
            Client clientToTransferBefore = new Client();
            clientToTransferBefore.setClient(clientToTransferAfter);

            Client before = new Client();
            before.setClient(this);
            this.withdraw(amount);
            clientToTransferAfter.deposit(amount);

            Transaction t2 = new Transaction(paysim.schedule.getSteps(),
                    before, this, 3, amount, "Debit");
            t2.setClientDestBefore(clientToTransferBefore);
            t2.setClientDestAfter(clientToTransferAfter);
            t2.setDay(this.currDay);
            t2.setHour(this.currHour);
            paysim.getTrans().add(t2);
            // System.out.println("Lenght of prob arr CORRECT:\t" +
            // this.probabilityArr.length + "\n");
            // printActionProbability();
        } else {
            // System.out.println("Lenght of prob arr NOT-CORRECT:\t" +
            // this.probabilityArr.length + "\n");
            // Manager.nrFailed++;
            // System.out.println("Curr Prob Type:\tDEBIT" + "\n"
            // + "Day: " + this.currDay + "\tHour:" + this.currHour + "\n"
            // );
            printActionProbability();
        }

    }

    // Handler functions for repetition

    public void handleCashInRepetition(PaySim paysim) {
        double amount = this.getAmountRepetition(this.currType, this.currDay,
                this.currHour, paysim);
        if (amount == -1) {
            return;
        }

        Client clientToTransferAfter = getRandomClient(amount, paysim);
        Client clientToTransferBefore = new Client();
        clientToTransferBefore.setClient(clientToTransferAfter);

        Client before = new Client();
        before.setClient(this);
        clientToTransferAfter.withdraw(amount);
        this.deposit(amount);

        Transaction t2 = new Transaction(paysim.schedule.getSteps(), before,
                this, 1, amount, "CashIn");
        t2.setClientDestAfter(clientToTransferAfter);
        t2.setClientDestBefore(clientToTransferBefore);
        t2.setDay(this.currDay);
        t2.setHour(this.currHour);
        paysim.getTrans().add(t2);

    }

    public void handleCashOutRepetition(PaySim paysim) {
        double amount = this.getAmountRepetition(this.currType, this.currDay,
                this.currHour, paysim);
        if (amount == -1) {
            return;
        }

        Client clientToTransferAfter = getRandomClient(amount, paysim);
        Client clientToTransferBefore = new Client();
        clientToTransferBefore.setClient(clientToTransferAfter);

        Client before = new Client();
        before.setClient(this);
        this.withdraw(amount);
        clientToTransferAfter.deposit(amount);

        Transaction t2 = new Transaction(paysim.schedule.getSteps(), before,
                this, 2, amount, "CashOut");
        t2.setDay(this.currDay);
        t2.setHour(this.currHour);
        t2.setClientDestAfter(clientToTransferAfter);
        t2.setClientDestBefore(clientToTransferBefore);
        paysim.getTrans().add(t2);
    }

    public void handleDebitRepetition(PaySim paysim) {
        double amount = this.getAmountRepetition(this.currType, this.currDay,
                this.currHour, paysim);
        if (amount == -1) {
            return;
        }

        Client clientToTransferAfter = getRandomClient(amount, paysim);
        Client clientToTransferBefore = new Client();
        clientToTransferBefore.setClient(clientToTransferAfter);

        Client before = new Client();
        before.setClient(this);
        this.withdraw(amount);
        clientToTransferAfter.deposit(amount);

        Transaction t2 = new Transaction(paysim.schedule.getSteps(), before,
                this, 3, amount, "Debit");
        t2.setClientDestBefore(clientToTransferBefore);
        t2.setClientDestAfter(clientToTransferAfter);
        t2.setDay(this.currDay);
        t2.setHour(this.currHour);
        paysim.getTrans().add(t2);
    }

    public void handleDepositRepetition(PaySim paysim) {
        double amount = this.getAmountRepetition(this.currType, this.currDay,
                this.currHour, paysim);
        if (amount == -1) {
            return;
        }

        // Store the client before the deposit to keep track of the previous
        // balance
        Client clientToTransferAfter = getRandomClient(amount, paysim);
        Client clientToTransferBefore = new Client();
        clientToTransferBefore.setClient(clientToTransferAfter);

        Client before = new Client();
        before.setClient(this);
        clientToTransferAfter.withdraw(amount);
        this.deposit(amount);
        this.numDeposits++;

        Transaction t2 = new Transaction(paysim.schedule.getSteps(), before,
                this, 4, amount, "Deposit");
        t2.setClientDestAfter(clientToTransferAfter);
        t2.setClientDestBefore(clientToTransferBefore);
        t2.setDay(this.currDay);
        t2.setHour(this.currHour);
        paysim.getTrans().add(t2);
    }

    public void handlePaymentRepetition(PaySim paysim) {

        Merchant mAfter = paysim.getRandomMerchant();
        Merchant merchantToTransferBefore = new Merchant();
        merchantToTransferBefore.setMerchant(mAfter);

        double amount = this.getAmountRepetition(this.currType, this.currDay,
                this.currHour, paysim);
        if (amount == -1) {
            return;
        }

        Client before = new Client();
        before.setClient(this);
        this.withdraw(amount);
        mAfter.deposit(amount);

        Transaction t2 = new Transaction(paysim.schedule.getSteps(), before,
                this, 5, amount, "Payment");
        t2.setMerchantAfter(mAfter);
        t2.setMerchantBefore(merchantToTransferBefore);
        t2.setDay(this.currDay);
        t2.setHour(this.currHour);
        paysim.getTrans().add(t2);
    }

    public void handleTrasferRepetition(PaySim paysim) {
        double amount = this.getAmountRepetition(this.currType, this.currDay,
                this.currHour, paysim);
        if (amount == -1) {
            return;
        }
        try {
            Client clientToTransferAfter = getRandomClient(amount, paysim);
            Client clientToTransferBefore = new Client();
            clientToTransferBefore.setClient(clientToTransferAfter);

            Client before = new Client();
            before.setClient(this);
            this.withdraw(amount);
            clientToTransferAfter.deposit(amount);

            Transaction t2 = new Transaction(paysim.schedule.getSteps(),
                    before, this, 6, amount, "Transfer");
            t2.setDay(this.currDay);
            t2.setHour(this.currHour);
            t2.setClientDestAfter(clientToTransferAfter);
            t2.setClientDestBefore(clientToTransferBefore);
            paysim.getTrans().add(t2);
        } catch (Exception e) {
            e.printStackTrace();
            // System.out.println("returned\n");
            return;
        }

    }

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

    private double getAmountRepetition(String type, int day, int hour,
                                       PaySim paysim) {
        AggregateTransactionRecord transRecord = this.stepHandler.getRecord(
                type, day, hour);
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

    private ActionProbability getProb(String probToGet, PaySim p) {

        for (ActionProbability temp : this.probList) {
            if (temp.getType().equals(probToGet)) {
                return temp;
            }
        }
        return null;
    }

    private void printActionProbability() {
        System.out.println("Printing prob\n");
        for (ActionProbability p : this.probList) {
            System.out.println(p.getType() + "\n" + p.getNrOfTransactions()
                    + "\n");
        }
        System.out.println("\n\n");
    }

    public void setParamFile(ArrayList<String> paramFile) {
        this.paramFile = paramFile;
    }

    public void setStepHandler(CurrentStepHandler stepHandler) {
        this.stepHandler = stepHandler;
    }
}
