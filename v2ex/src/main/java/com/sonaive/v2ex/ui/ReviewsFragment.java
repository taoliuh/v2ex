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
import android.provider.BaseColumns;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.io.model.Feed;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.SyncHelper;
import com.sonaive.v2ex.sync.api.Api;
import com.sonaive.v2ex.ui.adapter.ReviewCursorAdapter;
import com.sonaive.v2ex.ui.widgets.FlexibleRecyclerView;
import com.sonaive.v2ex.widget.LoadingState;
import com.sonaive.v2ex.widget.OnLoadMoreDataListener;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/21/14.
 */
public class ReviewsFragment extends Fragment implements OnLoadMoreDataListener {

    private static final String TAG = makeLogTag(ReviewsFragment.class);

    FlexibleRecyclerView mRecyclerView = null;
    TextView mEmptyView = null;

    ReviewCursorAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    Bundle loaderArgs;

    Feed mFeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ReviewCursorAdapter(getActivity(), null, 0);
        mAdapter.setOnLoadMoreDataListener(this);
        loaderArgs = new Bundle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list_fragment_layout, container, false);
        View header = inflater.inflate(R.layout.item_feed, container, false);
        Bundle args = getArguments();
        mFeed = args.getParcelable("feed");
        LOGD(TAG, "feed_id: " + mFeed.id + ", feed_member: " + mFeed.member.username + ", feed_node: " + mFeed.node.title);
        mRecyclerView = (FlexibleRecyclerView) root.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mEmptyView = (TextView) root.findViewById(android.R.id.empty);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!((FeedDetailActivity) getActivity()).checkShowNoNetworkButterBar()) {
            Bundle args = new Bundle();
            args.putString(Api.ARG_API_NAME, Api.API_REVIEWS);
            args.putInt(Api.ARG_API_PARAMS_ID, mFeed.id);
            SyncHelper.requestManualSync(getActivity(), args);
            // Set progress bar refreshing.
            ((FeedDetailActivity) getActivity()).updateSwipeRefreshProgressbarTopClearence();
            ((BaseActivity) getActivity()).onRefreshingStateChanged(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initializes the CursorLoader, the loader id must starts from 1, because
        // The BaseActivity already takes 0 loader id.
        getLoaderManager().initLoader(1, buildQueryParameter(), new ReviewsLoaderCallback());
    }

    public void setContentTopClearance(final int clearance, final boolean isActionbarShown) {
        if (mRecyclerView != null) {
            mRecyclerView.setContentTopClearance(clearance);
            // In case butter bar shows, the recycler view should scroll down.
            if (isActionbarShown) {
                mRecyclerView.smoothScrollBy(0, -clearance);
            }
        }
    }

    public boolean canRecyclerViewScrollUp() {
        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    @Override
    public void onLoadMoreData() {
        mAdapter.setLoadingState(LoadingState.LOADING);

        ((FeedDetailActivity) getActivity()).updateSwipeRefreshProgressbarTopClearence();
        ((BaseActivity) getActivity()).onRefreshingStateChanged(true);

        getLoaderManager().restartLoader(1, buildQueryParameter(), new ReviewsLoaderCallback());

        LOGD(TAG, "Load more reviews, loading state is: " + LoadingState.LOADING + ", preparing to load page " + mAdapter.getLoadedPage());
    }

    private void initHeader() {

    }

    class ReviewsLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            int limit = args.getInt("limit");
            int offset = args.getInt("offset");
            LOGD(TAG, "Reviews limit is: " + limit + ", offset is: " + offset);
            Uri uri = V2exContract.Reviews.buildReviewTopicUri(String.valueOf(mFeed.id)).buildUpon().
                    appendQueryParameter(V2exContract.QUERY_PARAMETER_LIMIT, String.valueOf(limit)).
                    build();
            return new CursorLoader(getActivity(), uri,
                    PROJECTION, null, null, V2exContract.Reviews.REVIEW_CREATED + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            getActivity().invalidateOptionsMenu();

            if (data == null || data.getCount() % PaginationCursorAdapter.pageSize > 0) {

                mAdapter.setLoadingState(LoadingState.NO_MORE_DATA);
                LOGD(TAG, "Reviews count is: " + (data == null ? 0 : data.getCount()) + ", loading state is: " + LoadingState.NO_MORE_DATA);
            } else {
                mAdapter.setLoadingState(LoadingState.FINISH);
                LOGD(TAG, "Reviews count is: " + (data.getCount()) + ", loading state is: " + LoadingState.FINISH);
            }

            if (data == null || data.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(getActivity().getString(R.string.no_data));
            } else {
                mEmptyView.setVisibility(View.GONE);
            }

            ((FeedDetailActivity) getActivity()).onRefreshingStateChanged(false);
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

    private static final String[] PROJECTION = {
            BaseColumns._ID,
            V2exContract.Reviews.REVIEW_MEMBER,
            V2exContract.Reviews.REVIEW_CONTENT,
            V2exContract.Reviews.REVIEW_CREATED,
    };
}
