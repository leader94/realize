package com.ps.realize.core.datamodels;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.ps.realize.core.daos.Converters;
import com.ps.realize.core.datamodels.json.ProjectObj;

import java.util.ArrayList;

@Entity
public class User {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String mobile;
    private String countryCode;
    private String profilePhoto;
    private String token;

    @Ignore
    private onUserObjectUpdate _updateListener;

    @TypeConverters({Converters.class})
    private ArrayList<ProjectObj> projects;

    public User() {
    }

    public User(User obj) {
        this(obj.id, obj.name, obj.firstName, obj.lastName,
                obj.mobile, obj.countryCode, obj.profilePhoto,
                obj.token, obj._updateListener, obj.projects);
    }

    public User(String id, String name, String firstName, String lastName,
                String mobile, String countryCode, String profilePhoto,
                String token, onUserObjectUpdate _updateListener, ArrayList<ProjectObj> projects) {
        this.id = id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ArrayList<ProjectObj> getProjects() {
        return this.projects;
    }

    public void setProjects(ArrayList<ProjectObj> projectObj) {
        this.projects = projectObj;
    }

    public interface onUserObjectUpdate {
        void onUpdate(User obj);
    }
}
