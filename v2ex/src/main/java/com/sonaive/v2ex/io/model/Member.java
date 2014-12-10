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
 * Created by liutao on 12/9/14.
 */
public class Member {
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
}
