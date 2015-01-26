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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.ui.adapter.FeedCursorAdapter;
import com.sonaive.v2ex.ui.event.SimpleEvent;
import com.sonaive.v2ex.ui.widgets.FlexibleRecyclerView;
import com.sonaive.v2ex.util.UIUtils;
import com.sonaive.v2ex.widget.LoadingStatus;
import com.sonaive.v2ex.widget.OnLoadMoreDataListener;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

import de.greenrobot.event.EventBus;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/18/14.
 */
public class FeedsFragment extends Fragment implements OnLoadMoreDataListener {

    private static final String TAG = makeLogTag(FeedsFragment.class);
    private static final String ARG_KEYWORD = "keyword";

    /** The handler message for updating the search query. */
    private static final int MESSAGE_QUERY_UPDATE = 1;
    /** The delay before actual requerying in millisecs. */
    private static final int QUERY_UPDATE_DELAY_MILLIS = 100;

    FlexibleRecyclerView mRecyclerView = null;
    TextView mEmptyView = null;

    FeedCursorAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    Bundle loaderArgs;
    String keyword;
    String nodeTitle = "";

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_QUERY_UPDATE) {
                Bundle args = (Bundle) msg.obj;
                keyword = args.getString("keyword");
                reloadFromArguments(keyword);
            }
        }
    };

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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(SimpleEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        nodeTitle = event.eventMessage;
        getLoaderManager().restartLoader(1, buildQueryParameter(), new FeedLoaderCallback());
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
                    // Just need to annotate these lines since it will crash on nexus 7.
//                    int[] findFirstVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
//                    LOGD(TAG, "StaggeredGridLayoutManager, first complete visible item position is: " + findFirstVisibleItemPositions[0]);
//                    if (findFirstVisibleItemPositions[0] == 0) {
//                        mRecyclerView.smoothScrollBy(0, -clearance);
//                    }
                }
            }
        }
    }

    public void reloadFromArguments(String keyword) {
        getLoaderManager().restartLoader(1, buildQueryParameter(keyword), new FeedLoaderCallback());
    }

    public void requestQueryUpdate(Bundle arguments) {
        mHandler.removeMessages(MESSAGE_QUERY_UPDATE);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_QUERY_UPDATE, arguments),
                QUERY_UPDATE_DELAY_MILLIS);
    }

    public boolean canRecyclerViewScrollUp() {
        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    @Override
    public void onLoadMoreData() {
        mAdapter.setLoadingState(LoadingStatus.LOADING);

        ((FeedsActivity) getActivity()).updateSwipeRefreshProgressbarTopClearence();
        ((BaseActivity) getActivity()).onRefreshingStateChanged(true);

        getLoaderManager().restartLoader(1, buildQueryParameter(), new FeedLoaderCallback());

        LOGD(TAG, "Load more nodes, loading state is: " + LoadingStatus.LOADING + ", preparing to load page " + mAdapter.getLoadedPage());
    }

    class FeedLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            int limit = args.getInt("limit");
            int offset = args.getInt("offset");
            String keyword = args.getString("keyword");
            LOGD(TAG, "Feeds limit is: " + limit + ", offset is: " + offset + ", keyword is: " + keyword);
            Uri uri = V2exContract.Feeds.CONTENT_URI.buildUpon().
                    appendQueryParameter(V2exContract.QUERY_PARAMETER_LIMIT, String.valueOf(limit)).
                    build();
            String selectionClause = null;
            if (keyword != null && !keyword.trim().isEmpty()) {
                selectionClause = "feed_title LIKE '%" + keyword + "%'";
            } else if (!nodeTitle.isEmpty()) {
                selectionClause = "feed_node LIKE '%" + nodeTitle + "%'";
            }
            return new CursorLoader(getActivity(), uri,
                    FeedsQuery.PROJECTION, selectionClause, null, V2exContract.Feeds.FEED_LAST_MODIFIED + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            getActivity().invalidateOptionsMenu();

            if (data == null || data.getCount() % PaginationCursorAdapter.pageSize > 0) {

                mAdapter.setLoadingState(LoadingStatus.NO_MORE_DATA);
                LOGD(TAG, "Feeds count is: " + (data == null ? 0 : data.getCount()) + ", loading state is: " + LoadingStatus.NO_MORE_DATA);
            } else {
                mAdapter.setLoadingState(LoadingStatus.FINISH);
                LOGD(TAG, "Feeds count is: " + (data.getCount()) + ", loading state is: " + LoadingStatus.FINISH);
            }

            if (data == null || data.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(getActivity().getString(R.string.no_data));
            } else {
                mEmptyView.setVisibility(View.GONE);
            }

            if (getActivity() instanceof FeedsActivity) {
                ((FeedsActivity) getActivity()).onRefreshingStateChanged(false);
            }
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    }

    private Bundle buildQueryParameter() {
        loaderArgs.putInt("limit", (mAdapter.getLoadedPage() + 1) * PaginationCursorAdapter.pageSize);
        return loaderArgs;
    }

    private Bundle buildQueryParameter(String keyword) {
        loaderArgs.putInt("limit", (mAdapter.getLoadedPage() + 1) * PaginationCursorAdapter.pageSize);
        loaderArgs.putString("keyword", keyword == null ? "" : keyword);
        return loaderArgs;
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
                V2exContract.Feeds.FEED_LAST_MODIFIED
        };

        int _ID = 0;
    }
}