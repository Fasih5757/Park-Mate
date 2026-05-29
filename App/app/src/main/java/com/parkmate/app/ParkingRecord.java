package com.parkmate.app;

public class ParkingRecord {
    public String name;
    public double lat;
    public double lng;
    public String date;
    public String time;
    public boolean includeInHistory;
    public boolean remindMe;

    public ParkingRecord(String name, double lat, double lng, String date, String time,
                         boolean includeInHistory, boolean remindMe) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
        this.time = time;
        this.includeInHistory = includeInHistory;
        this.remindMe = remindMe;
    }
}
