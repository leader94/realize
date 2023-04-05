package com.ps.realize.utils;

import org.json.JSONObject;

import okhttp3.Response;

public class JSONUtils {
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
