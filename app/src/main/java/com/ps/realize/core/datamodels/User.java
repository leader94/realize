package com.ps.realize.core.datamodels;

import org.json.JSONObject;

public class User {

    private String name;
    private String firstName;
    private String lastName;
    private String mobile;
    private String countryCode;
    private String profilePhoto;
    private onUserObjectUpdate _updateListener;

    public User() {

    }

    public void setUserObject(JSONObject userJSON) {
        try {
            this.name = userJSON.getString("name");
            this.firstName = userJSON.getString("firstName");
            this.lastName = userJSON.getString("lastName");
            this.mobile = userJSON.getString("mobile");
            this.countryCode = userJSON.getString("countryCode");
            this.profilePhoto = userJSON.getString("profilePhoto");
            _updateListener.onUpdate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public void setObjectUpdateListener(onUserObjectUpdate listener) {
        _updateListener = listener;
    }

    public interface onUserObjectUpdate {
        void onUpdate(User obj);
    }
}
