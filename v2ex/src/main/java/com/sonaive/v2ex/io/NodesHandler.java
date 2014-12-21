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
import com.sonaive.v2ex.io.model.Node;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.api.Api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGE;
import static com.sonaive.v2ex.util.LogUtils.LOGW;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;
/**
 * Created by liutao on 12/15/14.
 */
public class NodesHandler extends JSONHandler {

    private static final String TAG = makeLogTag(NodesHandler.class);

    // The api type.
    private int apiType;

    private HashMap<String, Node> mNodes = new HashMap<>();


    public NodesHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.Nodes.CONTENT_URI;
        HashMap<String, String> nodeHashcodes = loadNodeHashcodes();
        HashSet<String> nodesToKeep = new HashSet<>();
        boolean isIncrementalUpdate = nodeHashcodes != null && nodeHashcodes.size() > 0;

        if (isIncrementalUpdate) {
            LOGD(TAG, "Doing incremental update for nodes.");
        } else {
            LOGD(TAG, "Doing FULL (non incremental) update for nodes.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedNodes = 0;
        for (Node node : mNodes.values()) {
            String hashCode = node.getImportHashcode();
            nodesToKeep.add(String.valueOf(node.id));

            // add node, if necessary
            if (!isIncrementalUpdate || !nodeHashcodes.containsKey(String.valueOf(node.id)) ||
                    !nodeHashcodes.get(String.valueOf(node.id)).equals(hashCode)) {
                ++updatedNodes;
                boolean isNew = !isIncrementalUpdate || !nodeHashcodes.containsKey(String.valueOf(node.id));
                buildNode(isNew, node, list);
            }
        }

        LOGD(TAG, "Nodes: " + (isIncrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedNodes + " to update, New total: " + mNodes.size());
    }

    @Override
    public void process(JsonElement element) {
        if (element.isJsonArray()) {
            for (Node node : new Gson().fromJson(element, Node[].class)) {
                mNodes.put(String.valueOf(node.id), node);
            }
        } else {
            Node node = new Gson().fromJson(element, Node.class);
            mNodes.put(String.valueOf(node.id), node);
        }

    }

    @Override
    public String getBody(Bundle data) {
        if (data != null) {
            apiType = data.getInt(Api.ARG_TYPE);
            return data.getString(Api.ARG_RESULT);
        }
        return "";
    }

    private void buildNode(boolean isInsert, Node node,
                           ArrayList<ContentProviderOperation> list) {
        Uri allVideosUri = V2exContract.addCallerIsSyncAdapterParameter(
                V2exContract.Nodes.CONTENT_URI);
        Uri thisVideoUri = V2exContract.addCallerIsSyncAdapterParameter(
                V2exContract.Nodes.buildNodeUri(String.valueOf(node.id)));

        ContentProviderOperation.Builder builder;
        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allVideosUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisVideoUri);
        }

        if (TextUtils.isEmpty(String.valueOf(node.id))) {
            LOGW(TAG, "Ignoring feed with missing feed ID.");
            return;
        }

        list.add(builder.withValue(V2exContract.Nodes.NODE_ID, node.id)
                .withValue(V2exContract.Nodes.NODE_NAME, node.name)
                .withValue(V2exContract.Nodes.NODE_URL, node.url)
                .withValue(V2exContract.Nodes.NODE_TITLE, node.title)
                .withValue(V2exContract.Nodes.NODE_TITLE_ALTERNATIVE, node.title_alternative)
                .withValue(V2exContract.Nodes.NODE_TOPICS, node.topics)
                .withValue(V2exContract.Nodes.NODE_HEADER, node.header)
                .withValue(V2exContract.Nodes.NODE_FOOTER, node.footer)
                .withValue(V2exContract.Nodes.NODE_CREATED, node.created)
                .withValue(V2exContract.Nodes.NODE_AVATAR_MINI, node.avatar_mini)
                .withValue(V2exContract.Nodes.NODE_AVATAR_NORMAL, node.avatar_normal)
                .withValue(V2exContract.Nodes.NODE_AVATAR_LARGE, node.avatar_large)
                .withValue(V2exContract.Nodes.NODE_IMPORT_HASHCODE, node.getImportHashcode())
                .build());
    }

    private void buildDeleteOperation(String nodeId, ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.addCallerIsSyncAdapterParameter(
                V2exContract.Nodes.buildNodeUri(nodeId));
        list.add(ContentProviderOperation.newDelete(uri).build());
    }

    private HashMap<String, String> loadNodeHashcodes() {
        Uri uri = V2exContract.Nodes.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, NodeHashcodeQuery.PROJECTION,
                null, null, null);
        if (cursor == null) {
            LOGE(TAG, "Error querying node hashcodes (got null cursor)");
            return null;
        }
        if (cursor.getCount() < 1) {
            LOGE(TAG, "Error querying node hashcodes (no records returned)");
            cursor.close();
            return null;
        }
        HashMap<String, String> result = new HashMap<>();
        while (cursor.moveToNext()) {
            String feedId = cursor.getString(NodeHashcodeQuery.NODE_ID);
            String hashcode = cursor.getString(NodeHashcodeQuery.NODE_IMPORT_HASHCODE);
            result.put(feedId, hashcode == null ? "" : hashcode);
        }
        cursor.close();
        return result;
    }

    private interface NodeHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                V2exContract.Nodes.NODE_ID,
                V2exContract.Nodes.NODE_IMPORT_HASHCODE
        };
        final int _ID = 0;
        final int NODE_ID = 1;
        final int NODE_IMPORT_HASHCODE = 2;
    }
}
