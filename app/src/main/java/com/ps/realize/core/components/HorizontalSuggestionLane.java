package com.ps.realize.core.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.utils.LayoutUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HorizontalSuggestionLane {

    private String title;
    private ArrayList<CardsDataType> cardsData = new ArrayList();

    class CardsDataType {
        private  String url;
        CardsDataType(String url){
            this.url = url;
        }
    }

    public void setLaneData(JSONObject suggestionData) throws Exception{

        this.title= suggestionData.getString("title");

        JSONArray laneData = suggestionData.getJSONArray("data");

        for(int j = 0 ;j<laneData.length();j++){
            JSONObject cardData = laneData.getJSONObject(j);
            String gifUrl =  cardData.getString("url");
            CardsDataType  cardsDataType = new CardsDataType(gifUrl);
            this.cardsData.add(cardsDataType);
        }

    }
   public void addHorizontalSuggestionLane(LinearLayout parentLinearLayout){
        LayoutInflater inflater = (LayoutInflater) MyApp.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.horizontal_suggestion_lane, null);
        TextView titleTV = view.findViewById(R.id.hor_sug_lan_title);
        titleTV.setText(this.title);

        LinearLayout cardLaneLL =  view.findViewById(R.id.hor_sug_lan_card_lane);

        for(int i = 0;i<this.cardsData.size();i++){
            CardsDataType eachCardData = cardsData.get(i);
            this.addSuggestionBox(cardLaneLL,eachCardData.url);
        }
        parentLinearLayout.addView(view);
    }

    public void  addSuggestionBox(LinearLayout parentLinearLayout, String url){
        ImageView view = new ImageView(MyApp.getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LayoutUtils.dpToPx(135),LayoutUtils.dpToPx(135)       );
        params.setMargins(0, 0, LayoutUtils.dpToPx(35),0);

        view.setLayoutParams(params);
        view.setBackgroundResource(R.drawable.card);
        Glide.
                with(MyApp.getContext())
                .load(url)
                .into(view);

        parentLinearLayout.addView(view);
    }
}
