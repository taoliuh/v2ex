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
package com.sonaive.v2ex.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sonaive.v2ex.io.model.Feed;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.api.Api;
import com.sonaive.v2ex.sync.api.FeedsApi;
import com.sonaive.v2ex.util.ModelUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGW;
import static com.sonaive.v2ex.util.LogUtils.LOGE;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/13/14.
 */
public class FeedsHandler extends JSONHandler {

    private static final String TAG = makeLogTag(FeedsHandler.class);

    // The api type.
    private int apiType;

    private HashMap<String, Feed> mFeeds = new HashMap<>();

    public FeedsHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.Feeds.CONTENT_URI;
        HashMap<String, String> feedHashcodes = loadFeedsHashcodes();
        HashSet<String> feedsToKeep = new HashSet<>();
        boolean isIncrementalUpdate = feedHashcodes != null && feedHashcodes.size() > 0;

        if (isIncrementalUpdate) {
            LOGD(TAG, "Doing incremental update for feeds.");
        } else {
            LOGD(TAG, "Doing FULL (non incremental) update for feeds.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedFeeds = 0;
        for (Feed feed : mFeeds.values()) {
            String hashCode = feed.getImportHashcode();
            feedsToKeep.add(String.valueOf(feed.id));

            // add feed, if necessary
            if (!isIncrementalUpdate || !feedHashcodes.containsKey(String.valueOf(feed.id)) ||
                    !feedHashcodes.get(String.valueOf(feed.id)).equals(hashCode)) {
                ++updatedFeeds;
                boolean isNew = !isIncrementalUpdate || !feedHashcodes.containsKey(String.valueOf(feed.id));
                buildFeed(isNew, feed, list);
            }
        }

        LOGD(TAG, "Feeds: " + (isIncrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedFeeds + " to update, New total: " + mFeeds.size());
    }

    @Override
    public void process(JsonElement element) {
        for (Feed feed : new Gson().fromJson(element, Feed[].class)) {
            mFeeds.put(String.valueOf(feed.id), feed);
        }
    }

    @Override
    public String getBody(Bundle data) {
        if (data != null) {
            apiType = data.getInt(FeedsApi.ARG_TYPE);
            return data.getString(Api.ARG_RESULT);
        }
        return "";
    }

    private void buildFeed(boolean isInsert, Feed feed,
                            ArrayList<ContentProviderOperation> list) {
        Uri allVideosUri = V2exContract.addCallerIsSyncAdapterParameter(
                V2exContract.Feeds.CONTENT_URI);
        Uri thisVideoUri = V2exContract.addCallerIsSyncAdapterParameter(
                V2exContract.Feeds.buildFeedUri(String.valueOf(feed.id)));

        ContentProviderOperation.Builder builder;
        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allVideosUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisVideoUri);
        }

        if (TextUtils.isEmpty(String.valueOf(feed.id))) {
            LOGW(TAG, "Ignoring feed with missing feed ID.");
            return;
        }

        list.add(builder.withValue(V2exContract.Feeds.FEED_ID, feed.id)
                .withValue(V2exContract.Feeds.FEED_TITLE, feed.title)
                .withValue(V2exContract.Feeds.FEED_URL, feed.url)
                .withValue(V2exContract.Feeds.FEED_CONTENT, feed.content)
                .withValue(V2exContract.Feeds.FEED_CONTENT_RENDERED, feed.content_rendered)
                .withValue(V2exContract.Feeds.FEED_REPLIES, feed.replies)
                .withValue(V2exContract.Feeds.FEED_MEMBER, ModelUtils.serializeMember(feed.member))
                .withValue(V2exContract.Feeds.FEED_NODE, ModelUtils.serializeNode(feed.node))
                .withValue(V2exContract.Feeds.FEED_CREATED, feed.created)
                .withValue(V2exContract.Feeds.FEED_LAST_MODIFIED, feed.last_modified)
                .withValue(V2exContract.Feeds.FEED_LAST_TOUCHED, feed.last_touched)
                .withValue(V2exContract.Feeds.FEED_TYPE, apiType)
                .withValue(V2exContract.Feeds.FEED_IMPORT_HASHCODE, feed.getImportHashcode())
                .build());
    }

    private void buildDeleteOperation(String feedId, ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.addCallerIsSyncAdapterParameter(
                V2exContract.Feeds.buildFeedUri(feedId));
        list.add(ContentProviderOperation.newDelete(uri).build());
    }

    private HashMap<String, String> loadFeedsHashcodes() {
        Uri uri = V2exContract.Feeds.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, FeedHashcodeQuery.PROJECTION,
                null, null, null);
        if (cursor == null) {
            LOGE(TAG, "Error querying feed hashcodes (got null cursor)");
            return null;
        }
        if (cursor.getCount() < 1) {
            LOGE(TAG, "Error querying feed hashcodes (no records returned)");
            cursor.close();
            return null;
        }
        HashMap<String, String> result = new HashMap<>();
        while (cursor.moveToNext()) {
            String feedId = cursor.getString(FeedHashcodeQuery.FEED_ID);
            String hashcode = cursor.getString(FeedHashcodeQuery.FEED_IMPORT_HASHCODE);
            result.put(feedId, hashcode == null ? "" : hashcode);
        }
        cursor.close();
        return result;
    }

    private interface FeedHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                V2exContract.Feeds.FEED_ID,
                V2exContract.Feeds.FEED_IMPORT_HASHCODE
        };
        final int _ID = 0;
        final int FEED_ID = 1;
        final int FEED_IMPORT_HASHCODE = 2;
    }
}
