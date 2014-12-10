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

package com.sonaive.v2ex.util;

import android.app.Activity;
import android.content.Context;
import android.content.OperationApplicationException;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sonaive.v2ex.Api;
import com.sonaive.v2ex.R;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.V2exDataHandler;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import org.apache.http.Header;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGW;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;
/**
 * This helper handles the UI flow for signing in an account. It handles
 * connecting to the V2EX API to fetch profile data (name, cover photo, etc).
 * The life of this object is tied to an Activity. Do not attempt to share
 * it across Activities, as unhappiness will result.
 *
 * Created by liutao on 12/6/14.
 */
public class LoginHelper {
    private static final String TAG = makeLogTag(LoginHelper.class);

    Context mAppContext;

    // The Activity this object is bound to (we use a weak ref to avoid context leaks)
    WeakReference<Activity> mActivityRef;

    // Callbacks interface we invoke to notify the user of this class of useful events
    WeakReference<Callbacks> mCallbacksRef;

    // Name of the account to log in as
    String mAccountName;

    // account password
    String mPassword;

    // Are we in the started state? Started state is between onStart and onStop.
    boolean mStarted = false;

    private OkHttpClient okHttpClient;

    public interface Callbacks {
        void onIdentityCheckedSuccess(JsonObject result);
        void onIdentityCheckedFailed(JsonObject result);
        void onNodeCollectionFetchedSuccess(JsonObject result);
        void onNodeCollectionFetchedFailed(JsonObject result);
    }

    public LoginHelper(Activity activity, Callbacks callbacks, String accountName, String password) {
        LOGD(TAG, "Helper created. Account: " + mAccountName);
        mActivityRef = new WeakReference<>(activity);
        mCallbacksRef = new WeakReference<>(callbacks);
        mAppContext = activity.getApplicationContext();
        mAccountName = accountName;
        mPassword = password;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public String getAccountName() {
        return mAccountName;
    }

    private Activity getActivity(String methodName) {
        Activity activity = mActivityRef.get();
        if (activity == null) {
            LOGD(TAG, "Helper lost Activity reference, ignoring (" + methodName + ")");
        }
        return activity;
    }

    /** Starts the helper. Call this from your Activity's onStart(). */
    public void start() {
        Activity activity = getActivity("start()");
        if (activity == null) {
            return;
        }

        if (mStarted) {
            LOGW(TAG, "Helper already started. Ignoring redundant call.");
            return;
        }

        mStarted = true;
        LOGD(TAG, "Helper starting. Connecting " + mAccountName);

        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
        }

        verifyUserIdentity();
    }

    /** Stop the helper. Call this from your Activity's onStop(). */
    public void stop() {
        if (!mStarted) {
            LOGW(TAG, "Helper already stopped. Ignoring redundant call.");
            return;
        }

        LOGD(TAG, "Helper stopping.");
        mStarted = false;
    }

    private void verifyUserIdentity() {
        Request request = new Request.Builder()
                .url(Api.API_URLS.get(Api.API_USER_IDENTITY) + "?username=" + mAccountName)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                JsonObject result = new JsonObject();
                result.addProperty("result", "fail");
                result.addProperty("err_msg", "IOException");
                if (mCallbacksRef != null) {
                    mCallbacksRef.get().onIdentityCheckedFailed(result);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                JsonObject result = new JsonObject();
                if (response.code() == 200) {
                    String responseBody = response.body().string();
                    try {
                        JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                        String status = jsonObject.get("status").getAsString();
                        if (status != null && status.equals("found")) {
                            result.addProperty("result", "ok");
                            LOGD(TAG, "userInfo: " + responseBody);
                            V2exDataHandler dataHandler = new V2exDataHandler(mAppContext);
                            dataHandler.applyData(new String[]{responseBody}, V2exDataHandler.DATA_KEY_MEMBERS);
                            if (mCallbacksRef != null) {
                                mCallbacksRef.get().onIdentityCheckedSuccess(result);
                            }
                        } else if (status != null && status.equals("notfound")) {
                            result.addProperty("result", "fail");
                            result.addProperty("err_msg", mAppContext.getString(R.string.err_user_not_found));
                            if (mCallbacksRef != null) {
                                mCallbacksRef.get().onIdentityCheckedFailed(result);
                            }
                        } else {
                            result.addProperty("result", "fail");
                            result.addProperty("err_msg", mAppContext.getString(R.string.err_unknown_error));
                            if (mCallbacksRef != null) {
                                mCallbacksRef.get().onIdentityCheckedFailed(result);
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        result.addProperty("result", "fail");
                        result.addProperty("err_msg", "JsonSyntaxException");
                        if (mCallbacksRef != null) {
                            mCallbacksRef.get().onIdentityCheckedFailed(result);
                        }
                    } catch (UnsupportedOperationException e) {
                        result.addProperty("result", "fail");
                        result.addProperty("err_msg", "UnsupportedOperationException");
                        if (mCallbacksRef != null) {
                            mCallbacksRef.get().onIdentityCheckedFailed(result);
                        }
                    }
                } else if (response.code() == 403) {
                    result.addProperty("result", "fail");
                    result.addProperty("err_msg", "403 Forbidden");
                    if (mCallbacksRef != null) {
                        mCallbacksRef.get().onIdentityCheckedFailed(result);
                    }
                }
                LOGD(TAG, "responseCode: " + response.code() + ", result: " + result.get("result") + ", err_msg: " + result.get("err_msg"));
            }
        });
    }

