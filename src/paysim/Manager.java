package paysim;

import java.util.ArrayList;

import paysim.actors.Client;
import paysim.aggregation.AggregateTransactionRecord;
import paysim.base.ActionProbability;
import paysim.base.Repetition;
import paysim.parameters.BalanceClients;
import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import static java.lang.Math.abs;

public class Manager implements Steppable {
    //For debugging purposes
    static int trueNrOfClients = 0;
    static int nbStepParticipated = 0;

    private ProbabilityContainerHandler probabilityHandler = new ProbabilityContainerHandler();
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
        int remainingAssignements = stepHandler.getRemainingAssignements(currStep);
        if (remainingAssignements != -1) {
            nrOfClients -= remainingAssignements;
        }
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
            Manager.nbStepParticipated++;
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
        generatedClient.setBalance(BalanceClients.getBalance(paysim));
        generatedClient.setCurrStep(currStep);

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
                int randNr = abs(paysim.random.nextInt() % ((int) (cont.getHigh() - cont.getLow())));
                nrOfTimesToRepeat = (int) (cont.getLow() + randNr);
                //Check if the randomized nr of times to be repeated exceeds the max
                int maxTimesType = TransactionParameters.getMaxOccurenceGivenType(cont.getType());
                if (nrOfTimesToRepeat > maxTimesType) {
                    nrOfTimesToRepeat = maxTimesType;
                }
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
        ProbabilityRecordContainer cont = probabilityHandler.getList().get(step - 1);
        ArrayList<ActionProbability> probList = cont.getProbList();

        return probList;
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