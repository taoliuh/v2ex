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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.sonaive.v2ex.util.HashUtils;

import java.io.StringReader;

import static com.sonaive.v2ex.util.LogUtils.LOGE;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 *
 * Created by liutao on 12/13/14.
 */
public class Feed {
    private static final String TAG = makeLogTag(Feed.class);
    public int id;
    public String title;
    public String url;
    public String content;
    public String content_rendered;
    public int replies;
//    public String member;
//    public String node;
    public long created;
    public long last_modified;
    public long last_touched;

    public String getImportHashcode() {
        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id)
                .append("title").append(title == null ? "" : title)
                .append("url").append(url == null ? "" : url)
                .append("content").append(content == null ? "" : content)
                .append("content_rendered").append(content_rendered == null ? "" : content_rendered)
                .append("replies").append(replies)
//                .append("member").append(member == null ? "" : member)
//                .append("node").append(node == null ? "" : node)
                .append("created").append(created)
                .append("last_modified").append(last_modified)
                .append("last_touched").append(last_touched);
        return HashUtils.computeWeakHash(sb.toString());
    }

//    public Member getAuthor() {
//        Member result = null;
//        try {
//            JsonReader reader = new JsonReader(new StringReader(member));
//            JsonParser parser = new JsonParser();
//            result = new Gson().fromJson(parser.parse(reader), Member.class);
//        } catch (NullPointerException e) {
//            LOGE(TAG, "Member field is null, catch a NullPointerException");
//        } catch (JsonIOException e) {
//            LOGE(TAG, "Failed parsing JSON source: " + member + ", catch a JsonIOException");
//        } catch (JsonSyntaxException e) {
//            LOGE(TAG, "Failed parsing JSON source: " + member + ", catch a JsonSyntaxException");
//        }
//        return result;
//    }

}
