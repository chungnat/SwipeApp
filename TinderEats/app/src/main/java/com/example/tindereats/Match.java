package com.example.tindereats;

import java.util.List;

public class Match {

    private List<String> invitedFriends;
    private String location;
    private String category;
    private String price;
    private String UUID;
    private String senderDisplayName;
    private String senderUid;
    private Double latitude;
    private Double longitude;
    private boolean useUserLoc;

    public Match(){}

    public Match(List<String> invitedFriends, String location, String category, String price, String UUID, String senderDisplayName, String senderUid, Double latitude, Double longitude, Boolean useUserLoc) {
        this.invitedFriends = invitedFriends;
        this.location = location;
        this.category = category;
        this.price = price;
        this.UUID = UUID;
        this.senderDisplayName = senderDisplayName;
        this.senderUid = senderUid;
        this.useUserLoc = useUserLoc;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public List<String> getInvitedFriends() {
        return invitedFriends;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getUUID() {
        return UUID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public boolean isUseUserLoc() {
        return useUserLoc;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getSenderUid() {
        return senderUid;
    }
}
