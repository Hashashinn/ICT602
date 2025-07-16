package com.example.projectict;

public class BusLocation {
//    DATABASE PURPOSE
    public double latitude;
    public double longitude;
    public long timestamp;

    public BusLocation() {
        // Default constructor for database
    }

    public BusLocation(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
