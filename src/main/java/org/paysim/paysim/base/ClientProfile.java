package org.paysim.paysim.base;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import ec.util.MersenneTwisterFast;

import org.paysim.paysim.parameters.ActionTypes;

public class ClientProfile {
    private Map<String, ClientActionProfile> profile;
    private Map<String, Double> actionProbability = new HashMap<>();
    private final Map<String, Integer> targetCount = new HashMap<>();
    private int clientTargetCount;

    public ClientProfile(Map<String, ClientActionProfile> profile, MersenneTwisterFast random) {
        this.profile = profile;
        this.clientTargetCount = 0;
        for (String action : ActionTypes.getActions()) {
            int targetCountAction = pickTargetCount(action, random);
            targetCount.put(action, targetCountAction);
            clientTargetCount += targetCountAction;
        }
        computeActionProbability();
    }

    private int pickTargetCount(String action, MersenneTwisterFast random) {
        ClientActionProfile actionProfile = profile.get(action);
        int targetCountAction;

        int rangeSize = actionProfile.getMaxCount() - actionProfile.getMinCount();

        if (rangeSize == 0) {
            targetCountAction = actionProfile.getMinCount();
        } else {
            targetCountAction = actionProfile.getMinCount() + random.nextInt(rangeSize);
        }

        //TODO: check if this is really mandatory
        int maxCountAction = ActionTypes.getMaxOccurrenceGivenAction(actionProfile.getAction());
        if (targetCountAction > maxCountAction) {
            targetCountAction = maxCountAction;
        }

        return targetCountAction;
    }

    private void computeActionProbability() {
        actionProbability = targetCount.entrySet()
                .stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        c -> ((double) c.getValue()) / clientTargetCount)
                );
    }

    public Map<String, Double> getActionProbability() {
        return actionProbability;
    }

    public int getClientTargetCount() {
        return clientTargetCount;
    }

    public ClientActionProfile getProfilePerAction(String action) {
        return profile.get(action);
    }
}
