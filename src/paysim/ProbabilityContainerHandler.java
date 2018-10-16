package paysim;

import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;

import java.util.ArrayList;

public class ProbabilityContainerHandler {
    ArrayList<ProbabilityRecordContainer> list;

    public ProbabilityContainerHandler() {
        list = new ArrayList<>();
    }

    public ArrayList<ProbabilityRecordContainer> getList() {
        return list;
    }

    public void initRecordList(ArrayList<String> fileContents) {
        ArrayList<ActionProbability> aProbListTemp = new ArrayList<>();

        for (int i = 1; i <= Parameters.nbSteps; i++) {
            for (String actionType : TransactionParameters.getActions()) {
                ActionProbability probTemp = getActionProbabilityFromStep(actionType, i, fileContents);
                aProbListTemp.add(probTemp);
            }
            ProbabilityRecordContainer record = new ProbabilityRecordContainer(i, aProbListTemp);
            list.add(record);
            aProbListTemp = new ArrayList<>();
        }

    }

    private ActionProbability getActionProbabilityFromStep(String type, int step, ArrayList<String> paramFile) {
        ActionProbability probToReturn = new ActionProbability();
        for (String s : paramFile) {
            String split[] = s.split(",");
            if (String.valueOf(step).equals(split[8]) &&
                    split[0].equals(type)) {
                probToReturn = getActionProb(s);
            }
        }
        return probToReturn;
    }

    private ActionProbability getActionProb(String line) {
        String tokens[] = line.split(",");
        ActionProbability prob = new ActionProbability();

        prob.setType(tokens[0]);
        prob.setMonth(Double.parseDouble(tokens[1]));
        prob.setDay(Double.parseDouble(tokens[2]));
        prob.setHour(Double.parseDouble(tokens[3]));
        prob.setNrOfTransactions(Double.parseDouble(tokens[4]));
        prob.setTotalSum(Double.parseDouble(tokens[5]));
        prob.setAverage(Double.parseDouble(tokens[6]));
        prob.setStd(Double.parseDouble(tokens[7]));

        return prob;
    }
}