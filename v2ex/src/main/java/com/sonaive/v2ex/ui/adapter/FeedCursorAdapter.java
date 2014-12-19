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
package com.sonaive.v2ex.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.io.model.Member;
import com.sonaive.v2ex.io.model.Node;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.util.ImageLoader;
import com.sonaive.v2ex.util.ModelUtils;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

/**
 * Created by liutao on 12/18/14.
 */
public class FeedCursorAdapter extends PaginationCursorAdapter<FeedCursorAdapter.ViewHolder> {

    Context mContext;

    ImageLoader mImageLoader;

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter;
     *                Currently it accept {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public FeedCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mImageLoader = new ImageLoader(context);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        if (cursor != null) {
            String title = cursor.getString(cursor.getColumnIndex(V2exContract.Feeds.FEED_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(V2exContract.Feeds.FEED_CONTENT));
            String time = cursor.getString(cursor.getColumnIndex(V2exContract.Feeds.FEED_CREATED));
            String replies = cursor.getString(cursor.getColumnIndex(V2exContract.Feeds.FEED_REPLIES));
            String memberJson = cursor.getString(cursor.getColumnIndex(V2exContract.Feeds.FEED_MEMBER));
            String nodeJson = cursor.getString(cursor.getColumnIndex(V2exContract.Feeds.FEED_NODE));

            Member member = ModelUtils.getAuthor(memberJson);
            Node node = ModelUtils.getNode(nodeJson);

            holder.title.setText(title);
            if (content != null) {
                holder.content.setVisibility(View.VISIBLE);
                holder.content.setText(content);
            } else {
                holder.content.setVisibility(View.GONE);
            }
            if (member != null) {
                Drawable emptyAvatar = mContext.getResources().getDrawable(R.drawable.person_image_empty);
                String avatarUrl = member.avatar_large;
                if (avatarUrl != null) {
                    avatarUrl = avatarUrl.startsWith("http:") ? avatarUrl : "http:" + avatarUrl;
                } else {
                    avatarUrl = "";
                }
                mImageLoader.loadImage(avatarUrl, holder.avatar, null, emptyAvatar);
                holder.name.setText(member.username);
            }
            
            if (node != null) {
                holder.nodeTitle.setText(node.title);
            }
            
            holder.time.setText(DateUtils.getRelativeTimeSpanString(Long.valueOf(time) * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
            holder.replies.setText(replies + mContext.getString(R.string.noun_reply));
        }
    }

    @Override
    protected void onContentChanged() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView content;
        public ImageView avatar;
        public TextView name;
        public TextView time;
        public TextView replies;
        public TextView nodeTitle;
        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            content = (TextView) view.findViewById(R.id.content);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            name = (TextView) view.findViewById(R.id.name);
            time = (TextView) view.findViewById(R.id.time);
            replies = (TextView) view.findViewById(R.id.replies);
            nodeTitle = (TextView) view.findViewById(R.id.node_title);
        }
    }
}
