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
 * Created by liutao on 12/14/14.
 */
public class Node implements Parcelable {

    public int id;
    public String name;
    public String url;
    public String title;
    public String title_alternative;
    public int topics;
    public String header;
    public String footer;
    public long created;
    public String avatar_mini;
    public String avatar_normal;
    public String avatar_large;

    public Node() {}

    public String getImportHashcode() {
        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id)
                .append("name").append(name == null ? "" : name)
                .append("url").append(url == null ? "" : url)
                .append("title").append(title == null ? "" : title)
                .append("title_alternative").append(title_alternative == null ? "" : title_alternative)
                .append("topics").append(topics)
                .append("header").append(header == null ? "" : header)
                .append("footer").append(footer == null ? "" : footer)
                .append("created").append(created)
                .append("avatar_mini").append(avatar_mini == null ? "" : avatar_mini)
                .append("avatar_normal").append(avatar_normal == null ? "" : avatar_normal)
                .append("avatar_large").append(avatar_large == null ? "" : avatar_large);
        return HashUtils.computeWeakHash(sb.toString());
    }

    protected Node(Parcel in) {
        id = in.readInt();
        name = in.readString();
        url = in.readString();
        title = in.readString();
        title_alternative = in.readString();
        topics = in.readInt();
        header = in.readString();
        footer = in.readString();
        created = in.readLong();
        avatar_mini = in.readString();
        avatar_normal = in.readString();
        avatar_large = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(title_alternative);
        dest.writeInt(topics);
        dest.writeString(header);
        dest.writeString(footer);
        dest.writeLong(created);
        dest.writeString(avatar_mini);
        dest.writeString(avatar_normal);
        dest.writeString(avatar_large);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Node> CREATOR = new Parcelable.Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };
}
