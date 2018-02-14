package com.Emergency.Driver.Models;

/**
 * Created by kamlesh on 11-02-2018.
 */

public class Locate {
    private Double latitude;
    private Double longitude;


    public Locate() {
        super();
    }

    public Locate(Double latitude, Double longitude) {
       this.latitude=latitude;
       this.longitude=longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
