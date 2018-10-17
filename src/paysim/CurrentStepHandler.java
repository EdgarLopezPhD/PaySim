package paysim;

import java.util.ArrayList;

import paysim.aggregation.AggregateTransactionRecord;
import paysim.parameters.Parameters;
import paysim.parameters.TransactionParameters;

public class CurrentStepHandler {
    private static int COLUMN_TYPE = 0,
            COLUMN_MONTH = 1,
            COLUMN_DAY = 2,
            COLUMN_HOUR = 3,
            COLUMN_COUNT = 4,
            COLUMN_SUM = 5,
            COLUMN_AVG = 6,
            COLUMN_STD = 7,
            COLUMN_STEP = 8;
    private ArrayList<String> aggregateParameters = new ArrayList<>();
    private ArrayList<AggregateTransactionRecord> aggrRecordList = new ArrayList<>();
    private ArrayList<StepCounter> stepHandler = new ArrayList<>();

    public CurrentStepHandler(ArrayList<String> aggregateParameters, double mult) {
        setAggregateParameters(aggregateParameters);
        initAggregateRecordList();
        initStepHandler(Parameters.nbSteps);
        initStepCount();
        removeEmptySteps();
        modifyWithMultiplier(mult);
    }

    private void setAggregateParameters(ArrayList<String> aggregateParameters) {
        // Remove CSV Header
        if (!aggregateParameters.isEmpty())
            aggregateParameters.remove(0);
        this.aggregateParameters = aggregateParameters;
    }

    private void initAggregateRecordList() {
        for (String s : aggregateParameters) {
            String split[] = s.split(",");
            AggregateTransactionRecord record = new AggregateTransactionRecord(split[COLUMN_TYPE],
                    split[COLUMN_MONTH],
                    split[COLUMN_DAY],
                    split[COLUMN_HOUR],
                    split[COLUMN_COUNT],
                    split[COLUMN_SUM],
                    split[COLUMN_AVG],
                    split[COLUMN_STD],
                    split[COLUMN_STEP]);
            aggrRecordList.add(record);
        }
    }

    private void initStepHandler(int stepNb) {
        for (int i = 0; i < stepNb; i++) {
            StepCounter container = new StepCounter(i);
            stepHandler.add(container);
        }
    }

    private void initStepCount() {
        for (StepCounter stepContainer : stepHandler) {
            int step = stepContainer.getCurrentStep();

            if (isInAggrRecordList(step)) {
                int count = 0;
                for (String csvLine : aggregateParameters) {
                    String line[] = csvLine.split(",");
                    try {
                        if (TransactionParameters.isValidType(line[COLUMN_TYPE]) &&
                                Integer.parseInt(line[COLUMN_STEP]) == step) {
                            count += Integer.parseInt(line[COLUMN_COUNT]);
                        }
                    } catch (Exception e) {
                        System.out.println("initStepCount - Could not parse line " + csvLine);
                        e.printStackTrace();
                        continue;
                    }
                }
                stepContainer.setMaxCount(count);
            }
        }
    }

    private void removeEmptySteps() {
        stepHandler.removeIf(step -> step.getMaxCount() == 0);
    }

    private void modifyWithMultiplier(double mult) {
        for (StepCounter stepContainer : stepHandler) {
            int newMaxCount = stepContainer.getMaxCount();
            newMaxCount = Math.toIntExact(Math.round(newMaxCount * mult));
            stepContainer.setMaxCount(newMaxCount);
        }
    }

    private boolean isInAggrRecordList(int step) {
        for (AggregateTransactionRecord t : aggrRecordList) {
            if (t.gettStep().equals(Integer.toString(step))) {
                return true;
            }
        }
        return false;
    }

    public AggregateTransactionRecord getRecord(String type, int step) {
        for (AggregateTransactionRecord t : aggrRecordList) {
            if (t.getType().equals(type) && t.gettStep().equals(Integer.toString(step))) {
                return t;
            }
        }
        return null;
    }


    public int getRemainingAssignements(int stepNumber) {
        for (StepCounter stepContainer : stepHandler) {
            if (stepContainer.getCurrentStep() == stepNumber) {
                //int remains = stepContainer.getMaxCount() - stepContainer.getCountAssigned();
                return stepContainer.getCountAssigned();
            }
        }
        return -1;
    }


    public ArrayList<Integer> getSteps(int currentStep, int nrOfSteps) {
        ArrayList<Integer> stepsToBeRepeated = new ArrayList<>();
        int stepsGathered = 0;
        int index = 0;
        while (stepsGathered < nrOfSteps) {
            index = index % stepHandler.size();
            if (index == 0 && isFull(currentStep)) {
                    return null;
            }
            StepCounter step = stepHandler.get(index);
            if (step.canBeAssigned() && step.getCurrentStep() >= currentStep) {
                stepHandler.get(index).increment();
                stepsToBeRepeated.add(step.getCurrentStep());
                stepsGathered++;
            }
            index++;
        }
        return stepsToBeRepeated;

    }

    private boolean isFull(int currentStep) {
        for (StepCounter stepContainer : stepHandler) {
            if (stepContainer.getCurrentStep() >= currentStep &&
                    stepContainer.canBeAssigned()) {
                return false;
            }
        }
        return true;
    }
}