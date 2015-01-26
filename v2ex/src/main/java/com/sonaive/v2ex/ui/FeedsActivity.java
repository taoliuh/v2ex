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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.sonaive.v2ex.R;
import com.sonaive.v2ex.sync.ExceptionEvent;
import com.sonaive.v2ex.sync.SyncHelper;
import com.sonaive.v2ex.sync.api.Api;
import com.sonaive.v2ex.ui.event.SimpleEvent;
import com.sonaive.v2ex.ui.widgets.DrawShadowFrameLayout;
import com.sonaive.v2ex.util.UIUtils;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/18/14.
 */
public class FeedsActivity extends BaseActivity {
    private static final String TAG = makeLogTag(FeedsActivity.class);
    private static final String ARG_NODE_ID = "node_id";
    private static final String ARG_NODE_TITLE = "node_title";

    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mButterBar;
    private FeedsFragment mFrag;
    private int nodeId;
    private String nodeTitle;

    public static Intent getCallingIntent(Context context, String nodeTitle, int nodeId) {
        Intent intent = new Intent(context, FeedsActivity.class);
        intent.putExtra(ARG_NODE_TITLE, nodeTitle);
        intent.putExtra(ARG_NODE_ID, nodeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);
        if (savedInstanceState != null) {
            nodeId = savedInstanceState.getInt(ARG_NODE_ID);
            nodeTitle = savedInstanceState.getString(ARG_NODE_TITLE);
        } else {
            if (getIntent() != null) {
                nodeId = getIntent().getIntExtra(ARG_NODE_ID, -1);
                nodeTitle = getIntent().getStringExtra(ARG_NODE_TITLE);
            } else {
                nodeId = -1;
                nodeTitle = "";
            }
        }
        EventBus.getDefault().postSticky(new SimpleEvent(String.valueOf(nodeTitle == null ? "" : nodeTitle)));
        mButterBar = findViewById(R.id.butter_bar);
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        overridePendingTransition(0, 0);
        registerHideableHeaderView(findViewById(R.id.headerbar));
        registerHideableHeaderView(mButterBar);

        syncResource();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        enableActionBarAutoHide((RecyclerView) findViewById(R.id.recycler_view));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_NODE_ID, nodeId);
        outState.putString(ARG_NODE_TITLE, nodeTitle);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.feeds, menu);

        MenuItem refreshMenu = menu.findItem(R.id.menu_refresh);
        if (isRefreshing()) {
            refreshMenu.setVisible(true);
            refreshMenu.setActionView(R.layout.progress_bar);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            Intent intent = SearchActivity.getCallingIntent(this, SearchActivity.EXTRA_SEARCH_FEEDS);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        invalidateOptionsMenu();
        if (!checkShowNoNetworkButterBar()) {
            syncResource();
        } else {
            onRefreshingStateChanged(false);
        }
    }

    @Override
    protected void onNetworkChange() {
        checkShowNoNetworkButterBar();
    }

    public void syncResource() {
        HashMap<String, String> params = new HashMap<>();
        Bundle args = new Bundle();

        if (nodeId != -1) {
            args.putString(Api.ARG_API_NAME, Api.API_TOPICS_SPECIFIC);
            params.put("node_id", String.valueOf(nodeId));
            args.putString(Api.ARG_API_PARAMS, new Gson().toJson(params));
        } else {
            args.putString(Api.ARG_API_NAME, Api.API_TOPICS_LATEST);
        }
        SyncHelper.requestManualSync(this, args);
    }

    public void onEventMainThread(ExceptionEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        showExceptionButterBar(event);
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
        mFrag.setContentTopClearance(actionBarClearance + butterBarClearance, isActionBarShown());
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

    private void showExceptionButterBar(ExceptionEvent event) {
        UIUtils.setUpButterBar(mButterBar, event.errMessage,
                getString(R.string.close), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mButterBar.setVisibility(View.GONE);
                        updateFragContentTopClearance();
                    }
                }
        );
        invalidateOptionsMenu();
        onRefreshingStateChanged(false);
        updateFragContentTopClearance();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mButterBar.getVisibility() == View.VISIBLE) {
                    mButterBar.setVisibility(View.GONE);
                    updateFragContentTopClearance();
                }
            }
        }, 3000);
    }
}
