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

import com.sonaive.v2ex.sync.V2exDataHandler;

/**
 * Created by liutao on 12/13/14.
 */
public class FeedsApi extends Api {

    public static final int TYPE_LATEST = 0;
    public static final int TYPE_HOT = 1;
    public static final int TYPE_SPECIFIC = 2;

    public FeedsApi(Context context, int type) {
        this(context, type, null);
    }

    public FeedsApi(Context context, int type, Bundle params) {
        mContext = context;
        if (params != null) {
            mArguments = params;
        } else {
            mArguments = new Bundle();
        }
        mArguments.putString(V2exDataHandler.ARG_DATA_KEY, V2exDataHandler.DATA_KEY_FEEDS);
        if (type == TYPE_LATEST) {
            mUrl = Api.API_URLS.get(Api.API_TOPICS_LATEST);
            mArguments.putInt(Api.ARG_TYPE, TYPE_LATEST);

        } else if (type == TYPE_HOT) {
            mUrl = Api.API_URLS.get(Api.API_TOPICS_HOT);
            mArguments.putInt(Api.ARG_TYPE, TYPE_HOT);

        } else {
            String id = null;
            if (params != null) {
                id = params.getString(Api.ARG_API_PARAMS_ID, null);
            }
            mUrl = Api.API_URLS.get(Api.API_TOPICS_SPECIFIC).concat("?id=" + id);
            mArguments.putInt(Api.ARG_TYPE, TYPE_SPECIFIC);
        }
    }

//    @Override
//    public String sync() {
//        OkHttpClient okHttpClient = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url(mUrl)
//                .build();
//
//        try {
//            Response response = okHttpClient.newCall(request).execute();
//            if (response != null && response.code() == 200) {
//                LOGD(TAG, "Server returned HTTP_OK, so fetching feeds was successful.");
//                return response.body().string();
//            } else if (response != null && response.code() == 403) {
//                LOGW(TAG, "Server returned 403, fetching feeds was failed.");
//            } else {
//                LOGW(TAG, "Fetching feeds was failed. Unknown reason");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
