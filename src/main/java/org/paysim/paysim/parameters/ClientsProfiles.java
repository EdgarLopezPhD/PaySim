package org.paysim.paysim.parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ec.util.MersenneTwisterFast;

import org.paysim.paysim.base.ClientActionProfile;
import org.paysim.paysim.utils.CSVReader;
import org.paysim.paysim.utils.RandomCollection;

public class ClientsProfiles {
    private static final int COLUMN_ACTION = 0, COLUMN_LOW = 1, COLUMN_HIGH = 2, COLUMN_AVG = 3, COLUMN_STD = 4, COLUMN_FREQ = 5;
    private Map<String, RandomCollection<ClientActionProfile>> profilePickerPerAction = new HashMap<>();

    public ClientsProfiles(String filename) {
        ArrayList<String[]> parameters = CSVReader.read(filename);

        for (String action : ActionTypes.getActions()) {
            profilePickerPerAction.put(action, new RandomCollection<>());
        }

        for (String[] profileString : parameters) {
            if (ActionTypes.isValidAction(profileString[COLUMN_ACTION])) {
                RandomCollection<ClientActionProfile> profilePicker = profilePickerPerAction.get(profileString[COLUMN_ACTION]);
                ClientActionProfile clientActionProfile = new ClientActionProfile(profileString[COLUMN_ACTION],
                        Integer.parseInt(profileString[COLUMN_LOW]),
                        Integer.parseInt(profileString[COLUMN_HIGH]),
                        Double.parseDouble(profileString[COLUMN_AVG]),
                        Double.parseDouble(profileString[COLUMN_STD]));
                profilePicker.add(Double.parseDouble(profileString[COLUMN_FREQ]), clientActionProfile);
            }
        }

        for (RandomCollection profile: profilePickerPerAction.values()) {
            if (profile.isEmpty()){
                System.out.println("Warning : Missing action in " + filename);
                break;
            }
        }
    }

    public Collection<ClientActionProfile> getProfilesFromAction(String action) {
        return profilePickerPerAction.get(action).getCollection();
    }

    public ClientActionProfile pickNextActionProfile(String action) {
        return profilePickerPerAction.get(action).next();
    }

    public void setRandom(MersenneTwisterFast random){
        for (RandomCollection profilePicker : profilePickerPerAction.values()){
            profilePicker.setRandom(random);
        }
    }
}
