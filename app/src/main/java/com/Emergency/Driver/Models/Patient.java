package com.Emergency.Driver.Models;

/**
 * Created by kamlesh on 11-02-2018.
 */

public class Patient {
    private String id;
    private Double latitude;
    private Double longitude;

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Patient() {
        super();
    }

    public Patient(String id){
        this.id=id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


