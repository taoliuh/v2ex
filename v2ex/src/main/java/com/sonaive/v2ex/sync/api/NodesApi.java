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
 * Created by liutao on 12/15/14.
 */
//public class NodesApi extends Api {
//
//    public static final int TYPE_ALL = 0;
//    public static final int TYPE_SPECIFIC = 1;
//
//    public NodesApi(Context context, int type) {
//        this(context, type, null);
//    }
//
//    public NodesApi(Context context, int type, Bundle params) {
//        mContext = context;
//        if (params != null) {
//            mArguments = params;
//        } else {
//            mArguments = new Bundle();
//        }
//        mArguments.putString(V2exDataHandler.ARG_DATA_KEY, V2exDataHandler.DATA_KEY_NODES);
//        if (type == TYPE_ALL) {
//            mUrl = Api.API_URLS.get(Api.API_NODES_ALL);
//            mArguments.putInt(Api.ARG_TYPE, TYPE_ALL);
//        } else {
//            String id = null;
//            if (params != null) {
//                id = params.getString(Api.ARG_API_PARAMS_ID, null);
//            }
//            mUrl = Api.API_URLS.get(Api.API_NODES_SPECIFIC).concat("?id=" + id);
//            mArguments.putInt(Api.ARG_TYPE, TYPE_SPECIFIC);
//        }
//    }
//}