    /** After spent hours digging, I give up */
    private void signIn(String onceCode) {

        Map<String, String> params = new HashMap<>();
        params.put("next", "/");
        params.put("u", mAccountName);
        params.put("p", mPassword);
        params.put("once", onceCode);

        RequestBody postBody = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), params.toString());

        Request request = new Request.Builder()
                .header("Origin", "http://www.v2ex.com")
                .header("Referer", "http://www.v2ex.com/signin")
                .header("X-Requested-With", "com.android.browser")
                .header("Cache-Control", "max-age=0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN, en-US")
                .header("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
                .url(Api.API_URLS.get(Api.API_SIGNIN))
                .post(postBody)
                .build();

        try {

            okHttpClient.setFollowRedirects(false);
            Response response = okHttpClient.newCall(request).execute();

            final JsonObject result = new JsonObject();
            Pattern errorPattern = Pattern.compile("<div class=\"problem\">(.*)</div>");
            Matcher errorMatcher = errorPattern.matcher(response.body().string());
            final String errorContent;

            if (response.code() == 302) {// temporary moved, 302 found, disallow redirects.
                LOGD(TAG, "sign in success!");
                getUserInfo();
                return;
            } else if (errorMatcher.find()) {
                errorContent = errorMatcher.group(1).replaceAll("<[^>]+>", "");
            } else {
                errorContent = "Unknown error";
            }

            if (errorContent != null) {
                result.addProperty("result", "fail");
                result.addProperty("err_msg", errorContent);
                LOGD(TAG, "sign in error, err_msg = " + errorContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Get once code for sign in */
    private void getOnceCodeAndSignin() {
        Request request = new Request.Builder()
                .url(Api.API_URLS.get(Api.API_SIGNIN))
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                final JsonObject result = new JsonObject();
                result.addProperty("result", "fail");
                Activity activity = getActivity("getOnceCodeAndSignin()");
                if (activity == null) {
                    return;
                }
                if (e != null) {
                    result.addProperty("err_msg", activity.getString(R.string.err_io_exception));
                } else {
                    result.addProperty("err_msg", activity.getString(R.string.err_get_once_failed));
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final JsonObject result = new JsonObject();

                Pattern pattern = Pattern.compile("<input type=\"hidden\" value=\"([0-9]+)\" name=\"once\" />");
                final Matcher matcher = pattern.matcher(response.body().string());
                Activity activity = getActivity("getOnceCodeAndSignin()");
                if (activity == null) {
                    return;
                }
                if (matcher.find()) {
                    String code = matcher.group(1);
                    signIn(code);
                } else {
                    result.addProperty("result", "fail");
                    result.addProperty("err_msg", activity.getString(R.string.err_get_once_failed));
                }
            }
        });
    }

    /** Get signed in account info(node collections, account name), need cookie */
    private void getUserInfo() {
        Request request = new Request.Builder()
                .url(Api.API_URLS.get(Api.API_MY_NODES))
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                JsonObject result = new JsonObject();
                result.addProperty("result", "fail");
                result.addProperty("err_msg", mAppContext.getString(R.string.err_get_node_collections_failed));
                if (mCallbacksRef != null) {
                    mCallbacksRef.get().onNodeCollectionFetchedFailed(result);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {

                JsonObject result = new JsonObject();

                if (response.code() == 200) {
                    Pattern userPattern = Pattern.compile("<a href=\"/member/([^\"]+)\" class=\"top\">");
                    Matcher userMatcher = userPattern.matcher(response.body().string());
                    if (userMatcher.find()) {

                        String accountName = userMatcher.group(1);
                        Pattern collectionPattern = Pattern.compile("</a>&nbsp; <a href=\"/go/([^\"]+)\">");
                        Matcher collectionMatcher = collectionPattern.matcher(response.body().string());
                        List<String> collections = new ArrayList<>();
                        if (collectionMatcher.find()) {
                            collections.add(collectionMatcher.group(1));
                            while (collectionMatcher.find()) {
                                collections.add(collectionMatcher.group(1));
                            }
                        }
                        // TODO add the user node collections into database
                        LOGD(TAG, "Get user: " + accountName + " node collections: " + collections.toString());
                        result.addProperty("result", "ok");
                        if (mCallbacksRef != null) {
                            mCallbacksRef.get().onNodeCollectionFetchedSuccess(result);
                        }
                    } else {
                        result.addProperty("result", "fail");
                        result.addProperty("err_msg", mAppContext.getString(R.string.err_get_my_nodes_failed));
                        if (mCallbacksRef != null) {
                            mCallbacksRef.get().onNodeCollectionFetchedFailed(result);
                        }
                    }
                } else if (response.code() == 403) {
                    result.addProperty("result", "fail");
                    result.addProperty("err_msg", "403 Forbidden");
                    if (mCallbacksRef != null) {
                        mCallbacksRef.get().onIdentityCheckedFailed(result);
                    }

                } else {
                    result.addProperty("result", "fail");
                    result.addProperty("err_msg", mAppContext.getString(R.string.err_unknown_error));
                    if (mCallbacksRef != null) {
                        mCallbacksRef.get().onIdentityCheckedFailed(result);
                    }
                }

                LOGD(TAG, "responseCode: " + response.code() + ", result: " + result.get("result") + ", err_msg: " + result.get("err_msg"));
            }
        });
    }
}
