package com.ps.realize.utils;

import androidx.annotation.NonNull;

import com.ps.realize.core.interfaces.NetworkListener;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class NetworkUtils {

    //    static final String BASEURL = "https://apimocha.com/testingapprealise";
    static final String BASEURL = "http://192.168.1.46:3000";
    static OkHttpClient client = new OkHttpClient();

    private static String getIp() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            return ip.getHostAddress();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void _makeNewCall(Request request, NetworkListener listener) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                call.cancel();
                listener.onFailure(request, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

//                    System.out.println(responseBody.string());  // .string() cannot be called multiple times
                    listener.onResponse(response);
                }
            }
        });
    }

    public static void get(String url, NetworkListener listener) {
        getWithToken(url, null, listener);
    }

    public static void getWithToken(String url, String token, NetworkListener listener) {
//        Request request = new Request.Builder()
//                .url(BASEURL + url)
//                .build();
//        if (token != null) {
//            new Request.Builder()
//                    .url(BASEURL + url)
//                    .addHeader("Authorization", "Bearer " + token)
//                    .build();
//        }
        Request.Builder reqBuilder = new Request
                .Builder()
                .url(isValidUrl(url) ? url : BASEURL + url);

        if (token != null) {
            reqBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = reqBuilder.build();
        _makeNewCall(request, listener);
        /*client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                call.cancel();
                listener.onFailure(request, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

//                    System.out.println(responseBody.string());  // .string() cannot be called multiple times
                    listener.onResponse(response);
                }
            }
        });*/
    }

    public static void post(String url, String json, NetworkListener listener) {
        postWithToken(url, json, null, listener);
    }

    public static void postWithToken(String url, String json, String token, NetworkListener listener) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request.Builder reqBuilder = new Request
                .Builder()
                .url(isValidUrl(url) ? url : BASEURL + url)
                .post(body);

        if (token != null) {
            reqBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = reqBuilder.build();
        _makeNewCall(request, listener);

    }

    public static void postWithMultipart(
            String url, JSONObject formFields, InputStream fileInputStream, String fileName,
            String mimeType, NetworkListener listener, ProgressRequestBody.Listener progListener) {
        postWithMultipartAndToken(url, formFields, fileInputStream, fileName, mimeType, null, listener, progListener);
    }

    public static void postWithMultipartAndToken
            (String url, JSONObject formFields, InputStream fileInputStream, String fileName,
             String mimeType, String token, NetworkListener listener, ProgressRequestBody.Listener progListener) {

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        Iterator<String> keys = formFields.keys();

        try {
            while (keys.hasNext()) {
                String key = keys.next();
                if (formFields.get(key) instanceof String) {
                    String value = formFields.getString(key);
                    multipartBuilder.addFormDataPart(key, value);
                }
            }

//            multipartBuilder.addFormDataPart("file", file.getName(),
//                    RequestBody.create(file, MediaType.parse("image/jpeg")));

            RequestBody fileReqBody = createFileReqBody(MediaType.parse(mimeType), fileInputStream);

            if (progListener != null) {
                ProgressRequestBody reqBodyWithProgressData = new ProgressRequestBody(fileReqBody, progListener);
                multipartBuilder.addFormDataPart("file", fileName, reqBodyWithProgressData);
            } else {
                multipartBuilder.addFormDataPart("file", fileName, fileReqBody);
            }


            MultipartBody requestBody = multipartBuilder.build();
            Request.Builder reqBuilder = new Request
                    .Builder()
                    .url(isValidUrl(url) ? url : BASEURL + url)
                    .post(requestBody);

            if (token != null) {
                reqBuilder.addHeader("Authorization", "Bearer " + token);
            }

            Request request = reqBuilder.build();
            _makeNewCall(request, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidUrl(String url) {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    public static RequestBody createFileReqBody(final MediaType mediaType, final InputStream inputStream) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}
