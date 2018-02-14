package com.Emergency.Driver.Models;

/**
 * Created by kamlesh on 11-02-2018.
 */

public class Ambulance {

    private Locate location;
    private String TargetPatient;


    public Ambulance() {

    }

    public Locate getLocate() {
        return location;
    }

    public void setLocate(Locate location) {
        this.location = location;
    }

    public void setTargetPatient(String targetPatient) {
        TargetPatient = targetPatient;
    }

    public String getTargetPatient() {
        return TargetPatient;
    }
}
