package com.sonaive.v2ex.sync.api;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by liutao on 12/15/14.
 */
public class NodesApi extends Api {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_SPECIFIC = 1;

    public NodesApi(Context context, int type) {
        this(context, type, null);
    }

    public NodesApi(Context context, int type, Bundle params) {
        mContext = context;
        if (type == TYPE_ALL) {
            mUrl = Api.API_URLS.get(Api.API_NODES_ALL);
        } else {
            String id = null;
            if (params != null) {
                id = params.getString(Api.ARG_API_PARAMS_ID, null);
            }
            mUrl = Api.API_URLS.get(Api.API_NODES_SPECIFIC).concat("?id=" + id);
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
