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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.SyncHelper;
import com.sonaive.v2ex.sync.api.Api;
import com.sonaive.v2ex.ui.adapter.NodeCursorAdapter;
import com.sonaive.v2ex.ui.widgets.FlexibleRecyclerView;
import com.sonaive.v2ex.ui.widgets.RecyclerItemClickListener;
import com.sonaive.v2ex.widget.LoadingStatus;
import com.sonaive.v2ex.widget.OnLoadMoreDataListener;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/15/14.
 */
public class NodesFragment extends Fragment implements OnLoadMoreDataListener {

    private static final String TAG = makeLogTag(NodesFragment.class);

    /** The handler message for updating the search query. */
    private static final int MESSAGE_QUERY_UPDATE = 1;
    /** The delay before actual requerying in millisecs. */
    private static final int QUERY_UPDATE_DELAY_MILLIS = 100;

    FlexibleRecyclerView mRecyclerView = null;
    TextView mEmptyView = null;

    NodeCursorAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    String keyword;
    Bundle loaderArgs;

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
        mAdapter = new NodeCursorAdapter(getActivity(), null, 0);
        mAdapter.setOnLoadMoreDataListener(this);
        loaderArgs = new Bundle();
        Bundle args = new Bundle();
        args.putString(Api.ARG_API_NAME, Api.API_NODES_ALL);
        SyncHelper.requestManualSync(getActivity(), args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list_fragment_layout, container, false);
        mRecyclerView = (FlexibleRecyclerView) root.findViewById(R.id.recycler_view);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Cursor cursor = mAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String nodeTitle = cursor.getString(cursor.getColumnIndex(V2exContract.Nodes.NODE_TITLE));
                    int nodeId = cursor.getInt(cursor.getColumnIndex(V2exContract.Nodes.NODE_ID));
                    Intent intent = FeedsActivity.getCallingIntent(getActivity(), nodeTitle, nodeId);
                    startActivity(intent);
                }
            }
        }));
        mRecyclerView.setAdapter(mAdapter);
        mEmptyView = (TextView) root.findViewById(android.R.id.empty);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initializes the CursorLoader
        getLoaderManager().initLoader(1, null, new NodesLoaderCallback());
    }

    public void setContentTopClearance(int clearance) {
        if (mRecyclerView != null) {
            mRecyclerView.setContentTopClearance(clearance);
        }
    }

    public boolean canRecyclerViewScrollUp() {
        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    public void requestQueryUpdate(Bundle arguments) {
        mHandler.removeMessages(MESSAGE_QUERY_UPDATE);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_QUERY_UPDATE, arguments),
                QUERY_UPDATE_DELAY_MILLIS);
    }

    public void reloadFromArguments(String keyword) {
        getLoaderManager().restartLoader(1, buildQueryParameter(keyword), new NodesLoaderCallback());
    }

    private Bundle buildQueryParameter(String keyword) {
        loaderArgs.putInt("limit", (mAdapter.getLoadedPage() + 1) * PaginationCursorAdapter.pageSize);
        loaderArgs.putString("keyword", keyword == null ? "" : keyword);
        return loaderArgs;
    }

    @Override
    public void onLoadMoreData() {
        mAdapter.setLoadingState(LoadingStatus.LOADING);
        LOGD(TAG, "Load more nodes, loading state is: " + LoadingStatus.LOADING);
    }

    class NodesLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selectionClause = null;
            if (args != null) {
                String keyword = args.getString("keyword");

                if (keyword != null && !keyword.trim().isEmpty()) {
                    selectionClause = "node_title LIKE '%" + keyword + "%'";
                }
            }
            return new CursorLoader(
                    getActivity(),                                     // Context
                    V2exContract.Nodes.CONTENT_URI,                    // Table to query
                    PROJECTION,                                        // Projection to return
                    selectionClause,                                              // No selection clause
                    null,                                              // No selection arguments
                    V2exContract.Nodes.NODE_ID + " ASC"                // Default sort order
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            LOGD(TAG, "Nodes count is: " + (data == null ? 0 : data.getCount()));
            mAdapter.setLoadingState(LoadingStatus.FINISH);
            if (getActivity() instanceof NodesActivity) {
                ((NodesActivity) getActivity()).onRefreshingStateChanged(false);
            }

            if (data == null || data.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(getActivity().getString(R.string.no_data));
            } else {
                mEmptyView.setVisibility(View.GONE);
            }

            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    }

    private static final String[] PROJECTION = {
            V2exContract.Nodes._ID,
            V2exContract.Nodes.NODE_ID,
            V2exContract.Nodes.NODE_TITLE,
            V2exContract.Nodes.NODE_HEADER,
            V2exContract.Nodes.NODE_TOPICS
    };
}
