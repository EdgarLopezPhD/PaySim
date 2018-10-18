package paysim;

import java.util.ArrayList;
import java.util.Map;

import paysim.actors.Client;
import paysim.aggregation.AggregateTransactionRecord;
import paysim.base.ActionProbability;
import paysim.base.Repetition;
import paysim.parameters.BalanceClients;
import paysim.parameters.Parameters;
import paysim.parameters.StepParameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import static java.lang.Math.abs;

public class Manager implements Steppable {
    //For debugging purposes
    static int nbStepParticipated = 0;

    public void step(SimState state) {
        /*
         * Algorithm
         *
         * 1) Get the probabilities to load from the current step
         * 2) From that, get the number of clients to allocate at the next step
         * 3) For each client that is created, make sure there is a 10% chance of that client to re-enter after x-amount of steps
         */
        PaySim paysim = (PaySim) state;

        int step = (int) paysim.schedule.getSteps();

        //Get the corresponding probabilities for that step from the parameter file
        Map<String, ActionProbability> actionProbabilities = StepParameters.get(step);

        //Get the number of clients to load from actionProbabilities
        int nrOfClients = getNrOfClients(actionProbabilities);

        nrOfClients *= Parameters.multiplier;

        //FIX THIS
        double probArr[] = paysim.loadProbabilities(actionProbabilities, nrOfClients);

        //If there are no clients to repeat, "-1" is returned, hence, if its -1, nrOfClients should remain 0 because there are originally
        //no transactions to be executed at that step.
        int remainingAlignments = StepParameters.getRemainingAssignments(step);
        if (remainingAlignments != -1) {
            nrOfClients -= remainingAlignments;
        }
        for (int i = 0; i < nrOfClients; i++) {
            Client c = this.generateClient(probArr, actionProbabilities, paysim, step);
            if (c.getStepsToRepeat().size() != 0) {
                paysim.getClients().add(c);
            }
            paysim.schedule.scheduleOnce(c);
        }

        updatePaySimOutputs(paysim);
    }

    private void updatePaySimOutputs(PaySim paysim) {
        ArrayList<AggregateTransactionRecord> records = paysim.getAggregateCreator().
                generateAggregateParamFile(paysim.getTrans());

        if (records.size() > 0) {
            Manager.nbStepParticipated++;
        }
        for (AggregateTransactionRecord r : records) {
            paysim.getAggrTransRecordList().add(r);
        }

        Output.writeLog(paysim.logFileName, paysim.trans);
        if (Parameters.saveToDB) {
            Output.writeDatabaseLog(Parameters.dbUrl, Parameters.dbUser, Parameters.dbPassword, paysim.trans);
        }
        paysim.resetVariables();
    }

    private Client generateClient(double probArr[], Map<String, ActionProbability> actionProbabilities, PaySim paysim, int step) {
        //Create the client
        Client generatedClient = new Client(paysim.generateIdentifier());
        generatedClient.setProbabilityArr(probArr);
        generatedClient.setActionProbabilities(actionProbabilities);
        generatedClient.setBalance(BalanceClients.getBalance(paysim));
        generatedClient.setStep(step);

        Repetition cont = TransactionParameters.getRepetition();
        //Check whether the action is to be repeated
        if (cont.getLow() == 1 && cont.getHigh() == 1) {
            return generatedClient;
        } else {
            int nrOfTimesToRepeat;

            //Get how many times to repeat
            if ((cont.getLow() - cont.getHigh()) == 0) {
                nrOfTimesToRepeat = (int) cont.getLow();
            } else {
                int randNr = abs(paysim.random.nextInt() % ((int) (cont.getHigh() - cont.getLow())));
                nrOfTimesToRepeat = (int) (cont.getLow() + randNr);
                //Check if the randomized nr of times to be repeated exceeds the max
                int maxTimesAction = TransactionParameters.getMaxOccurrenceGivenAction(cont.getAction());
                if (nrOfTimesToRepeat > maxTimesAction) {
                    nrOfTimesToRepeat = maxTimesAction;
                }
            }
            nrOfTimesToRepeat *= Parameters.multiplier;
            ArrayList<Integer> stepsToRepeat = StepParameters.getSteps(step, nrOfTimesToRepeat);
            if (stepsToRepeat == null) {
                return generatedClient;
            }
            generatedClient.setStepsToRepeat(stepsToRepeat);
            generatedClient.setCont(cont);
            return generatedClient;
        }
    }

    private int getNrOfClients(Map<String, ActionProbability> actionProbabilities) {
        int nrOfClients = 0;

        for (ActionProbability p : actionProbabilities.values()) {
            nrOfClients += p.getNbTransactions();
        }

        return nrOfClients;
    }
}