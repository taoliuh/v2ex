/*
 * Copyright 2014 so_naive. All rights reserved.
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

package com.sonaive.v2ex;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liutao on 12/6/14.
 */
public class Api {
    public static final String API_HOST_URL = "http://www.v2ex.com";
    public static final String API_LATEST = "/topics/latest.json";
    public static final String API_ALL_NODES = "/nodes/all.json";
    public static final String API_REPLIES = "/replies/show.json";
    public static final String API_TOPIC = "/topics/show.json";
    public static final String API_USER = "/members/show.json";
    public static final String API_SIGNIN = "/signin";
    public static final String API_MY_NODES = "/my/nodes";
    public static final String API_USER_IDENTITY = "/api/members/show.json";

    public static Map<String, String> API_URLS = new HashMap<>();

    static {
        API_URLS.put(API_LATEST, API_HOST_URL + API_LATEST);
        API_URLS.put(API_ALL_NODES, API_HOST_URL + API_ALL_NODES);
        API_URLS.put(API_REPLIES, API_HOST_URL + API_REPLIES);
        API_URLS.put(API_TOPIC, API_HOST_URL + API_TOPIC);
        API_URLS.put(API_USER, API_HOST_URL + API_USER);
        API_URLS.put(API_SIGNIN, API_HOST_URL + API_SIGNIN);
        API_URLS.put(API_MY_NODES, API_HOST_URL + API_MY_NODES);
        API_URLS.put(API_USER_IDENTITY, API_HOST_URL + API_USER_IDENTITY);
    }

}
