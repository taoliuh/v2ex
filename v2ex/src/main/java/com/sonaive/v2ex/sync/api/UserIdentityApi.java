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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sonaive.v2ex.R;
import com.sonaive.v2ex.sync.V2exDataHandler;
import com.sonaive.v2ex.util.AccountUtils;
import com.sonaive.v2ex.util.LoginHelper;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/13/14.
 */
public class UserIdentityApi {
    private static final String TAG = makeLogTag(UserIdentityApi.class);

    private final Context mContext;
    private final String mUrl;

    public UserIdentityApi(Context context) {
        mContext = context;
        mUrl = Api.API_URLS.get(Api.API_USER_IDENTITY);
    }

    public void verifyUserIdentity(final String accountName) {
        verifyUserIdentity(accountName, null);
    }

    public void verifyUserIdentity(final String accountName, final WeakReference<LoginHelper.Callbacks> callbacksRef) {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(mUrl + "?username=" + accountName)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                JsonObject result = new JsonObject();
                result.addProperty("result", "fail");
                result.addProperty("err_msg", "IOException");
                if (callbacksRef != null) {
                    callbacksRef.get().onIdentityCheckedFailed(result);
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
                            AccountUtils.setActiveAccount(mContext, accountName);
                            V2exDataHandler dataHandler = new V2exDataHandler(mContext);

                            Bundle data = new Bundle();
                            data.putString(Api.ARG_RESULT, responseBody);
                            data.putString(V2exDataHandler.ARG_DATA_KEY, V2exDataHandler.DATA_KEY_MEMBERS);
                            dataHandler.applyData(new Bundle[] {data});

                            if (callbacksRef != null) {
                                callbacksRef.get().onIdentityCheckedSuccess(result);
                            }
                        } else if (status != null && status.equals("notfound")) {
                            result.addProperty("result", "fail");
                            result.addProperty("err_msg", mContext.getString(R.string.err_user_not_found));
                            if (callbacksRef != null) {
                                callbacksRef.get().onIdentityCheckedFailed(result);
                            }
                        } else {
                            result.addProperty("result", "fail");
                            result.addProperty("err_msg", mContext.getString(R.string.err_unknown_error));
                            if (callbacksRef != null) {
                                callbacksRef.get().onIdentityCheckedFailed(result);
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        result.addProperty("result", "fail");
                        result.addProperty("err_msg", "JsonSyntaxException");
                        if (callbacksRef != null) {
                            callbacksRef.get().onIdentityCheckedFailed(result);
                        }
                    } catch (UnsupportedOperationException e) {
                        result.addProperty("result", "fail");
                        result.addProperty("err_msg", "UnsupportedOperationException");
                        if (callbacksRef != null) {
                            callbacksRef.get().onIdentityCheckedFailed(result);
                        }
                    }
                } else if (response.code() == 403) {
                    result.addProperty("result", "fail");
                    result.addProperty("err_msg", "403 Forbidden");
                    if (callbacksRef != null) {
                        callbacksRef.get().onIdentityCheckedFailed(result);
                    }
                }
                LOGD(TAG, "responseCode: " + response.code() + ", result: " + result.get("result") + ", err_msg: " + result.get("err_msg"));
            }
        });
    }
}
