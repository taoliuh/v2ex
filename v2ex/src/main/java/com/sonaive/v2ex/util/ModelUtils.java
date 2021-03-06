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
package com.sonaive.v2ex.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.sonaive.v2ex.io.MemberSerializer;
import com.sonaive.v2ex.io.NodeSerializer;
import com.sonaive.v2ex.io.model.Member;
import com.sonaive.v2ex.io.model.Node;

import java.io.StringReader;

import static com.sonaive.v2ex.util.LogUtils.LOGE;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;
/**
 * Created by liutao on 12/15/14.
 */
public class ModelUtils {

    private static final String TAG = makeLogTag(ModelUtils.class);

    public static String serializeMember(Member member) {
        if (member != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Member.class, new MemberSerializer()).create();
            return gson.toJson(member);
        } else {
            return "";
        }
    }

    public static String serializeNode(Node node) {
        if (node != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Node.class, new NodeSerializer()).create();
            return gson.toJson(node);
        } else {
            return "";
        }
    }

    public static Member getAuthor(String json) {
        Member result = null;
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            JsonParser parser = new JsonParser();
            result = new Gson().fromJson(parser.parse(reader), Member.class);
        } catch (NullPointerException e) {
            LOGE(TAG, "Failed parsing member, JSON source is null, catch a NullPointerException");
        } catch (JsonIOException e) {
            LOGE(TAG, "Failed parsing JSON source: " + json + ", catch a JsonIOException");
        } catch (JsonSyntaxException e) {
            LOGE(TAG, "Failed parsing JSON source: " + json + ", catch a JsonSyntaxException");
        }
        return result;
    }

    public static Node getNode(String json) {
        Node result = null;
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            JsonParser parser = new JsonParser();
            result = new Gson().fromJson(parser.parse(reader), Node.class);
        } catch (NullPointerException e) {
            LOGE(TAG, "Failed parsing node, JSON source is null, catch a NullPointerException");
        } catch (JsonIOException e) {
            LOGE(TAG, "Failed parsing JSON source: " + json + ", catch a JsonIOException");
        } catch (JsonSyntaxException e) {
            LOGE(TAG, "Failed parsing JSON source: " + json + ", catch a JsonSyntaxException");
        }
        return result;
    }
}
