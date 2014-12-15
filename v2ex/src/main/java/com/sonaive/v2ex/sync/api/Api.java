/*
 * Copyright 2014 sonaive.com. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sonaive.v2ex.sync.api;

import android.content.Context;
import android.os.Bundle;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGW;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/6/14.
 */
public abstract class Api {

    private static final String TAG = makeLogTag(Api.class);

    public static final String ARG_TYPE = "arg_type";
    public static final String ARG_RESULT = "arg_result";
    public static final String ARG_API_PARAMS_ID = "arg_params_id";
    public static final String ARG_API_NAME = "arg_api_name";

    public static final String API_HOST_URL = "http://www.v2ex.com";
    public static final String API_TOPICS_LATEST = "/api/topics/latest.json";
    public static final String API_TOPICS_HOT = "/api/topics/hot.json";
    public static final String API_TOPICS_SPECIFIC = "/api/topics/show.json";
    public static final String API_NODES_ALL = "/api/nodes/all.json";
    public static final String API_NODES_SPECIFIC = "/api/nodes/show.json";
    public static final String API_REVIEWS = "/api/replies/show.json";
    public static final String API_MEMBER = "/members/show.json";
    public static final String API_SIGNIN = "/signin";
    public static final String API_MY_NODES = "/my/nodes";
    public static final String API_USER_IDENTITY = "/api/members/show.json";

    public static Map<String, String> API_URLS = new HashMap<>();

    static {
        API_URLS.put(API_TOPICS_LATEST, API_HOST_URL + API_TOPICS_LATEST);
        API_URLS.put(API_TOPICS_HOT, API_HOST_URL + API_TOPICS_HOT);
        API_URLS.put(API_TOPICS_SPECIFIC, API_HOST_URL + API_TOPICS_SPECIFIC);
        API_URLS.put(API_NODES_ALL, API_HOST_URL + API_NODES_ALL);
        API_URLS.put(API_NODES_SPECIFIC, API_HOST_URL + API_NODES_SPECIFIC);
        API_URLS.put(API_REVIEWS, API_HOST_URL + API_REVIEWS);
        API_URLS.put(API_MEMBER, API_HOST_URL + API_MEMBER);
        API_URLS.put(API_SIGNIN, API_HOST_URL + API_SIGNIN);
        API_URLS.put(API_MY_NODES, API_HOST_URL + API_MY_NODES);
        API_URLS.put(API_USER_IDENTITY, API_HOST_URL + API_USER_IDENTITY);
    }

    protected Context mContext;
    protected String mUrl;
    protected Bundle mArguments;

    public Bundle sync() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(mUrl)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response != null && response.code() == 200) {
                LOGD(TAG, "Request: " + mUrl + ", server returned HTTP_OK, so fetching was successful.");
                mArguments.putString(Api.ARG_RESULT, response.body().string());
                return mArguments;
            } else if (response != null && response.code() == 403) {
                LOGW(TAG, "Request: " + mUrl + ", Server returned 403, fetching was failed.");
            } else {
                LOGW(TAG, "Request: " + mUrl + ", Fetching was failed. Unknown reason");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
