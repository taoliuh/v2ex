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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sonaive.v2ex.io.model.Member;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.api.Api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGE;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/10/14.
 */
public class MembersHandler extends JSONHandler {

    private static final String TAG = makeLogTag(MembersHandler.class);

    private HashMap<String, Member> mMembers = new HashMap<>();

    public MembersHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.Members.CONTENT_URI;
        HashMap<String, String> memberHashcodes = loadMemberHashcodes();
        HashSet<String> membersToKeep = new HashSet<>();
        boolean isIncrementalUpdate = memberHashcodes != null && memberHashcodes.size() > 0;

        if (isIncrementalUpdate) {
            LOGD(TAG, "Doing incremental update for members.");
        } else {
            LOGD(TAG, "Doing FULL (non incremental) update for members.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedMembers = 0;
        for (Member member : mMembers.values()) {
            String hashCode = member.getImportHashcode();
            membersToKeep.add(String.valueOf(member.id));

            // add member, if necessary
            if (!isIncrementalUpdate || !memberHashcodes.containsKey(String.valueOf(member.id)) ||
                    !memberHashcodes.get(String.valueOf(member.id)).equals(hashCode)) {
                ++updatedMembers;
                boolean isNew = !isIncrementalUpdate || !memberHashcodes.containsKey(String.valueOf(member.id));
                buildMember(isNew, member, list);
            }
        }

        LOGD(TAG, "Members: " + (isIncrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedMembers + " to update, New total: " + mMembers.size());
    }

    private void buildMember(boolean isInsert, Member member,
                            ArrayList<ContentProviderOperation> list) {
        Uri uri = V2exContract.Members.buildMemberUsernameUri(member.username);

        ContentProviderOperation.Builder builder;
        if (isInsert) {
            builder = ContentProviderOperation.newInsert(uri);
        } else {
            builder = ContentProviderOperation.newUpdate(uri);
        }

        list.add(builder.withValue(V2exContract.Members.MEMBER_ID, member.id)
                .withValue(V2exContract.Members.MEMBER_URL, member.url)
                .withValue(V2exContract.Members.MEMBER_USERNAME, member.username)
                .withValue(V2exContract.Members.MEMBER_WEBSITE, member.website)
                .withValue(V2exContract.Members.MEMBER_TWITTER, member.twitter)
                .withValue(V2exContract.Members.MEMBER_PSN, member.psn)
                .withValue(V2exContract.Members.MEMBER_GITHUB, member.github)
                .withValue(V2exContract.Members.MEMBER_BTC, member.btc)
                .withValue(V2exContract.Members.MEMBER_LOCATION, member.location)
                .withValue(V2exContract.Members.MEMBER_TAGLINE, member.tagline)
                .withValue(V2exContract.Members.MEMBER_BIO, member.bio)
                .withValue(V2exContract.Members.MEMBER_AVATAR_MINI, member.avatar_mini)
                .withValue(V2exContract.Members.MEMBER_AVATAR_NORMAL, member.avatar_normal)
                .withValue(V2exContract.Members.MEMBER_AVATAR_LARGE, member.avatar_large)
                .withValue(V2exContract.Members.MEMBER_CREATED, member.created)
                .withValue(V2exContract.Members.MEMBER_IMPORT_HASHCODE, member.getImportHashcode())
                .build());
    }

    private void buildDeleteOperation(String memberId, ArrayList<ContentProviderOperation> list) {
        Uri thisMemberUri = V2exContract.Members.buildMemberUsernameUri(memberId);
        list.add(ContentProviderOperation.newDelete(thisMemberUri).build());
    }

    @Override
    public void process(JsonElement element) {
        Member member = new Gson().fromJson(element, Member.class);
        mMembers.put(String.valueOf(member.id), member);
    }

    @Override
    public String getBody(Bundle data) {
        if (data != null) {
            return data.getString(Api.ARG_RESULT);
        }
        return "";
    }

    private HashMap<String, String> loadMemberHashcodes() {
        Uri uri = V2exContract.Members.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, MemberHashcodeQuery.PROJECTION,
                null, null, null);
        if (cursor == null) {
            LOGE(TAG, "Error querying member hashcodes (got null cursor)");
            return null;
        }
        if (cursor.getCount() < 1) {
            LOGE(TAG, "Error querying member hashcodes (no records returned)");
            return null;
        }
        HashMap<String, String> result = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            String videoId = cursor.getString(MemberHashcodeQuery.MEMBER_ID);
            String hashcode = cursor.getString(MemberHashcodeQuery.MEMBER_IMPORT_HASHCODE);
            result.put(videoId, hashcode == null ? "" : hashcode);
        }
        cursor.close();
        return result;
    }

    private interface MemberHashcodeQuery {
        String[] PROJECTION = {
                V2exContract.Members._ID,
                V2exContract.Members.MEMBER_ID,
                V2exContract.Members.MEMBER_IMPORT_HASHCODE
        };
        final int _ID = 0;
        final int MEMBER_ID = 1;
        final int MEMBER_IMPORT_HASHCODE = 2;
    }

}
