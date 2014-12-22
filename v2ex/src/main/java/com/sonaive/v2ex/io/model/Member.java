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

package com.sonaive.v2ex.io.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.sonaive.v2ex.util.HashUtils;

/**
 * Created by liutao on 12/9/14.
 */
public class Member implements Parcelable {
    public int id;
    public String url;
    public String username;
    public String website;
    public String twitter;
    public String psn;
    public String github;
    public String btc;
    public String location;
    public String tagline;
    public String bio;
    public String avatar_mini;
    public String avatar_normal;
    public String avatar_large;
    public long created;

    public Member() {}

    public String getImportHashcode() {
        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id)
                .append("url").append(url == null ? "" : url)
                .append("username").append(username == null ? "" : username)
                .append("website").append(website == null ? "" : website)
                .append("twitter").append(twitter == null ? "" : twitter)
                .append("psn").append(psn == null ? "" : psn)
                .append("github").append(github == null ? "" : github)
                .append("btc").append(btc == null ? "" : btc)
                .append("location").append(location == null ? "" : location)
                .append("tagline").append(tagline == null ? "" : tagline)
                .append("bio").append(bio == null ? "" : bio)
                .append("avatar_mini").append(avatar_mini == null ? "" : avatar_mini)
                .append("avatar_normal").append(avatar_normal == null ? "" : avatar_normal)
                .append("avatar_large").append(avatar_large == null ? "" : avatar_large)
                .append("created").append(created);
        return HashUtils.computeWeakHash(sb.toString());
    }

    protected Member(Parcel in) {
        id = in.readInt();
        url = in.readString();
        username = in.readString();
        website = in.readString();
        twitter = in.readString();
        psn = in.readString();
        github = in.readString();
        btc = in.readString();
        location = in.readString();
        tagline = in.readString();
        bio = in.readString();
        avatar_mini = in.readString();
        avatar_normal = in.readString();
        avatar_large = in.readString();
        created = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(username);
        dest.writeString(website);
        dest.writeString(twitter);
        dest.writeString(psn);
        dest.writeString(github);
        dest.writeString(btc);
        dest.writeString(location);
        dest.writeString(tagline);
        dest.writeString(bio);
        dest.writeString(avatar_mini);
        dest.writeString(avatar_normal);
        dest.writeString(avatar_large);
        dest.writeLong(created);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Member> CREATOR = new Parcelable.Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };
}
