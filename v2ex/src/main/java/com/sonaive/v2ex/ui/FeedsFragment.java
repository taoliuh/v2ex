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
package com.sonaive.v2ex.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.ui.adapter.FeedCursorAdapter;
import com.sonaive.v2ex.ui.widgets.FlexibleRecyclerView;

import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/18/14.
 */
public class FeedsFragment extends Fragment {

    private static final String TAG = makeLogTag(FeedsFragment.class);

    FlexibleRecyclerView mRecyclerView = null;
    View mEmptyView = null;

    FeedCursorAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new FeedCursorAdapter(getActivity(), null, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list_fragment_layout, container, false);
        mRecyclerView = (FlexibleRecyclerView) root.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mEmptyView = root.findViewById(android.R.id.empty);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initializes the CursorLoader, the loader id must starts from 1, because
        // The BaseActivity already takes 0 loader id.
        getLoaderManager().initLoader(1, null, new FeedLoaderCallback());
    }

    public void setContentTopClearance(int clearance) {
        if (mRecyclerView != null) {
            mRecyclerView.setContentTopClearance(clearance);
        }
    }

    public boolean canRecyclerViewScrollUp() {
        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    class FeedLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), V2exContract.Feeds.CONTENT_URI,
                    PROJECTION, null, null, V2exContract.Feeds.FEED_ID + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ((FeedsActivity) getActivity()).onRefreshingStateChanged(false);
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            loader = null;
        }
    }

    private static final String[] PROJECTION = {
            BaseColumns._ID,
            V2exContract.Feeds.FEED_ID,
            V2exContract.Feeds.FEED_TITLE,
            V2exContract.Feeds.FEED_CONTENT,
            V2exContract.Feeds.FEED_CONTENT_RENDERED,
            V2exContract.Feeds.FEED_MEMBER,
            V2exContract.Feeds.FEED_NODE,
            V2exContract.Feeds.FEED_REPLIES,
            V2exContract.Feeds.FEED_CREATED
    };

}
