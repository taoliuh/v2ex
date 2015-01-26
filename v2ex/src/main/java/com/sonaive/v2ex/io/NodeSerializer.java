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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sonaive.v2ex.io.model.Node;

import java.lang.reflect.Type;

/**
 * Created by liutao on 12/14/14.
 */
public class NodeSerializer implements JsonSerializer<Node> {
    @Override
    public JsonElement serialize(Node src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("id", new JsonPrimitive(src.id));
        result.add("name", new JsonPrimitive(src.name));
        result.add("title", new JsonPrimitive(src.title));
        result.add("title_alternative", new JsonPrimitive(src.title_alternative == null ?  "" : src.title_alternative));
        result.add("url", new JsonPrimitive(src.url));
        result.add("topics", new JsonPrimitive(src.topics));
        result.add("avatar_mini", new JsonPrimitive(src.avatar_mini));
        result.add("avatar_normal", new JsonPrimitive(src.avatar_normal));
        result.add("avatar_large", new JsonPrimitive(src.avatar_large));
        return result;
    }
}
