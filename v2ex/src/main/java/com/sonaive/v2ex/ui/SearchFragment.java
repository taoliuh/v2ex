package com.sonaive.v2ex.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.io.model.SearchItem;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.ui.adapter.SearchCursorAdapter;
import com.sonaive.v2ex.ui.widgets.FlexibleRecyclerView;
import com.sonaive.v2ex.widget.HeaderViewRecyclerAdapter;
import com.sonaive.v2ex.widget.PaginationCursorAdapter;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;
/**
 * Created by liutao on 1/5/15.
 */
public class SearchFragment extends Fragment {

    private static final String TAG = makeLogTag(SearchFragment.class);
    private static final String ARG_KEYWORD = "keyword";

    FlexibleRecyclerView mRecyclerView = null;
    TextView mEmptyView = null;
    View footer;

    SearchCursorAdapter mAdapter;
    HeaderViewRecyclerAdapter headerAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    Bundle loaderArgs;
    boolean isFooterViewAdded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SearchCursorAdapter(getActivity(), null, 0);
        loaderArgs = new Bundle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list_fragment_layout, container, false);
        footer = inflater.inflate(R.layout.item_footer_clear_history, container, false);
        mRecyclerView = (FlexibleRecyclerView) root.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
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
        getLoaderManager().initLoader(1, null, new SearchLoaderCallback());
    }

    public void setContentTopClearance(final int clearance) {
        if (mRecyclerView != null) {
            mRecyclerView.setContentTopClearance(clearance);
        }
    }

    /**
     * Update one record
     * @param keyword
     */
    public void updateLocalRecords(String keyword) {
        if (keyword == null || keyword.isEmpty()) return;
        String selection = "search_keyword = ?";
        String[] selectionArgs = new String[] {keyword};
        if (getActivity() == null) {
            return;
        }
        Cursor c = getActivity().getContentResolver().query(V2exContract.Search.CONTENT_URI, SearchQuery.PROJECTION, selection, selectionArgs, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing updating solution...");
        SearchItem record = new SearchItem();
        record.keyword = keyword;
        record.updateTime = System.currentTimeMillis();
        if (c.moveToNext()) {
            // update
            getActivity().getContentResolver().update(V2exContract.Search.CONTENT_URI, buildSearchItem(record), selection, selectionArgs);
        } else {
            // insert
            getActivity().getContentResolver().insert(V2exContract.Search.CONTENT_URI, buildSearchItem(record));
        }
    }

    /**
     * Delete all of search history
     */
    private void deleteAll() {
        int affectedRowCount = getActivity().getContentResolver().delete(V2exContract.Search.CONTENT_URI, null, null);
        LOGD(TAG, "delete " + affectedRowCount + " rows!");
    }

    private ContentValues buildSearchItem(SearchItem item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(V2exContract.Search.SEARCH_KEYWORD, item.keyword);
        contentValues.put(V2exContract.Search.SEARCH_UPDATE_TIME, item.updateTime);
        return contentValues;
    }

    class SearchLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = V2exContract.Search.CONTENT_URI.buildUpon().
                    appendQueryParameter(V2exContract.QUERY_PARAMETER_LIMIT, "10").
                    build();
            return new CursorLoader(getActivity(), uri,
                    SearchQuery.PROJECTION, null, null, V2exContract.Search.SEARCH_UPDATE_TIME + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data == null || data.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(getActivity().getString(R.string.no_data));
            } else {
                mEmptyView.setVisibility(View.GONE);
            }
            if (data != null && data.getCount() > 0 && !isFooterViewAdded) {
                isFooterViewAdded = true;
                headerAdapter = new HeaderViewRecyclerAdapter(mAdapter);
                headerAdapter.addFooterView(footer);
                mRecyclerView.setAdapter(headerAdapter);
                footer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteAll();
                        headerAdapter = new HeaderViewRecyclerAdapter(mAdapter);
                        mRecyclerView.setAdapter(headerAdapter);
                    }
                });
            }
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    }

    private interface SearchQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                V2exContract.Search.SEARCH_KEYWORD,
                V2exContract.Search.SEARCH_UPDATE_TIME
        };

        int _ID = 0;
        int SEARCH_KEYWORD = 1;
        int SEARCH_UPDATE_TIME = 2;
    }

}
