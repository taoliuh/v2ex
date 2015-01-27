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
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.ui.BaseActivity;
import com.sonaive.v2ex.ui.FeedsActivity;
import com.sonaive.v2ex.widget.CursorAdapter;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

/**
 * Created by liutao on 12/15/14.
 */
public class NodeCursorAdapter extends PaginationCursorAdapter<NodeCursorAdapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter;
     *                Currently it accept {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public NodeCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
        mCursor = c;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        return super.swapCursor(newCursor);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        if (cursor != null) {
            String title = cursor.getString(cursor.getColumnIndex(V2exContract.Nodes.NODE_TITLE));
            String header = cursor.getString(cursor.getColumnIndex(V2exContract.Nodes.NODE_HEADER));
            String topics = cursor.getString(cursor.getColumnIndex(V2exContract.Nodes.NODE_TOPICS));
            holder.title.setText(title);
            if (header == null || TextUtils.isEmpty(header.trim())) {
                holder.header.setVisibility(View.GONE);
            } else {
                holder.header.setVisibility(View.VISIBLE);
                holder.header.setText(Html.fromHtml(header));
            }
            holder.topics.setText(topics + "  topics");
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCursor != null && mCursor.moveToPosition(holder.getPosition())) {
                        String nodeTitle = mCursor.getString(mCursor.getColumnIndex(V2exContract.Nodes.NODE_TITLE));
                        int nodeId = mCursor.getInt(mCursor.getColumnIndex(V2exContract.Nodes.NODE_ID));
                        Intent intent = FeedsActivity.getCallingIntent(mContext, nodeTitle, nodeId);
                        mContext.startActivity(intent);
                    }

                }
            });
        }
    }

    @Override
    protected void onContentChanged() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView card;
        public TextView title;
        public TextView header;
        public TextView topics;
        public ViewHolder(View view) {
            super(view);
            card = (CardView) view.findViewById(R.id.card_container);
            title = (TextView) view.findViewById(R.id.title);
            header = (TextView) view.findViewById(R.id.header);
            topics = (TextView) view.findViewById(R.id.topics_num);
        }
    }
}
