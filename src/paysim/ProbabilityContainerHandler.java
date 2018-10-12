package paysim;

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
        ArrayList<String> actionTypes = new ArrayList<>();
        ArrayList<ActionProbability> aProbListTemp = new ArrayList<>();
        actionTypes.add("CASH_IN");
        actionTypes.add("CASH_OUT");
        actionTypes.add("DEBIT");
        actionTypes.add("PAYMENT");
        actionTypes.add("TRANSFER");
        for (int i = 1; i <= 744; i++) {
            for (int j = 0; j < actionTypes.size(); j++) {
                ActionProbability probTemp = getActionProbabilityFromStep(actionTypes.get(j), i, fileContents);
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
            String splitted[] = s.split(",");
            if (String.valueOf(step).equals(splitted[8]) &&
                    splitted[0].equals(type)) {
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