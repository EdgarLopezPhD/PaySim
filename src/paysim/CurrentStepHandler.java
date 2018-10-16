package paysim;

import java.util.ArrayList;

import paysim.aggregation.AggregateTransactionRecord;
import paysim.parameters.TransactionParameters;

public class CurrentStepHandler {
    String UNKNOWN_STEP = "";
    int STEP_NB = 744;
    private static int HOUR_IN_DAY = 24;
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
    private ArrayList<CurrentStepContainer> stepHandler = new ArrayList<>();

    public CurrentStepHandler(ArrayList<String> aggregateParameters, double mult) {
        setAggregateParameters(aggregateParameters);
        initAggregateRecordList();
        initStepHandler(STEP_NB);
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
                    UNKNOWN_STEP);
            aggrRecordList.add(record);
        }
    }

    private void initStepHandler(int stepNb) {
        for (int i = 0; i < stepNb; i++) {
            CurrentStepContainer container = new CurrentStepContainer(i);
            stepHandler.add(container);
        }
    }

    private void initStepCount() {
        for (CurrentStepContainer stepContainer : stepHandler) {
            int step = stepContainer.getCurrentStep();
            int day = step / HOUR_IN_DAY + 1;
            int hour = step % HOUR_IN_DAY;

            if (isInAggrRecordList(day, hour)) {
                int count = 0;
                for (String csvLine : aggregateParameters) {
                    String line[] = csvLine.split(",");
                    try {
                        if (TransactionParameters.isValidType(line[COLUMN_TYPE]) &&
                                Integer.parseInt(line[COLUMN_DAY]) == day &&
                                Integer.parseInt(line[COLUMN_HOUR]) == hour) {
                            count += Integer.parseInt(line[COLUMN_COUNT]);
                        }
                    } catch (Exception e) {
                        System.out.println("initStepCount - Could not parse line " + csvLine);
                        e.printStackTrace();
                        continue;
                    }
                }
                stepContainer.setCounter(count);
            }
        }
    }

    private void removeEmptySteps() {
        stepHandler.removeIf(step -> step.getCounter() == 0);
    }

    private void modifyWithMultiplier(double mult) {
        for (CurrentStepContainer stepContainer : stepHandler) {
            int newCounter = stepContainer.getCounter();
            newCounter = Math.toIntExact(Math.round(newCounter * mult));
            stepContainer.setCounter(newCounter);
        }
    }

    private boolean isInAggrRecordList(int day, int hour) {
        for (AggregateTransactionRecord t : aggrRecordList) {
            if (t.gettDay().equals(String.valueOf(day)) &&
                    t.gettHour().equals(String.valueOf(hour))) {
                return true;
            }
        }
        return false;
    }

    public AggregateTransactionRecord getRecord(String type, int day, int hour) {
        for (AggregateTransactionRecord t : aggrRecordList) {
            if (t.getType().equals(type) &&
                    t.gettDay().equals(String.valueOf(day)) &&
                    t.gettHour().equals(String.valueOf(hour))) {
                return t;
            }
        }
        return null;
    }


    public int getNrOfTimesToReduce(int stepNumber) {
        for (CurrentStepContainer stepContainer : stepHandler) {
            if (stepContainer.getCurrentStep() == stepNumber) {
                return stepContainer.getNrReduced();
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
            if (index == 0) {
                if (isFull(currentStep)) {
                    return null;
                }
            }
            CurrentStepContainer gottenOne = stepHandler.get(index);
            if (gottenOne.canBeReduced()
                    && gottenOne.getCurrentStep() >= currentStep) {
                stepHandler.get(index).increment();
                stepsToBeRepeated.add(gottenOne.getCurrentStep());
                stepsGathered++;
            }
            index++;
        }
        return stepsToBeRepeated;

    }

    private boolean isFull(int currentStep) {
        for (CurrentStepContainer stepContainer : stepHandler) {
            if (stepContainer.getCurrentStep() >= currentStep &&
                    stepContainer.canBeReduced()) {
                return false;
            }
        }
        return true;
    }
}