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
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.io.model.Feed;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.ui.adapter.FeedCursorAdapter;
import com.sonaive.v2ex.ui.widgets.FlexibleRecyclerView;
import com.sonaive.v2ex.ui.widgets.RecyclerItemClickListener;
import com.sonaive.v2ex.util.ModelUtils;
import com.sonaive.v2ex.util.UIUtils;
import com.sonaive.v2ex.widget.LoadingState;
import com.sonaive.v2ex.widget.OnLoadMoreDataListener;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/18/14.
 */
public class FeedsFragment extends Fragment implements OnLoadMoreDataListener {

    private static final String TAG = makeLogTag(FeedsFragment.class);

    FlexibleRecyclerView mRecyclerView = null;
    TextView mEmptyView = null;

    FeedCursorAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    Bundle loaderArgs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new FeedCursorAdapter(getActivity(), null, 0);
        mAdapter.setOnLoadMoreDataListener(this);
        loaderArgs = new Bundle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list_fragment_layout, container, false);
        mRecyclerView = (FlexibleRecyclerView) root.findViewById(R.id.recycler_view);
        if (UIUtils.isTablet(getActivity())) {
            mLayoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.feeds_columns), StaggeredGridLayoutManager.VERTICAL);
        } else {
            mLayoutManager = new LinearLayoutManager(getActivity());
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Cursor cursor = mAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
                    Bundle bundle = new Bundle();
                    Parcelable feed = cursor2Parcelable(cursor);
                    bundle.putParcelable("feed", feed);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }
            }
        }));
        mEmptyView = (TextView) root.findViewById(android.R.id.empty);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initializes the CursorLoader, the loader id must starts from 1, because
        // The BaseActivity already takes 0 loader id.
        getLoaderManager().initLoader(1, buildQueryParameter(), new FeedLoaderCallback());
    }

    public void setContentTopClearance(final int clearance, final boolean isActionbarShown) {
        if (mRecyclerView != null) {
            mRecyclerView.setContentTopClearance(clearance);
            // In case butter bar shows, the recycler view should scroll down.
            if (isActionbarShown) {
                RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    int index = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    LOGD(TAG, "LinearLayoutManager, first complete visible item position is: " + index);
                    if (index == 0) {
                        mRecyclerView.smoothScrollBy(0, -clearance);
                    }
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    int[] findFirstVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
                    LOGD(TAG, "StaggeredGridLayoutManager, first complete visible item position is: " + findFirstVisibleItemPositions[0]);
                    if (findFirstVisibleItemPositions[0] == 0) {
                        mRecyclerView.smoothScrollBy(0, -clearance);
                    }
                }
            }
        }
    }

    public boolean canRecyclerViewScrollUp() {
        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    @Override
    public void onLoadMoreData() {
        mAdapter.setLoadingState(LoadingState.LOADING);

        ((FeedsActivity) getActivity()).updateSwipeRefreshProgressbarTopClearence();
        ((BaseActivity) getActivity()).onRefreshingStateChanged(true);

        getLoaderManager().restartLoader(1, buildQueryParameter(), new FeedLoaderCallback());

        LOGD(TAG, "Load more nodes, loading state is: " + LoadingState.LOADING + ", preparing to load page " + mAdapter.getLoadedPage());
    }

    class FeedLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            int limit = args.getInt("limit");
            int offset = args.getInt("offset");
            LOGD(TAG, "Feeds limit is: " + limit + ", offset is: " + offset);
            Uri uri = V2exContract.Feeds.CONTENT_URI.buildUpon().
                    appendQueryParameter(V2exContract.QUERY_PARAMETER_LIMIT, String.valueOf(limit)).
                    build();
            return new CursorLoader(getActivity(), uri,
                    FeedsQuery.PROJECTION, null, null, V2exContract.Feeds.FEED_ID + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            getActivity().invalidateOptionsMenu();

            if (data == null || data.getCount() % PaginationCursorAdapter.pageSize > 0) {

                mAdapter.setLoadingState(LoadingState.NO_MORE_DATA);
                LOGD(TAG, "Feeds count is: " + (data == null ? 0 : data.getCount()) + ", loading state is: " + LoadingState.NO_MORE_DATA);
            } else {
                mAdapter.setLoadingState(LoadingState.FINISH);
                LOGD(TAG, "Feeds count is: " + (data.getCount()) + ", loading state is: " + LoadingState.FINISH);
            }

            if (data == null || data.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(getActivity().getString(R.string.no_data));
            } else {
                mEmptyView.setVisibility(View.GONE);
            }

            ((FeedsActivity) getActivity()).onRefreshingStateChanged(false);
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            loader = null;
        }
    }

    private Bundle buildQueryParameter() {
        loaderArgs.putInt("limit", (mAdapter.getLoadedPage() + 1) * PaginationCursorAdapter.pageSize);
        return loaderArgs;
    }

    private Parcelable cursor2Parcelable(Cursor cursor) {
        int feedId = cursor.getInt(FeedsQuery.FEED_ID);
        String feedTitle = cursor.getString(FeedsQuery.FEED_TITLE);
        String feedContent = cursor.getString(FeedsQuery.FEED_CONTENT);
        String feedContentRendered = cursor.getString(FeedsQuery.FEED_CONTENT_RENDERED);
        String feedMember = cursor.getString(FeedsQuery.FEED_MEMBER);
        String feedNode = cursor.getString(FeedsQuery.FEED_NODE);
        int feedReplies = cursor.getInt(FeedsQuery.FEED_REPLIES);
        long feedCreated = cursor.getLong(FeedsQuery.FEED_CREATED);

        Feed feed = new Feed();
        feed.id = feedId;
        feed.title = feedTitle;
        feed.content = feedContent;
        feed.content_rendered = feedContentRendered;
        feed.member = ModelUtils.getAuthor(feedMember);
        feed.node = ModelUtils.getNode(feedNode);
        feed.replies = feedReplies;
        feed.created = feedCreated;

        return feed;
    }

    private interface FeedsQuery {
        String[] PROJECTION = {
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

        int _ID = 0;
        int FEED_ID = 1;
        int FEED_TITLE = 2;
        int FEED_CONTENT = 3;
        int FEED_CONTENT_RENDERED = 4;
        int FEED_MEMBER = 5;
        int FEED_NODE = 6;
        int FEED_REPLIES = 7;
        int FEED_CREATED = 8;
    }
}