package paysim;

import java.util.ArrayList;

import paysim.actors.Client;
import paysim.aggregation.AggregateTransactionRecord;
import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import static java.lang.Math.abs;

public class Manager implements Steppable {
    //For debugging purposes
    public static int trueNrOfClients = 0;
    public static int trueNrOfRepetitions = 0;
    public static int nrOfDaysParticipated = 0;

    ProbabilityContainerHandler probabilityHandler = new ProbabilityContainerHandler();

    int currDay = 0;
    int currHour = 0;
    private InitBalanceHandler balanceHandler;
    private CurrentStepHandler stepHandler;

    public void step(SimState state) {
        /*
         * Algorithm
         *
         * 1) Get the probabilities to load from the current step
         * 2) From that, get the number of clients to allocate at the next step
         * 3) For each client that is created, make sure there is a 10% chance of that client to re-enter after x-amount of steps
         */
        PaySim paysim = (PaySim) state;

        //Get the current step number in the simulation
        int currStep = (int) paysim.schedule.getSteps() + 1;

        //Get the corresponding probabilities for that step from the parameter file
        ArrayList<ActionProbability> aProbList = getActionProbabilityFromStep(currStep);

        //Get the number of clients to load from aProbList
        int nrOfClients = getNrOfClients(aProbList);
        trueNrOfClients += nrOfClients;

        nrOfClients *= Parameters.multiplier;

        //FIX THIS
        double probArr[] = paysim.loadProbabilites(aProbList, nrOfClients);

        //If there are no clients to repeat, "-1" is returned, hence, if its -1, nrOfClients should remain 0 because there are originally
        //no transactions to be executed at that step.
        int nrOfTimesToReduce = stepHandler.getNrOfTimesToReduce(currStep);
        if (nrOfTimesToReduce != -1) {
            nrOfClients -= nrOfTimesToReduce;
        }
        Manager.trueNrOfRepetitions += nrOfTimesToReduce;

        for (int i = 0; i < nrOfClients; i++) {
            Client c = this.generateClient(probArr, aProbList, paysim, currStep);
            if (c.getStepsToRepeat().size() != 0) {
                paysim.getClients().add(c);
            }
            paysim.schedule.scheduleOnce(c);
        }

        updatePaysimOutputs(paysim);
    }

    private void updatePaysimOutputs(PaySim paysim) {
        ArrayList<AggregateTransactionRecord> records = paysim.getAggregateCreator().
                generateAggregateParamFile(paysim.getTrans());

        if (records.size() > 0) {
            Manager.nrOfDaysParticipated++;
        }
        for (int i = 0; i < records.size(); i++) {
            paysim.getAggrTransRecordList().add(records.get(i));
        }

        Output.writeLog(paysim.logFileName, paysim.trans);
        if (Parameters.saveToDB) {
            Output.writeDatabaseLog(Parameters.dbUrl, Parameters.dbUser, Parameters.dbPassword, paysim.trans);
        }
        paysim.resetVariables();
    }

    private Client generateClient(double probArr[], ArrayList<ActionProbability> aProbList, PaySim paysim, int currStep) {
        //Create the client
        Client generatedClient = new Client();
        generatedClient.setStepHandler(stepHandler);
        generatedClient.setProbabilityArr(probArr);
        generatedClient.setProbList(aProbList);
        generatedClient.setName("C" + String.valueOf(abs(String.valueOf(System.currentTimeMillis()).hashCode())));
        generatedClient.setBalance(balanceHandler.getBalance());
        generatedClient.setCurrDay(currDay);
        generatedClient.setCurrHour(currHour);


        Repetition cont = TransactionParameters.getRepetition();
        //Check whether the action is to be repeated
        if (cont.getLow() == 1 && cont.getHigh() == 1) {
            return generatedClient;
        } else {
            int nrOfTimesToRepeat = 0;

            //Get how many times to repeat
            if ((cont.getLow() - cont.getHigh()) == 0) {
                nrOfTimesToRepeat = (int) cont.getLow();
            } else {
                int randNr = paysim.random.nextInt() % ((int) (cont.getHigh() - cont.getLow()));
                if (randNr < 0) {
                    randNr *= -1;
                }
                nrOfTimesToRepeat = (int) (cont.getLow() + randNr);
                //Check if the randomized nr of times to be repeated exceeds the max
                int maxTimesType = TransactionParameters.getMaxOccurenceGivenType(cont.getType());
                if (nrOfTimesToRepeat > maxTimesType) {
                    nrOfTimesToRepeat = maxTimesType;
                }
                //System.out.println("High:\t" + cont.getHigh() + "\tLow:\t" + cont.getLow() + "\tRandomizedInBetween:\t" + nrOfTimesToRepeat + "\n");

            }
            nrOfTimesToRepeat *= Parameters.multiplier;
            ArrayList<Integer> stepsToRepeat = this.stepHandler.getSteps(currStep, nrOfTimesToRepeat);
            if (stepsToRepeat == null) {
                return generatedClient;
            }
            generatedClient.setStepsToRepeat(stepsToRepeat);
            generatedClient.setCont(cont);
            return generatedClient;
        }

    }

    public ArrayList<ActionProbability> getActionProbabilityFromStep(int step) {
        int day = (step / 24) + 1;
        int hour = step - ((day - 1) * 24);
        currDay = day;
        currHour = hour;

        //FIX THIS CHANGE THIS TO GET DIRECTLY FROM THE CACHED CONTAINER
        ProbabilityRecordContainer cont = probabilityHandler.getList().get(step - 1);
        ArrayList<ActionProbability> probList = cont.getProbList();

        return probList;
    }

    public void setBalanceHandler(InitBalanceHandler balanceHandler) {
        this.balanceHandler = balanceHandler;
    }

    public int getNrOfClients(ArrayList<ActionProbability> probList) {
        int nrOfClients = 0;

        for (ActionProbability p : probList) {
            nrOfClients += p.getNrOfTransactions();
        }

        return nrOfClients;
    }

    public void setStepHandler(CurrentStepHandler stepHandler) {
        this.stepHandler = stepHandler;
    }

    public void setProbabilityHandler(ProbabilityContainerHandler probabilityHandler) {
        this.probabilityHandler = probabilityHandler;
    }
}