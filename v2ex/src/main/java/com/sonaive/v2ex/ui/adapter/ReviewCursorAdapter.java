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
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.util.ImageLoader;
import com.sonaive.v2ex.util.ModelUtils;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

/**
 * Created by liutao on 12/21/14.
 */
public class ReviewCursorAdapter extends PaginationCursorAdapter<ReviewCursorAdapter.ViewHolder> {

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
    public ReviewCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mImageLoader = new ImageLoader(context);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        if (cursor != null) {
            String content = cursor.getString(cursor.getColumnIndex(V2exContract.Reviews.REVIEW_CONTENT));
            String time = cursor.getString(cursor.getColumnIndex(V2exContract.Reviews.REVIEW_CREATED));
            String memberJson = cursor.getString(cursor.getColumnIndex(V2exContract.Reviews.REVIEW_MEMBER));

            Member member = ModelUtils.getAuthor(memberJson);

            holder.review.setText(content);
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
            
            holder.time.setText(DateUtils.getRelativeTimeSpanString(Long.valueOf(time) * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        }
    }

    @Override
    protected void onContentChanged() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView review;
        public ImageView avatar;
        public TextView time;
        public ViewHolder(View view) {
            super(view);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            name = (TextView) view.findViewById(R.id.name);
            time = (TextView) view.findViewById(R.id.time);
            review = (TextView) view.findViewById(R.id.review);
        }
    }
}
