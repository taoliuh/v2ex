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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGW;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/13/14.
 */
public class FeedsApi extends Api {
    private static final String TAG = makeLogTag(UserIdentityApi.class);

    public static final int TYPE_LATEST = 0;
    public static final int TYPE_HOT = 1;
    public static final String ARG_TYPE = "arg_type";

    protected final Context mContext;
    protected final String mUrl;

    public FeedsApi(Context context, int type) {
        mContext = context;
        if (type == TYPE_LATEST) {
            mUrl = Api.API_URLS.get(Api.API_LATEST);
        } else {
            mUrl = Api.API_URLS.get(Api.API_HOT);
        }
    }

    @Override
    public String sync() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(mUrl)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response != null && response.code() == 200) {
                LOGD(TAG, "Server returned HTTP_OK, so fetching feeds was successful.");
                return response.body().string();
            } else if (response != null && response.code() == 403) {
                LOGW(TAG, "Server returned 403, fetching feeds was failed.");
            } else {
                LOGW(TAG, "Fetching feeds was failed. Unknown reason");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
