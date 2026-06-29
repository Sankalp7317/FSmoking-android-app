package com.fsmoking.app.models;

public class UserData {
    private String quitDate;
    private float cigsPerDay;
    private float packSize;
    private float packCost;
    private String currency;

    public UserData() {}

    public UserData(String quitDate, float cigsPerDay,
                    float packSize, float packCost, String currency) {
        this.quitDate = quitDate;
        this.cigsPerDay = cigsPerDay;
        this.packSize = packSize;
        this.packCost = packCost;
        this.currency = currency;
    }

    public String getQuitDate() { return quitDate; }
    public void setQuitDate(String v) { this.quitDate = v; }

    public float getCigsPerDay() { return cigsPerDay; }
    public void setCigsPerDay(float v) { this.cigsPerDay = v; }

    public float getPackSize() { return packSize; }
    public void setPackSize(float v) { this.packSize = v; }

    public float getPackCost() { return packCost; }
    public void setPackCost(float v) { this.packCost = v; }

    public String getCurrency() { return currency != null ? currency : "₹"; }
    public void setCurrency(String v) { this.currency = v; }
}