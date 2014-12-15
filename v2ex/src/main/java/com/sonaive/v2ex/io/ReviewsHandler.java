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

        LOGD(TAG, "Reviews: " + (isIncrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedReviews + " to update, New total: " + mReviews.size());
    }

    @Override
    public void process(JsonElement element) {
        if (element.isJsonArray()) {
            for (Review review : new Gson().fromJson(element, Review[].class)) {
                mReviews.put(String.valueOf(review.id), review);
            }
        }
    }

    @Override
    public String getBody(Bundle data) {
        if (data != null) {
            return data.getString(Api.ARG_RESULT);
        }
        return "";
    }

    private void buildReview(boolean isInsert, Review review,
                             ArrayList<ContentProviderOperation> list) {
        Uri allReviewsUri = V2exContract.Reviews.CONTENT_URI;
        Uri thisReviewUri = V2exContract.Reviews.buildReviewUri(String.valueOf(review.id));

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
        Uri uri = V2exContract.Reviews.buildReviewUri(reviewId);
        list.add(ContentProviderOperation.newDelete(uri).build());
    }

    private HashMap<String, String> loadReviewHashcodes() {
        Uri uri = V2exContract.Reviews.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, ReviewHashcodeQuery.PROJECTION,
                null, null, null);
        if (cursor == null) {
            LOGE(TAG, "Error querying REVIEW hashcodes (got null cursor)");
            return null;
        }
        if (cursor.getCount() < 1) {
            LOGE(TAG, "Error querying REVIEW hashcodes (no records returned)");
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
