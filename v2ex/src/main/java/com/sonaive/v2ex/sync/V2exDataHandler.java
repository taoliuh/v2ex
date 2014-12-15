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
package com.sonaive.v2ex.sync;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sonaive.v2ex.io.FeedsHandler;
import com.sonaive.v2ex.io.JSONHandler;
import com.sonaive.v2ex.io.MembersHandler;
import com.sonaive.v2ex.io.NodesHandler;
import com.sonaive.v2ex.io.ReviewsHandler;
import com.sonaive.v2ex.provider.V2exContract;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGE;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/10/14.
 */
public class V2exDataHandler {
    private static final String TAG = makeLogTag(V2exDataHandler.class);

    public static final String ARG_DATA_KEY = "arg_data_key";

    public static final String DATA_KEY_MEMBERS = "members";
    public static final String DATA_KEY_FEEDS = "feeds";
    public static final String DATA_KEY_NODES = "nodes";
    public static final String DATA_KEY_REVIEWS = "reviews";

    private static final String[] DATA_KEYS_IN_ORDER = {
            DATA_KEY_MEMBERS,
            DATA_KEY_FEEDS,
            DATA_KEY_NODES,
            DATA_KEY_REVIEWS
    };

    Context mContext = null;

    // Handlers for each entity type:
    MembersHandler mMembersHandler = null;
    FeedsHandler mFeedsHandler = null;
    NodesHandler mNodesHandler = null;
    ReviewsHandler mReviewsHandler = null;

    // Convenience map that maps the key name to its corresponding handler (e.g.
    // "blocks" to mBlocksHandler (to avoid very tedious if-elses)
    HashMap<String, JSONHandler> mHandlerForKey = new HashMap<>();

    // Tally of total content provider operations we carried out (for statistical purposes)
    private int mContentProviderOperationsDone = 0;

    public V2exDataHandler(Context ctx) {
        mContext = ctx;
    }

    /**
     * Parses the data in the given objects and imports the data into the
     * content provider.
     *
     * @param dataBodies The collection of JSON objects to parse and import.
     * @throws java.io.IOException If there is a problem parsing the data.
     */
    public void applyData(Bundle[] dataBodies) throws IOException {
        LOGD(TAG, "Applying data from " + dataBodies.length + " files");

        // create handlers for each data type
        mHandlerForKey.put(DATA_KEY_MEMBERS, mMembersHandler = new MembersHandler(mContext));
        mHandlerForKey.put(DATA_KEY_FEEDS, mFeedsHandler = new FeedsHandler(mContext));
        mHandlerForKey.put(DATA_KEY_NODES, mNodesHandler = new NodesHandler(mContext));
        mHandlerForKey.put(DATA_KEY_REVIEWS, mReviewsHandler = new ReviewsHandler(mContext));

        // process the jsons. This will call each of the handlers when appropriate to deal
        // with the objects we see in the data.
        LOGD(TAG, "Processing " + dataBodies.length + " JSON objects.");
        for (int i = 0; i < dataBodies.length; i++) {
            LOGD(TAG, "Processing json object #" + (i + 1) + " of " + dataBodies.length);
            String key = dataBodies[i].getString(V2exDataHandler.ARG_DATA_KEY);
            processDataBody(dataBodies[i], key);
        }

        // produce the necessary content provider operations
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for (String dataKey : DATA_KEYS_IN_ORDER) {
            LOGD(TAG, "Building content provider operations for: " + dataKey);
            mHandlerForKey.get(dataKey).makeContentProviderOperations(batch);
            LOGD(TAG, "Content provider operations so far: " + batch.size());
        }
        LOGD(TAG, "Total content provider operations: " + batch.size());

        // finally, push the changes into the Content Provider
        LOGD(TAG, "Applying " + batch.size() + " content provider operations.");
        try {
            int operations = batch.size();
            if (operations > 0) {
                mContext.getContentResolver().applyBatch(V2exContract.CONTENT_AUTHORITY, batch);
            }
            LOGD(TAG, "Successfully applied " + operations + " content provider operations.");
            mContentProviderOperationsDone += operations;
        } catch (RemoteException ex) {
            LOGE(TAG, "RemoteException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        } catch (OperationApplicationException ex) {
            LOGE(TAG, "OperationApplicationException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        }

        // notify all top-level paths
        LOGD(TAG, "Notifying changes on all top-level paths on Content Resolver.");
        ContentResolver resolver = mContext.getContentResolver();
        for (String path : V2exContract.TOP_LEVEL_PATHS) {
            Uri uri = V2exContract.BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            resolver.notifyChange(uri, null);
        }

        LOGD(TAG, "Done applying data.");
    }

    public int getContentProviderOperationsDone() {
        return mContentProviderOperationsDone;
    }

    /**
     * Processes data body and calls the appropriate data type handlers
     * to process each of the objects represented therein.
     *
     * @param dataBundle The body of data to process
     * @throws IOException If there is an error parsing the data.
     */
    private void processDataBody(Bundle dataBundle, String key) throws IOException {

        if (mHandlerForKey.containsKey(key)) {
            String dataBody = mHandlerForKey.get(key).getBody(dataBundle);
            JsonReader reader = new JsonReader(new StringReader(dataBody));
            JsonParser parser = new JsonParser();
            // pass the value to the corresponding handler
            mHandlerForKey.get(key).process(parser.parse(reader));
        }
    }
}
