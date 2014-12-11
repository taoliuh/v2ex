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

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

/**
 * Contract class for interacting with {@link com.sonaive.v2ex.provider.V2exProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link android.net.Uri}
 * are generated using stronger {@link String} identifiers, instead of
 * {@code int} {@link android.provider.BaseColumns#_ID} values, which are prone to shuffle during
 * sync.
 *
 * Created by liutao on 12/6/14.
 */
public class V2exContract {

    /**
     * Query parameter to create a distinct query.
     */
    public static final String QUERY_PARAMETER_DISTINCT = "distinct";
    public static final String OVERRIDE_ACCOUNTNAME_PARAMETER = "overrideAccount";

    interface MemberColumns {
        String MEMBER_ID = "member_id";
        /**
         * User web page of v2ex
         */
        String MEMBER_URL = "member_url";
        String MEMBER_USERNAME = "member_username";
        /**
         * User personal website
         */
        String MEMBER_WEBSITE = "member_website";
        String MEMBER_TWITTER = "member_twitter";
        String MEMBER_PSN = "member_psn";
        String MEMBER_GITHUB = "member_github";
        String MEMBER_BTC = "member_btc";
        String MEMBER_LOCATION = "member_location";
        String MEMBER_TAGLINE = "member_tagline";
        String MEMBER_BIO = "member_bio";
        String MEMBER_AVATAR_MINI = "member_avatar_mini";
        String MEMBER_AVATAR_NORMAL = "member_avatar_normal";
        String MEMBER_AVATAR_LARGE = "member_avatar_large";
        String MEMBER_CREATED = "member_created";
        String MEMBER_IMPORT_HASHCODE = "member_import_hashcode";
    }

    interface PicasaImageColumns {
        String PICASA_THUMB_URL = "picasa_thumb_url";
        String PICASA_THUMB_URL_NAME = "picasa_thumb_url_name";
        String PICASA_IMAGE_URL = "picasa_image_url";
        String PICASA_IMAGE_NAME = "picasa_image_name";
    }

    interface ModiDateColumns {
        String MODI_DATE = "modi_date";
    }

    public static final String CONTENT_AUTHORITY = "com.sonaive.v2ex";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_MEMBERS = "members";
    private static final String PATH_PICASA_IMAGES = "picasas";
    private static final String PATH_MODI_DATES = "dates";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_MEMBERS
    };

    public static class Members implements MemberColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MEMBERS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.v2ex.members";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.v2ex.members";

        /** Build {@link Uri} for given member. */
        public static Uri buildMemberUsernameUri(String username) {
            return CONTENT_URI.buildUpon().appendPath(username).build();
        }

        /** Return member USERNAME given URI. */
        public static String getMemberUsername(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static class PicasaImages implements PicasaImageColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PICASA_IMAGES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.v2ex.picasaimages";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.v2ex.picasaimages";

        /** Build {@link Uri} for given member. */
        public static Uri buildPicasaImagesUri(String picasaId) {
            return CONTENT_URI.buildUpon().appendPath(picasaId).build();
        }
    }

    public static class ModiDates implements ModiDateColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MODI_DATES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.v2ex.modidates";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.v2ex.modidates";
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",
                uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }

}
