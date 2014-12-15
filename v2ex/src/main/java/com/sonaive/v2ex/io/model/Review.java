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
import com.sonaive.v2ex.util.ModelUtils;

/**
 * Created by liutao on 12/15/14.
 */
public class Review {

    public int id;
    public int thanks;
    public String content;
    public String content_rendered;
    public Member member;
    public long created;
    public long last_modified;

    public String getImportHashcode() {
        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id)
                .append("thanks").append(thanks)
                .append("content").append(content == null ? "" : content)
                .append("content_rendered").append(content_rendered == null ? "" : content_rendered)
                .append("member").append(member == null ? "" : ModelUtils.serializeMember(member))
                .append("created").append(created)
                .append("last_modified").append(last_modified);
        return HashUtils.computeWeakHash(sb.toString());
    }
}
