package paysim;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class RepetitionFreqHandler {
    ArrayList<RepetitionFreqContainer> freqContList;

    public RepetitionFreqHandler() {
        freqContList = new ArrayList<>();
    }

    public void add(RepetitionContainer cont) {
        //1) First, check that this type of repetitionContainer exists
        //2) If it does, add the frequencies
        //3) If not, create a new frequencyContainer, and add the freq and add it to the list
        if (freqContList.contains(cont)) {
            for (RepetitionFreqContainer cToCheck : freqContList) {
                if (cToCheck.getCont().equals(cont)) {
                    cToCheck.incrementFrequency();
                }
            }
        } else {
            RepetitionFreqContainer contNew = new RepetitionFreqContainer();
            contNew.setCont(cont);
            contNew.incrementFrequency();
            freqContList.add(contNew);
        }
    }

    private double getTotalFreq() {
        return freqContList.stream()
                .mapToDouble(RepetitionFreqContainer::getFreq)
                .sum();
    }

    public String getFrequencyList() {
        String toReturn = "";
        double tot = getTotalFreq();
        DecimalFormat format = new DecimalFormat("#.######");

        Collections.sort(freqContList);
        for (RepetitionFreqContainer f : freqContList) {
            toReturn += f.toString() + "," + format.format((f.getFreq()) / tot) + "\n";
        }
        return toReturn;
    }
}