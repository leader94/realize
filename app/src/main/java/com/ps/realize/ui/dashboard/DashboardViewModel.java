package com.ps.realize.ui.dashboard;

import android.widget.LinearLayout;

import androidx.lifecycle.ViewModel;

import com.ps.realize.core.components.HorizontalSuggestionLane;
import com.ps.realize.core.data.LocalData;

import org.json.JSONArray;
import org.json.JSONObject;

public class DashboardViewModel extends ViewModel {

    public String getProfilePhoto() {
        return LocalData.curUser.getProfilePhoto();
    }

    public String getSuggestionLaneIV(String id) {
        switch (id) {
            case "id1_i1":
                return "https://media.tenor.com/8kX_6u-O4QIAAAAj/party.gif";
            case "id1_i2":
                return "https://media.tenor.com/eSc2KWdhZPMAAAAj/parrot-party.gif";
            case "id2_i1":
                return "https://media.tenor.com/5mY0_OI1MSUAAAAj/peach-cat.gif";
            case "id2_i2":
                return "https://media.tenor.com/g7m_dcsETm4AAAAj/%E3%83%91%E3%83%BC%E3%83%86%E3%82%A3-%E6%A5%BD%E3%81%97%E3%81%84.gif";
            default:
                return "https://media.tenor.com/4eIVp2bI-34AAAAj/angkukuehgirl-angkukuehgirlandfriends.gif";
        }
    }



    public void updateUI(JSONObject configJSON, LinearLayout linearLayout){
        try{
            JSONObject data = configJSON.getJSONObject("data");
            JSONArray suggestions = data.getJSONArray("suggestions");
            for (int i=0;i<suggestions.length();i++){
                JSONObject curObj =  suggestions.getJSONObject(i);
                HorizontalSuggestionLane horizontalSuggestionLane = new HorizontalSuggestionLane();
                horizontalSuggestionLane.setLaneData(curObj);
                horizontalSuggestionLane.addHorizontalSuggestionLane(linearLayout);
            }

        }catch ( Exception e){
            e.printStackTrace();
        }


    }
}