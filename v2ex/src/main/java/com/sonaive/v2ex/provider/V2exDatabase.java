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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.sonaive.v2ex.provider.V2exContract.*;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGW;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/6/14.
 */
public class V2exDatabase extends SQLiteOpenHelper {

    private static final String TAG = makeLogTag(V2exDatabase.class);

    private static final String DATABASE_NAME = "v2ex.db";

    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.

    private static final int VER_2014_RELEASE_A = 1; // app version 1.0
    private static final int CUR_DATABASE_VERSION = VER_2014_RELEASE_A;

    private final Context mContext;

    interface Tables {
        String MEMBERS = "members";
        String FEEDS = "feeds";
        String NODES = "nodes";
        String REVIEWS = "reviews";
        String PICASA_IMAGES = "picasa_images";
        String MODI_DATE = "modi_date";
    }

    public V2exDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.MEMBERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MemberColumns.MEMBER_ID + " TEXT NOT NULL,"
                + MemberColumns.MEMBER_URL + " TEXT NOT NULL,"
                + MemberColumns.MEMBER_USERNAME + " TEXT NOT NULL,"
                + MemberColumns.MEMBER_WEBSITE + " TEXT,"
                + MemberColumns.MEMBER_TWITTER + " TEXT,"
                + MemberColumns.MEMBER_PSN + " TEXT,"
                + MemberColumns.MEMBER_GITHUB + " TEXT,"
                + MemberColumns.MEMBER_BTC + " TEXT,"
                + MemberColumns.MEMBER_LOCATION + " TEXT,"
                + MemberColumns.MEMBER_TAGLINE + " TEXT,"
                + MemberColumns.MEMBER_BIO + " TEXT,"
                + MemberColumns.MEMBER_AVATAR_MINI + " TEXT,"
                + MemberColumns.MEMBER_AVATAR_NORMAL + " TEXT,"
                + MemberColumns.MEMBER_AVATAR_LARGE + " TEXT,"
                + MemberColumns.MEMBER_CREATED + " TEXT NOT NULL,"
                + MemberColumns.MEMBER_IMPORT_HASHCODE + " TEXT NOT NULL,"
                + "UNIQUE (" + MemberColumns.MEMBER_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.FEEDS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FeedColumns.FEED_ID + " TEXT NOT NULL,"
                + FeedColumns.FEED_TITLE + " TEXT NOT NULL,"
                + FeedColumns.FEED_URL + " TEXT NOT NULL,"
                + FeedColumns.FEED_CONTENT + " TEXT NOT NULL,"
                + FeedColumns.FEED_CONTENT_RENDERED + " TEXT NOT NULL,"
                + FeedColumns.FEED_REPLIES + " TEXT NOT NULL,"
                + FeedColumns.FEED_MEMBER + " TEXT,"
                + FeedColumns.FEED_NODE + " TEXT,"
                + FeedColumns.FEED_CREATED + " TEXT NOT NULL,"
                + FeedColumns.FEED_LAST_MODIFIED + " TEXT NOT NULL,"
                + FeedColumns.FEED_LAST_TOUCHED + " TEXT NOT NULL,"
                + FeedColumns.FEED_TYPE + " TEXT NOT NULL,"
                + FeedColumns.FEED_IMPORT_HASHCODE + " TEXT NOT NULL,"
                + "UNIQUE (" + FeedColumns.FEED_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.NODES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NodeColumns.NODE_ID + " TEXT NOT NULL,"
                + NodeColumns.NODE_NAME + " TEXT NOT NULL,"
                + NodeColumns.NODE_URL + " TEXT NOT NULL,"
                + NodeColumns.NODE_TITLE + " TEXT NOT NULL,"
                + NodeColumns.NODE_TITLE_ALTERNATIVE + " TEXT,"
                + NodeColumns.NODE_TOPICS + " TEXT NOT NULL,"
                + NodeColumns.NODE_HEADER + " TEXT,"
                + NodeColumns.NODE_FOOTER + " TEXT,"
                + NodeColumns.NODE_CREATED + " TEXT NOT NULL,"
                + NodeColumns.NODE_AVATAR_MINI + " TEXT,"
                + NodeColumns.NODE_AVATAR_NORMAL + " TEXT,"
                + NodeColumns.NODE_AVATAR_LARGE + " TEXT,"
                + NodeColumns.NODE_IMPORT_HASHCODE + " TEXT NOT NULL,"
                + "UNIQUE (" + NodeColumns.NODE_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.REVIEWS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ReviewColumns.REVIEW_ID + " TEXT NOT NULL,"
                + ReviewColumns.REVIEW_THANKS + " TEXT,"
                + ReviewColumns.REVIEW_CONTENT + " TEXT NOT NULL,"
                + ReviewColumns.REVIEW_CONTENT_RENDERED + " TEXT,"
                + ReviewColumns.REVIEW_MEMBER + " TEXT NOT NULL,"
                + ReviewColumns.REVIEW_CREATED + " TEXT,"
                + ReviewColumns.REVIEW_LAST_MODIFIED + " TEXT,"
                + ReviewColumns.REVIEW_IMPORT_HASHCODE + " TEXT NOT NULL,"
                + "UNIQUE (" + ReviewColumns.REVIEW_ID + ") ON CONFLICT REPLACE)");

        // Defines an SQLite statement that builds the Picasa picture URL table
        db.execSQL("CREATE TABLE " + Tables.PICASA_IMAGES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PicasaImageColumns.PICASA_THUMB_URL + " TEXT,"
                + PicasaImageColumns.PICASA_IMAGE_URL + " TEXT,"
                + PicasaImageColumns.PICASA_THUMB_URL_NAME + " TEXT,"
                + PicasaImageColumns.PICASA_IMAGE_NAME + " TEXT)");

        // Defines an SQLite statement that builds the URL modification date table
        db.execSQL("CREATE TABLE " + Tables.MODI_DATE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ModiDateColumns.MODI_DATE + " INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGD(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // Current DB version. We update this variable as we perform upgrades to reflect
        // the current version we are in.
        int version = oldVersion;

        // Indicates whether the data we currently have should be invalidated as a
        // result of the db upgrade. Default is true (invalidate); if we detect that this
        // is a trivial DB upgrade, we set this to false.
        boolean dataInvalidated = true;

        LOGD(TAG, "After upgrade logic, at version " + version);

        // at this point, we ran out of upgrade logic, so if we are still at the wrong
        // version, we have no choice but to delete everything and create everything again.
        if (version != CUR_DATABASE_VERSION) {
            LOGW(TAG, "Upgrade unsuccessful -- destroying old data during upgrade");
            db.execSQL("DROP TABLE IF EXISTS " + Tables.MEMBERS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.FEEDS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.NODES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.REVIEWS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PICASA_IMAGES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.MODI_DATE);
            onCreate(db);
        }

        if (dataInvalidated) {

        }
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
