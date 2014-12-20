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

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.sync.SyncHelper;
import com.sonaive.v2ex.sync.api.Api;
import com.sonaive.v2ex.ui.widgets.DrawShadowFrameLayout;
import com.sonaive.v2ex.util.UIUtils;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/18/14.
 */
public class FeedsActivity extends BaseActivity {
    private static final String TAG = makeLogTag(FeedsActivity.class);
    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mButterBar;
    private FeedsFragment mFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);
        mButterBar = findViewById(R.id.butter_bar);
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        overridePendingTransition(0, 0);
        registerHideableHeaderView(findViewById(R.id.headerbar));
        registerHideableHeaderView(mButterBar);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        enableActionBarAutoHide((RecyclerView) findViewById(R.id.recycler_view));
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();

        mFrag = (FeedsFragment) getFragmentManager().findFragmentById(R.id.feeds_fragment);
        checkShowNoNetworkButterBar();
        updateFragContentTopClearance();
        enableDisableSwipeRefresh(true);
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        if (mFrag != null) {
            return mFrag.canRecyclerViewScrollUp();
        }
        return super.canSwipeRefreshChildScrollUp();
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_FEEDS;
    }

    @Override
    protected void requestDataRefresh() {
        super.requestDataRefresh();
        if (!checkShowNoNetworkButterBar()) {
            Bundle args = new Bundle();
            args.putString(Api.ARG_API_NAME, Api.API_TOPICS_LATEST);
            SyncHelper.requestManualSync(this, args);
        } else {
            onRefreshingStateChanged(false);
        }

    }

    @Override
    protected void onNetworkChange() {
        checkShowNoNetworkButterBar();
    }

    public void updateSwipeRefreshProgressbarTopClearence() {
        final boolean butterBarVisible = mButterBar != null
                && mButterBar.getVisibility() == View.VISIBLE;

        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        int butterBarClearance = butterBarVisible
                ? getResources().getDimensionPixelSize(R.dimen.butter_bar_height) : 0;

        setProgressBarTopWhenActionBarShown(actionBarClearance + butterBarClearance);
    }

    // Updates the Feeds fragment content top clearance to take our chrome into account
    private void updateFragContentTopClearance() {
        mFrag = (FeedsFragment) getFragmentManager().findFragmentById(
                R.id.feeds_fragment);
        if (mFrag == null) {
            return;
        }

        final boolean butterBarVisible = mButterBar != null
                && mButterBar.getVisibility() == View.VISIBLE;

        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        int butterBarClearance = butterBarVisible
                ? getResources().getDimensionPixelSize(R.dimen.butter_bar_height) : 0;

        setProgressBarTopWhenActionBarShown(actionBarClearance + butterBarClearance);
        mDrawShadowFrameLayout.setShadowTopOffset(actionBarClearance + butterBarClearance);
        mFrag.setContentTopClearance(actionBarClearance + butterBarClearance);
    }

    private boolean checkShowNoNetworkButterBar() {

        if (!isOnline()) {
            UIUtils.setUpButterBar(mButterBar, getString(R.string.error_network_unavailable),
                    getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mButterBar.setVisibility(View.GONE);
                            updateFragContentTopClearance();
                        }
                    }
            );
            updateFragContentTopClearance();
            return true;
        } else {
            if (mButterBar.getVisibility() == View.VISIBLE) {
                mButterBar.setVisibility(View.GONE);
                updateFragContentTopClearance();
            }
            return false;
        }
    }
}
