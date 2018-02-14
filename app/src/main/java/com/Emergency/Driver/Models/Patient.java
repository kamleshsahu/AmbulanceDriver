package com.Emergency.Driver.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by kamlesh on 11-02-2018.
 */

public class Patient {
    private String id;
    private Double latitude;
    private Double longitude;

    private  Object timestamp=ServerValue.TIMESTAMP;;


    Object getTimestamp() {
        return timestamp;
    }

    @Exclude
    public long timestamp() {
        return (long) timestamp;
    }

    public Patient() {
        super();
    }

    public Patient(String id){
        this.id=id;
        timestamp = ServerValue.TIMESTAMP;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}


