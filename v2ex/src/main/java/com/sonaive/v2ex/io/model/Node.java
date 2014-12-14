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

import com.sonaive.v2ex.util.HashUtils;

/**
 * Created by liutao on 12/14/14.
 */
public class Node {

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
}
