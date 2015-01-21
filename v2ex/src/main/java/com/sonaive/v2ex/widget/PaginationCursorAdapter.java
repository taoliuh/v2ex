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
package com.sonaive.v2ex.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import com.sonaive.v2ex.Config;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/19/14.
 */
public abstract class PaginationCursorAdapter<VH extends RecyclerView.ViewHolder> extends CursorAdapter<VH> {

    private static final String TAG = makeLogTag(PaginationCursorAdapter.class);

    /** The page size of data set. */
    public static final int pageSize = Config.PAGE_SIZE;

    private static final int THRESHOLD = 1;

    /** Current page index already loaded. */
    private int mLoadedPage = 0;

    /** Indicates the state of the loading. */
    private boolean mIsLoading;

    /** Indicates whether we should load data or not, since we may loaded all the data. */
    private boolean moreDataToLoad = true;

    /** When scrolls to the bottom of view(nearly), it's time to load more items. */
    private OnLoadMoreDataListener mOnLoadMoreDataListener;

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter;
     *                Currently it accept {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public PaginationCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        super.onBindViewHolder(holder, position);
        if (position == getItemCount() - THRESHOLD) {

            if (!mIsLoading && moreDataToLoad) {
                if (mOnLoadMoreDataListener != null) {
                    mOnLoadMoreDataListener.onLoadMoreData();
                    LOGD(TAG, "Loading more data, loadedPage is: " + mLoadedPage);
                }
            }

        }
    }

    public void setOnLoadMoreDataListener(OnLoadMoreDataListener listener) {
        mOnLoadMoreDataListener = listener;
    }

    public void setLoadingState(LoadingState loadingState) {
        if (loadingState == LoadingState.FINISH) {
            setLoading(false);
            setMoreDataToLoad(true);
            incrementPageIndex();

        } else if (loadingState == LoadingState.NO_MORE_DATA) {
            setLoading(false);
            setMoreDataToLoad(false);
            incrementPageIndex();
        } else {
            setLoading(true);
            setMoreDataToLoad(false);
        }
    }

    public LoadingState getLoadingState() {
        if (!mIsLoading && moreDataToLoad) {
            return LoadingState.FINISH;
        } else if (!mIsLoading) {
            return LoadingState.NO_MORE_DATA;
        } else {
            return LoadingState.LOADING;
        }
    }

    public int getLoadedPage() {
        return mLoadedPage;
    }

    private void incrementPageIndex() {
        mLoadedPage++;
    }

    private void setLoading(boolean loading) {
        mIsLoading = loading;
    }

    private void setMoreDataToLoad(boolean moreDataToLoad) {
        this.moreDataToLoad = moreDataToLoad;
    }

}
