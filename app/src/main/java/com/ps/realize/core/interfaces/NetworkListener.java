package com.ps.realize.core.interfaces;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public interface NetworkListener {
    void onFailure(Request request, IOException e);

    void onResponse(Response response);
}
