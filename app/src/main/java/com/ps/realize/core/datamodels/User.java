package com.ps.realize.core.datamodels;

import com.ps.realize.core.datamodels.json.ProjectObj;

import java.util.ArrayList;

public class User {

    private String name;
    private String firstName;
    private String lastName;
    private String mobile;
    private String countryCode;
    private String profilePhoto;
    private String token;
    private onUserObjectUpdate _updateListener;

    private ArrayList<ProjectObj> projects;

    public User() {
    }

    public User(User obj) {
        this(obj.name, obj.firstName, obj.lastName,
                obj.mobile, obj.countryCode, obj.profilePhoto,
                obj.token, obj._updateListener, obj.projects);
    }

    public User(String name, String firstName, String lastName,
                String mobile, String countryCode, String profilePhoto,
                String token, onUserObjectUpdate _updateListener, ArrayList<ProjectObj> projects) {
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.countryCode = countryCode;
        this.profilePhoto = profilePhoto;
        this.token = token;
        this._updateListener = _updateListener;
        this.projects = projects;
    }

//    public void setUserObject(JSONObject userJSON) {
//        try {
//            this.name = userJSON.getString("name");
//            this.firstName = userJSON.getString("firstName");
//            this.lastName = userJSON.getString("lastName");
//            this.mobile = userJSON.getString("mobile");
//            this.countryCode = userJSON.getString("countryCode");
//            this.profilePhoto = userJSON.getString("profilePhoto");
//            this.projects = userJSON.getJSONArray("projects");
//            _updateListener.onUpdate(this);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ArrayList<ProjectObj> getProjects() {
        return this.projects;
    }

    public interface onUserObjectUpdate {
        void onUpdate(User obj);
    }
}
