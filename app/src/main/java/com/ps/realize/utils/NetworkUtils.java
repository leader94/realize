package com.ps.realize.utils;

import com.ps.realize.core.interfaces.NetworkListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkUtils {

     static final String BASEURL = "https://apimocha.com/testingapprealise";
    static OkHttpClient client = new OkHttpClient();

     public static void get(String url, NetworkListener listener) throws IOException {
        Request request = new Request.Builder().url(BASEURL+url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                call.cancel();
                listener.onFailure(request,e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

//                    System.out.println(responseBody.string());  // .string() cannot be called multiple times
                    listener.onResponse(response);
                }
            }
        });
    }


}
