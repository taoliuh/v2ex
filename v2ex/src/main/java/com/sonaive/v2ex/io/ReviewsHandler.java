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
import com.google.gson.JsonObject;
import com.sonaive.v2ex.io.model.Review;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.api.Api;
import com.sonaive.v2ex.util.ModelUtils;

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
public class ReviewsHandler extends JSONHandler {

    private static final String TAG = makeLogTag(ReviewsHandler.class);

    private HashMap<String, Review> mReviews = new HashMap<>();

    private int page = 1;

    private String topicId;

    public ReviewsHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.Reviews.CONTENT_URI;
        HashMap<String, String> reviewHashcodes = loadReviewHashcodes();
        HashSet<String> reviewsToKeep = new HashSet<>();
        boolean isIncrementalUpdate = reviewHashcodes != null && reviewHashcodes.size() > 0;

        if (isIncrementalUpdate) {
            LOGD(TAG, "Doing incremental update for reviews.");
        } else {
            LOGD(TAG, "Doing FULL (non incremental) update for reviews.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedReviews = 0;
        for (Review review : mReviews.values()) {
            String hashCode = review.getImportHashcode();
            reviewsToKeep.add(String.valueOf(review.id));

            // add review, if necessary
            if (!isIncrementalUpdate || !reviewHashcodes.containsKey(String.valueOf(review.id)) ||
                    !reviewHashcodes.get(String.valueOf(review.id)).equals(hashCode)) {
                ++updatedReviews;
                boolean isNew = !isIncrementalUpdate || !reviewHashcodes.containsKey(String.valueOf(review.id));
                buildReview(isNew, review, list);
            }
        }

        int deletedReviews = 0;
        if (isIncrementalUpdate) {
            for (String posterId : reviewHashcodes.keySet()) {
                if (!reviewsToKeep.contains(posterId)) {
                    buildDeleteOperation(posterId, list);
                    ++deletedReviews;
                }
            }
        }
        LOGD(TAG, "Reviews: " + (isIncrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedReviews + " to update, " + deletedReviews + " to delete, New total: " + mReviews.size());
    }

    @Override
    public void process(JsonElement element) {
        if (element.isJsonArray()) {
            for (Review review : new Gson().fromJson(element, Review[].class)) {
                mReviews.put(String.valueOf(review.id), review);
            }
        }
        // Due to api hasn't supported pagination.The following code will be commented.
//        if (mReviews.size() > 0) {
//            EventBus.getDefault().postSticky(new UpdateLoadingStateEvent(LoadingStatus.FINISH));
//        } else {
//            EventBus.getDefault().postSticky(new UpdateLoadingStateEvent(LoadingStatus.NO_MORE_DATA));
//        }
    }

    @Override
    public String getBody(Bundle data) {
        if (data != null) {
            String requestParams = data.getString(Api.ARG_API_PARAMS);
            JsonObject jo = new Gson().fromJson(requestParams, JsonObject.class);
            JsonElement topicIdJsonElement = jo.get("topic_id");
            if (topicIdJsonElement != null) {
                topicId = topicIdJsonElement.getAsString();
            }
            return data.getString(Api.ARG_RESULT);
        }
        return "";
    }

    private void buildReview(boolean isInsert, Review review,
                             ArrayList<ContentProviderOperation> list) {
        Uri allReviewsUri = V2exContract.Reviews.CONTENT_URI;
        Uri thisReviewUri = V2exContract.Reviews.buildReviewTopicUri(String.valueOf(review.id));

        ContentProviderOperation.Builder builder;
        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allReviewsUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisReviewUri);
        }

        if (TextUtils.isEmpty(String.valueOf(review.id))) {
            LOGW(TAG, "Ignoring feed with missing feed ID.");
            return;
        }

        list.add(builder.withValue(V2exContract.Reviews.REVIEW_ID, review.id)
                .withValue(V2exContract.Reviews.REVIEW_TOPIC_ID, topicId)
                .withValue(V2exContract.Reviews.REVIEW_THANKS, review.thanks)
                .withValue(V2exContract.Reviews.REVIEW_CONTENT, review.content)
                .withValue(V2exContract.Reviews.REVIEW_CONTENT_RENDERED, review.content_rendered)
                .withValue(V2exContract.Reviews.REVIEW_MEMBER, ModelUtils.serializeMember(review.member))
                .withValue(V2exContract.Reviews.REVIEW_CREATED, review.created)
                .withValue(V2exContract.Reviews.REVIEW_LAST_MODIFIED, review.last_modified)
                .withValue(V2exContract.Reviews.REVIEW_IMPORT_HASHCODE, review.getImportHashcode())
                .build());
    }

    private void buildDeleteOperation(String reviewId, ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.Reviews.buildReviewTopicUri(reviewId);
        list.add(ContentProviderOperation.newDelete(uri).build());
    }

    private HashMap<String, String> loadReviewHashcodes() {
//        int limit = page * Config.PAGE_SIZE;
//        int offset = (page - 1) * Config.PAGE_SIZE;
//        // Put the limit clause as a query parameter using the syntax 'limit = offset, limit'
//        Uri uri = V2exContract.Reviews.CONTENT_URI.buildUpon().encodedQuery("limit=" + offset + "," + limit).build();
        Uri uri = V2exContract.Reviews.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, ReviewHashcodeQuery.PROJECTION,
                "review_topic_id = ?", new String[] {topicId}, V2exContract.Reviews.REVIEW_LAST_MODIFIED + " DESC");
        if (cursor == null) {
            LOGE(TAG, "Error querying REVIEW hashcodes (got null cursor)");
            return null;
        }
        if (cursor.getCount() < 1) {
            LOGE(TAG, "Error querying REVIEW hashcodes (no records returned)");
            cursor.close();
            return null;
        }
        HashMap<String, String> result = new HashMap<>();
        while (cursor.moveToNext()) {
            String reviewId = cursor.getString(ReviewHashcodeQuery.REVIEW_ID);
            String hashcode = cursor.getString(ReviewHashcodeQuery.REVIEW_IMPORT_HASHCODE);
            result.put(reviewId, hashcode == null ? "" : hashcode);
        }
        cursor.close();
        return result;
    }

    private interface ReviewHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                V2exContract.Reviews.REVIEW_ID,
                V2exContract.Reviews.REVIEW_IMPORT_HASHCODE
        };
        final int _ID = 0;
        final int REVIEW_ID = 1;
        final int REVIEW_IMPORT_HASHCODE = 2;
    }
}
