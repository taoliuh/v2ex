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
import com.sonaive.v2ex.util.ModelUtils;

/**
 *
 * Created by liutao on 12/13/14.
 */
public class Feed implements Parcelable {

    public int id;
    public String title;
    public String url;
    public String content;
    public String content_rendered;
    public int replies;
    public long created;
    public long last_modified;
    public long last_touched;
    public Member member = new Member();
    public Node node = new Node();

    public Feed() {}

    public String getImportHashcode() {
        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id)
                .append("title").append(title == null ? "" : title)
                .append("url").append(url == null ? "" : url)
                .append("content").append(content == null ? "" : content)
                .append("content_rendered").append(content_rendered == null ? "" : content_rendered)
                .append("replies").append(replies)
                .append("member").append(member == null ? "" : ModelUtils.serializeMember(member))
                .append("node").append(node == null ? "" : ModelUtils.serializeNode(node))
                .append("created").append(created)
                .append("last_modified").append(last_modified)
                .append("last_touched").append(last_touched);
        return HashUtils.computeWeakHash(sb.toString());
    }

    protected Feed(Parcel in) {
        id = in.readInt();
        title = in.readString();
        url = in.readString();
        content = in.readString();
        content_rendered = in.readString();
        replies = in.readInt();
        created = in.readLong();
        last_modified = in.readLong();
        last_touched = in.readLong();
        member = in.readParcelable(Member.class.getClassLoader());
        node = in.readParcelable(Node.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(content);
        dest.writeString(content_rendered);
        dest.writeInt(replies);
        dest.writeLong(created);
        dest.writeLong(last_modified);
        dest.writeLong(last_touched);
        dest.writeParcelable(member, flags);
        dest.writeParcelable(node, flags);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Feed> CREATOR = new Parcelable.Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel in) {
            return new Feed(in);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

}
