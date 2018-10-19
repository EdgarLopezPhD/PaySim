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
    public void step(SimState state) {
        PaySim paysim = (PaySim) state;
        int step = (int) paysim.schedule.getSteps();

        //Get the corresponding probabilities for that step from the parameter file
        Map<String, ActionProbability> actionProbabilities = StepParameters.get(step);

        //Get the number of clients to load from actionProbabilities
        int nbClients = getNbClients(actionProbabilities);

        nbClients *= Parameters.multiplier;

        double normalizedProbabilities[] = normalizeProbabilities(actionProbabilities, nbClients);

        int alreadyAssigned = paysim.getCountAssigned(step);
        nbClients -= alreadyAssigned;

        for (int i = 0; i < nbClients; i++) {
            Client c = generateClient(normalizedProbabilities, actionProbabilities, paysim, step);
            if (c.getStepsToRepeat().size() != 0) {
                paysim.getClients().add(c);
            }
            paysim.schedule.scheduleOnce(c);
        }

        writeOutputStep(paysim, step);
    }

    private void writeOutputStep(PaySim paysim, int step) {
        ArrayList<Transaction> transactions = paysim.getTransactions();

        Output.writeLog(Parameters.filenameLog, transactions);
        if (Parameters.saveToDB) {
            Output.writeDatabaseLog(Parameters.dbUrl, Parameters.dbUser, Parameters.dbPassword, transactions, paysim.simulatorName);
        }

        Output.writeAggregateStep(Parameters.filenameOutputAggregate, step, paysim.getTransactions());
        paysim.resetVariables();
    }

    private Client generateClient(double normalizedProbabilities[], Map<String, ActionProbability> actionProbabilities, PaySim paysim, int step) {
        //Create the client
        Client generatedClient = new Client(paysim.generateIdentifier());
        generatedClient.setNormalizedProbabilities(normalizedProbabilities);
        generatedClient.setActionProbabilities(actionProbabilities);
        generatedClient.setBalance(BalanceClients.getBalance(paysim));
        generatedClient.setStep(step);

        Repetition cont = paysim.getRepetition();
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
            ArrayList<Integer> stepsToRepeat = paysim.getSteps(step, nbTimesToRepeat);
            if (stepsToRepeat == null) {
                return generatedClient;
            }
            generatedClient.setStepsToRepeat(stepsToRepeat);
            generatedClient.setCont(cont);
            return generatedClient;
        }
    }

    private int getNbClients(Map<String, ActionProbability> actionProbabilities) {
        return actionProbabilities.values()
                .stream()
                .mapToInt(ActionProbability::getNbTransactions)
                .sum();
    }

    private double[] normalizeProbabilities(Map<String, ActionProbability> actionProbabilities, int nbClients) {
        double coef = Parameters.multiplier / ((double) nbClients);
        return actionProbabilities.values().stream()
                .map(ActionProbability::getNbTransactions)
                .mapToDouble(x -> x * coef)
                .toArray();
    }

}