package com.ps.realize.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import okhttp3.Response;

public class JSONUtils {

    private static Gson gson;

    public static Gson getGsonParser() {
        if (null == gson) {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        }
        return gson;
    }

    public static JSONObject getJSONObject(Response response) {
        try {
            String jsonData = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
