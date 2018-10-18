package paysim;

import java.util.ArrayList;
import java.util.Map;

import paysim.actors.Client;
import paysim.base.ActionProbability;
import paysim.base.Repetition;
import paysim.base.Transaction;
import paysim.parameters.BalanceClients;
import paysim.parameters.Parameters;
import paysim.parameters.StepParameters;
import paysim.parameters.TransactionParameters;
import paysim.utils.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import static java.lang.Math.abs;

public class Manager implements Steppable {
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
        int nbClients = getNbClients(actionProbabilities);

        nbClients *= Parameters.multiplier;

        double probArr[] = paysim.loadProbabilities(actionProbabilities, nbClients);

        //TODO: Verify implementation of this check
        //If there are no clients to repeat, "-1" is returned, hence, if its -1, nbClients should remain 0 because there are originally
        //no transactions to be executed at that step.
        int remainingAlignments = StepParameters.getRemainingAssignments(step);
        if (remainingAlignments != -1) {
            nbClients -= remainingAlignments;
        }
        for (int i = 0; i < nbClients; i++) {
            Client c = this.generateClient(probArr, actionProbabilities, paysim, step);
            if (c.getStepsToRepeat().size() != 0) {
                paysim.getClients().add(c);
            }
            paysim.schedule.scheduleOnce(c);
        }

        updatePaySimOutputs(paysim, step);
    }

    private void updatePaySimOutputs(PaySim paysim, int step) {
        ArrayList<Transaction> transactions = paysim.getTransactions();
        if (transactions.size() > 0) {
            Manager.nbStepParticipated++;
        }

        Output.writeLog(Parameters.filenameLog, transactions);
        if (Parameters.saveToDB) {
            Output.writeDatabaseLog(Parameters.dbUrl, Parameters.dbUser, Parameters.dbPassword, transactions);
        }

        Output.writeAggregateStep(Parameters.filenameOutputAggregate, step, paysim.getTransactions());
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
            int nbTimesToRepeat;

            //Get how many times to repeat
            if ((cont.getLow() - cont.getHigh()) == 0) {
                nbTimesToRepeat = (int) cont.getLow();
            } else {
                int randNr = abs(paysim.random.nextInt() % ((int) (cont.getHigh() - cont.getLow())));
                nbTimesToRepeat = (int) (cont.getLow() + randNr);
                //Check if the randomized nr of times to be repeated exceeds the max
                int maxTimesAction = TransactionParameters.getMaxOccurrenceGivenAction(cont.getAction());
                if (nbTimesToRepeat > maxTimesAction) {
                    nbTimesToRepeat = maxTimesAction;
                }
            }
            nbTimesToRepeat *= Parameters.multiplier;
            ArrayList<Integer> stepsToRepeat = StepParameters.getSteps(step, nbTimesToRepeat);
            if (stepsToRepeat == null) {
                return generatedClient;
            }
            generatedClient.setStepsToRepeat(stepsToRepeat);
            generatedClient.setCont(cont);
            return generatedClient;
        }
    }

    private int getNbClients(Map<String, ActionProbability> actionProbabilities) {
        int nbClients = 0;

        for (ActionProbability p : actionProbabilities.values()) {
            nbClients += p.getNbTransactions();
        }

        return nbClients;
    }
}