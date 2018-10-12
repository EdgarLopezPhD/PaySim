package paysim;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Manager implements Steppable {

    private int nrOfClientRepeat = 0;

    //For debugging purposes
    public static int trueNrOfClients = 0;
    public static int trueNrOfRepetitions = 0;
    public static int nrFailed = 0;
    public static int nrOfDaysParticipated = 0;

    ProbabilityContainerHandler probabilityHandler = new ProbabilityContainerHandler();

    int currDay = 0;
    int currHour = 0;
    int nrOfStepsTotal = 0;
    RepetitionHandler repHandler;
    private TransferMaxHandler transferMaxHandler;
    private InitBalanceHandler balanceHandler;
    private CurrentStepHandler stepHandler;
    RepetitionFreqHandler repFreqHandler = new RepetitionFreqHandler();


    public void setTransferMaxHandler(TransferMaxHandler handler) {
        this.transferMaxHandler = handler;
    }

    public void step(SimState state) {
        /*
         * Algorithm
         *
         * 1) Get the probabilities to load from the current step
         * 2) From that, get the number of clients to allocate at the next step
         * 3) For each client that is created, make sure there is a 10% chance of that client to re-enter after x-amount of steps
         */
        PaySim paysim = (PaySim) state;
        this.repHandler = new RepetitionHandler(paysim.seed, paysim);

        //Get the current step number in the simulation
        int currStep = (int) paysim.schedule.getSteps() + 1;
        int nrOfTimesToReduce = stepHandler.getNrOfTimesToReduce(currStep);

        //Get the corresponding probabilities for that step from the parameter file
        ArrayList<ActionProbability> aProbList = getActionProbabilityFromStep(currStep, paysim);

        //Get the number of clients to load from aProbList
        int nrOfClients = getNrOfClients(aProbList);
        trueNrOfClients += nrOfClients;


        //Multiply it by the "multiplier"
        nrOfClients *= paysim.getMultiplier();

        //Subtract from the number of clients that are to repeat themselves
        if (paysim.debugFlag) {
            System.out.println("Step:\t" + currStep + "\t" + "NrOfClients Before:\t" + nrOfClients + "\n");
        }

        //FIX THIS
        double probArr[] = paysim.loadProbabilites(aProbList, nrOfClients);


        //If there are no clients to repeat, "-1" is returned, hence, if its -1, nrOfClients should remain 0 because there are originally
        //no transactions to be executed at that step.
        if (nrOfTimesToReduce != -1) {
            nrOfClients -= nrOfTimesToReduce;
        }
        Manager.trueNrOfRepetitions += nrOfTimesToReduce;

        if (paysim.debugFlag) {
//			System.out.println("Nr Of Times Repeated\t" + nrOfTimesToReduce + "\t" + "NrOfClients Updated:\t" + nrOfClients + "\n");
//			System.out.println("Day:\t" + this.currDay + "\tHour:\t" + (this.currHour) + "\n");
//			System.out.println("ProbArr:\n");
//			for(Double d: probArr){
//				System.out.println(d + "\n");
//			}
//			for(ActionProbability prob: aProbList){
//				System.out.println(prob.toString() + "\n");
//			}
        }

        if (nrOfClients < 0) {
            this.nrOfClientRepeat = nrOfClients * -1;
        } else {
            this.nrOfClientRepeat = 0;
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

//		for(Transaction t: paysim.getTrans()){
//			if(t.getDay() == 4 && t.getHour() == 16){
//				System.out.println(paysim.schedule.getSteps() + "\t" + t.toString() + "\nGOT ILLEGAL STEP\n\n");
//			}
//		}

        if (paysim.debugFlag) {
            System.out.println("Updating\n");
        }

        ArrayList<AggregateTransactionRecord> records = paysim.getAggregateCreator().
                generateAggregateParamFile(paysim.getTrans());

        if (records.size() > 0) {
            Manager.nrOfDaysParticipated++;
        }
        for (int i = 0; i < records.size(); i++) {
            paysim.getAggrTransRecordList().add(records.get(i));
        }

        //Update logs and database
        paysim.writeLog();
        if (paysim.saveToDbFlag) {
            //paysim.writeDatabaseLog();
        }
        paysim.resetVariables();

    }

    private Client generateClient(double probArr[], ArrayList<ActionProbability> aProbList, PaySim paysim, int currStep) {
        double max = 0;
        //Create the client
        Client generatedClient = new Client();
        generatedClient.setStepHandler(this.stepHandler);
        generatedClient.setParamFile(paysim.getParamFileList());
        generatedClient.setProbabilityArr(probArr);
        generatedClient.setProbList(aProbList);
        generatedClient.setName(String.valueOf(String.valueOf(System.currentTimeMillis()).hashCode()));
        generatedClient.setBalance(this.balanceHandler.getBalance());
        generatedClient.setCurrDay(this.currDay);
        generatedClient.setCurrHour(this.currHour);
        generatedClient.setTransferMaxHandler(this.transferMaxHandler);


        RepetitionContainer cont = this.repHandler.getRepetition();
        this.repFreqHandler.add(cont);
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
                max = this.transferMaxHandler.getMaxGivenType(cont.getType());
                if (nrOfTimesToRepeat > max) {
                    nrOfTimesToRepeat = (int) max;
                }
                //System.out.println("High:\t" + cont.getHigh() + "\tLow:\t" + cont.getLow() + "\tRandomizedInBetween:\t" + nrOfTimesToRepeat + "\n");

            }
            nrOfTimesToRepeat *= paysim.getMultiplier();
            ArrayList<Integer> stepsToRepeat = this.stepHandler.getSteps(currStep, nrOfTimesToRepeat);
            if (stepsToRepeat == null) {
                return generatedClient;
            }
            this.nrOfClientRepeat += nrOfTimesToRepeat;
            generatedClient.setStepsToRepeat(stepsToRepeat);
            generatedClient.setCont(cont);
            return generatedClient;
        }


    }

    public void setNrOfStepsTotal(int nrOfStepsTotal) {
        this.nrOfStepsTotal = nrOfStepsTotal;
    }

    public boolean doesRepeat(PaySim paysim) {
        int randNr = paysim.random.nextInt() % 10;
        while (randNr <= 0) {
            randNr = paysim.random.nextInt() % 10;
        }

        //Indicates 10% chance
        if (randNr == 1) {
            return true;
        }

        return false;
    }

    public ArrayList<ActionProbability> getActionProbabilityFromStep(int step, PaySim paysim) {
        ArrayList<ActionProbability> probList = new ArrayList<ActionProbability>();
        int day = (int) (step / 24) + 1;
        int hour = (int) (step - ((day - 1) * 24));
        this.currDay = day;
        this.currHour = hour;

        //FIX THIS		CHANGE THIS TO GET DIRECTLY FROM THE CACHED CONTAINER
        ProbabilityRecordContainer cont = this.probabilityHandler.getList().get(((int) step - 1));
        probList = cont.getProbList();

        if (paysim.debugFlag) {
            System.out.println("Was searching for step:\t" + step + " And got from origAggrTransList:\n");
            for (ActionProbability temp : probList) {
                System.out.println(temp.toString() + "\n\n");
            }

        }

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

    public void setRepFreqHandler(RepetitionFreqHandler repFreqHandler) {
        this.repFreqHandler = repFreqHandler;
    }

}