package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class RepetitionHandler {
    private ArrayList<String> freqParams;
    private ArrayList<Double> probActions;
    private Random r;

    public RepetitionHandler(long seed, PaySim p) {
        freqParams = new ArrayList<>();
        probActions = new ArrayList<>();
        init(p);
        r = new Random(seed);
    }

    private void init(PaySim paysim) {
        loadFreqParams(paysim);
    }

    private void loadFreqParams(PaySim paysim) {
        // Init probability of actions
        File f1 = new File(paysim.transferFreqModInit);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f1));
            String line = "";
            while ((line = reader.readLine()) != null) {
                probActions.add(Double.parseDouble(line.split(",")[1]));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File f = new File(paysim.transferFreqMod);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = "";
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                freqParams.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getAction() {
        int actionGenerated = -1;
        while (actionGenerated == -1)
            actionGenerated = randomEvent(probActions);

        return actionGenerated;
    }

    public RepetitionContainer getRepetition() {
        RepetitionContainer contToReturn = new RepetitionContainer();

        //Firstly, get the action to be potentially repeated, CASH_IN/CASH_OUT ...etc.
        int action = getAction();

        //Get the name of the action
        String actionType = freqParams.get(action).split(",")[0]; // INVALID !!!

        //Now, given that action, and based on the probabilities for each rep combination for that action, randomize how many
        //times to repeat that action
        ArrayList<String> listAction = new ArrayList<>();
        for (int i = 0; i < freqParams.size(); i++) {
            if (freqParams.get(i).split(",")[0].equals(actionType)) {
                listAction.add(freqParams.get(i));
            }
        }

        //Now that all of the records for that action type are separated, make a new probability array and randomize an index from that
        ArrayList<Double> probArr = new ArrayList<>();
        for (String subEvent : listAction) {
            probArr.add(Double.parseDouble(subEvent.split(",")[5]));
        }

        //And now, get the index based on that probabilities
        int index = randomEvent(probArr);

        //From that index, parse out a repetition container record
        String record = listAction.get(index);
        String splitRecord[] = record.split(",");

        contToReturn.setType(splitRecord[0]);
        contToReturn.setLow(Double.parseDouble(splitRecord[1]));
        contToReturn.setHigh(Double.parseDouble(splitRecord[2]));
        contToReturn.setAvg(Double.parseDouble(splitRecord[3]));
        contToReturn.setStd(Double.parseDouble(splitRecord[4]));

        return contToReturn;
    }

    private int randomEvent(ArrayList<Double> probArr) {
        double randNr = r.nextDouble();
        double lowThresh = 0;

        for (int i = 0; i < probArr.size(); i++) {
            double probEvent = probArr.get(i);

            if (lowThresh <= randNr && randNr <= lowThresh + probEvent)
                return i;
            else
                lowThresh += probEvent;
        }
        return -1;
    }
}