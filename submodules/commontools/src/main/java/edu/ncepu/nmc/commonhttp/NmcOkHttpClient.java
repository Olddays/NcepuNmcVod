package edu.ncepu.nmc.commonhttp;

import android.content.Context;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.Callback;

import edu.ncepu.nmc.commonutils.LibConfigs;

/**
 * Created by liu on 8/27/15.
 */

public class NmcOkHttpClient {
    private static final boolean DEBUG = LibConfigs.DEBUG_LOG;
    private static final String TAG = "NmcOkHttpClient";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient mClient;
    private static NmcOkHttpClient mIntance;
    private Context mCxt;

    private NmcOkHttpClient(Context cxt) {
        this.mCxt = cxt.getApplicationContext();
        mClient = new OkHttpClient();
        mClient.setConnectTimeout(15, TimeUnit.SECONDS);
    }

    public static NmcOkHttpClient getInstance(Context cxt) {
        if (mIntance == null) {
            synchronized (NmcOkHttpClient.class) {
                if (mIntance == null) {
                    mIntance = new NmcOkHttpClient(cxt);
                }
            }
        }
        return mIntance;
    }

    /**
     * 不会开启异步线程
     */
    public Response execute(Request request) throws IOException {
        return mClient.newCall(request).execute();
    }

    /**
     * 开启异步线程
     */
    public void enqueue(Request request, Callback responseCallback) {
        if (responseCallback != null) {
            mClient.newCall(request).enqueue(responseCallback);
        } else {
            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Response arg0) throws IOException {
                }

                @Override
                public void onFailure(Request arg0, IOException arg1) {
                }
            });
        }
    }

    public String commonGet(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    public void commonPost(String url, String json, Callback responseCallback) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();
        enqueue(request, responseCallback);
    }

    public void commonPostKValue(String url, Map<String, String> params, Callback responseCallback) throws IOException {
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params != null && params.size() > 0) {
            for (Entry<String, String> param : params.entrySet()) {
                builder.add(param.getKey(), param.getValue());
            }
        }
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        enqueue(request, responseCallback);
    }

}
