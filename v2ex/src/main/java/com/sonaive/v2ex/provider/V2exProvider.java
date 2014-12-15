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

package com.sonaive.v2ex.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.sonaive.v2ex.provider.V2exContract.*;
import com.sonaive.v2ex.util.SelectionBuilder;

import java.util.ArrayList;
import java.util.Arrays;

import com.sonaive.v2ex.provider.V2exDatabase.*;

import static com.sonaive.v2ex.util.LogUtils.LOGV;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/6/14.
 */
public class V2exProvider extends ContentProvider {

    private static final String TAG = makeLogTag(V2exProvider.class);

    private static final int MEMBERS = 100;
    private static final int MEMBERS_USERNAME = 101;

    private static final int PICASAS = 200;
    private static final int PICASAS_ID = 201;

    private static final int DATE = 300;

    private static final int FEEDS = 400;
    private static final int FEEDS_ID = 401;

    private static final int NODES = 500;
    private static final int NODES_ID = 501;

    private static final int REVIEWS = 600;
    private static final int REVIEWS_ID = 601;

    private V2exDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = V2exContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "members", MEMBERS);
        matcher.addURI(authority, "members/*", MEMBERS_USERNAME);

        matcher.addURI(authority, "feeds", FEEDS);
        matcher.addURI(authority, "feeds/*", FEEDS_ID);

        matcher.addURI(authority, "nodes", NODES);
        matcher.addURI(authority, "nodes/*", NODES_ID);

        matcher.addURI(authority, "reviews", REVIEWS);
        matcher.addURI(authority, "reviews/*", REVIEWS_ID);

        matcher.addURI(authority, "picasas", PICASAS);
        matcher.addURI(authority, "picasas/*", PICASAS_ID);

        matcher.addURI(authority, "dates", DATE);
        return matcher;

    }

    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mOpenHelper.close();
        Context context = getContext();
        V2exDatabase.deleteDatabase(context);
        mOpenHelper = new V2exDatabase(getContext());
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new V2exDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEMBERS: {
                return Members.CONTENT_TYPE;
            }
            case MEMBERS_USERNAME: {
                return Members.CONTENT_ITEM_TYPE;
            }
            case FEEDS: {
                return Feeds.CONTENT_TYPE;
            }
            case FEEDS_ID: {
                return Feeds.CONTENT_ITEM_TYPE;
            }
            case NODES: {
                return Nodes.CONTENT_TYPE;
            }
            case NODES_ID: {
                return Nodes.CONTENT_ITEM_TYPE;
            }
            case REVIEWS: {
                return Reviews.CONTENT_TYPE;
            }
            case REVIEWS_ID: {
                return Reviews.CONTENT_ITEM_TYPE;
            }
            case PICASAS: {
                return PicasaImages.CONTENT_TYPE;
            }
            case PICASAS_ID: {
                return PicasaImages.CONTENT_ITEM_TYPE;
            }
            case DATE: {
                return ModiDates.CONTENT_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);

        // avoid the expensive string concatenation below if not loggable
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            LOGV(TAG, "uri=" + uri + " match=" + match + " proj=" + Arrays.toString(projection) +
                    " selection=" + selection + " args=" + Arrays.toString(selectionArgs) + ")");
        }

        switch (match) {
            default:
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildExpandedSelection(uri, match);

                boolean distinct = !TextUtils.isEmpty(
                        uri.getQueryParameter(V2exContract.QUERY_PARAMETER_DISTINCT));

                Cursor cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, distinct, projection, sortOrder, null);
                Context context = getContext();
                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LOGV(TAG, "insert(uri=" + uri + ", values=" + values.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // For the modification date table
            case DATE: {

                // Inserts the row into the table and returns the new row's _id value
                long id = db.insert(
                        Tables.MODI_DATE,
                        ModiDates.MODI_DATE,
                        values
                );

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    notifyChange(uri);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
            }
            case MEMBERS_USERNAME: {
                db.insertOrThrow(Tables.MEMBERS, null, values);
                notifyChange(uri);
                return Members.buildMemberUsernameUri(values.getAsString(Members.MEMBER_USERNAME));
            }
            case FEEDS: {
                db.insertOrThrow(Tables.FEEDS, null, values);
                notifyChange(uri);
                return Feeds.buildFeedUri(values.getAsString(Feeds.FEED_ID));
            }
            case NODES: {
                db.insertOrThrow(Tables.NODES, null, values);
                notifyChange(uri);
                return Nodes.buildNodeUri(values.getAsString(Nodes.NODE_ID));
            }
            case REVIEWS: {
                db.insertOrThrow(Tables.REVIEWS, null, values);
                notifyChange(uri);
                return Reviews.buildReviewUri(values.getAsString(Reviews.REVIEW_ID));
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LOGV(TAG, "delete(uri=" + uri);
        if (uri == V2exContract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        LOGV(TAG, "update(uri=" + uri + ", values=" + values.toString());
        // Decodes the content URI and choose which insert to use
        switch (match) {
            default: {

                final SelectionBuilder builder = buildSimpleSelection(uri);
                int retVal = builder.where(selection, selectionArgs).update(db, values);
                notifyChange(uri);
                return retVal;
            }

            // A picture URL content URI
            case DATE: {

                // Updates the table
                int rows = db.update(
                        Tables.MODI_DATE,
                        values,
                        selection,
                        selectionArgs);

                // If the update succeeded, notify a change and return the number of updated rows.
                if (0 != rows) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rows;
                } else {
                    throw new SQLiteException("Update error:" + uri);
                }
            }
            case PICASAS: {
                throw new UnsupportedOperationException("Update: Invalid URI: " + uri);
            }
        }
    }

    /**
     * Implements bulk row insertion using
     * {@link android.database.sqlite.SQLiteDatabase#insert(String, String, android.content.ContentValues) SQLiteDatabase.insert()}
     * and SQLite transactions. The method also notifies the current
     * {@link android.content.ContentResolver} that the {@link android.content.ContentProvider} has
     * been changed.
     * @see android.content.ContentProvider#bulkInsert(android.net.Uri, android.content.ContentValues[])
     * @param uri The content URI for the insertion
     * @param insertValuesArray A {@link android.content.ContentValues} array containing the row to
     * insert
     * @return The number of rows inserted.
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] insertValuesArray) {

        // Gets a writeable database instance if one is not already cached
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // picture URLs table
            case PICASAS:
                /*
                 * Begins a transaction in "exclusive" mode. No other mutations can occur on the
                 * db until this transaction finishes.
                 */
                db.beginTransaction();

                // Deletes all the existing rows in the table
                db.delete(Tables.PICASA_IMAGES, null, null);

                // Gets the size of the bulk insert
                int numImages = insertValuesArray.length;

                // Inserts each ContentValues entry in the array as a row in the database
                for (int i = 0; i < numImages; i++) {

                    db.insert(Tables.PICASA_IMAGES,
                            PicasaImages.PICASA_IMAGE_URL, insertValuesArray[i]);
                }

                // Reports that the transaction was successful and should not be backed out.
                db.setTransactionSuccessful();

                // Ends the transaction and closes the current db instances
                db.endTransaction();
                db.close();

                /*
                 * Notifies the current ContentResolver that the data associated with "uri" has
                 * changed.
                 */

                getContext().getContentResolver().notifyChange(uri, null);

                // The semantics of bulkInsert is to return the number of rows inserted.
                return numImages;

            default:
                throw new IllegalArgumentException("Bulk insert -- Invalid URI:" + uri);
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private void notifyChange(Uri uri) {
        // We only notify changes if the caller is not the sync adapter.
        // The sync adapter has the responsibility of notifying changes (it can do so
        // more intelligently than we can -- for example, doing it only once at the end
        // of the sync instead of issuing thousands of notifications for each record).
        if (!V2exContract.hasCallerIsSyncAdapterParameter(uri)) {
            Context context = getContext();
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEMBERS:
                return builder.table(Tables.MEMBERS);
            case MEMBERS_USERNAME: {
                final String username = Members.getMemberUsername(uri);
                return builder.table(Tables.MEMBERS).where(Members.MEMBER_USERNAME + "=?", username);
            }
            case FEEDS: {
                return builder.table(Tables.FEEDS);
            }
            case FEEDS_ID: {
                final String feedId = Feeds.getFeedId(uri);
                return builder.table(Tables.FEEDS).where(Feeds.FEED_ID + "=?", feedId);
            }
            case NODES: {
                return builder.table(Tables.NODES);
            }
            case NODES_ID: {
                final String nodeId = Nodes.getNodeId(uri);
                return builder.table(Tables.NODES).where(Nodes.NODE_ID + "=?", nodeId);
            }
            case REVIEWS: {
                return builder.table(Tables.REVIEWS);
            }
            case REVIEWS_ID: {
                final String reviewId = Reviews.getReviewId(uri);
                return builder.table(Tables.REVIEWS).where(Reviews.REVIEW_ID + "=?", reviewId);
            }
            case PICASAS: {
                return builder.table(Tables.PICASA_IMAGES);
            }
            case DATE: {
                return builder.table(Tables.MODI_DATE);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case MEMBERS: {
                return builder.table(Tables.MEMBERS);
            }
            case MEMBERS_USERNAME: {
                final String username = Members.getMemberUsername(uri);
                return builder.table(Tables.MEMBERS).where(Members.MEMBER_USERNAME + "=?", username);
            }
            case FEEDS: {
                return builder.table(Tables.FEEDS);
            }
            case FEEDS_ID: {
                final String feedId = Feeds.getFeedId(uri);
                return builder.table(Tables.FEEDS).where(Feeds.FEED_ID + "=?", feedId);
            }
            case NODES: {
                return builder.table(Tables.NODES);
            }
            case NODES_ID: {
                final String nodeId = Nodes.getNodeId(uri);
                return builder.table(Tables.NODES).where(Nodes.NODE_ID + "=?", nodeId);
            }
            case REVIEWS: {
                return builder.table(Tables.REVIEWS);
            }
            case REVIEWS_ID: {
                final String reviewId = Reviews.getReviewId(uri);
                return builder.table(Tables.REVIEWS).where(Reviews.REVIEW_ID + "=?", reviewId);
            }
            case PICASAS: {
                return builder.table(Tables.PICASA_IMAGES);
            }
            case DATE: {
                return builder.table(Tables.MODI_DATE);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

    }
}
