/*
 * Copyright 2014 so_naive. All rights reserved.
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
package com.sonaive.v2ex.ui.debug;

import android.os.Bundle;
import android.view.View;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.ui.BaseActivity;
import com.sonaive.v2ex.ui.widgets.CollectionView;
import com.sonaive.v2ex.ui.widgets.DrawShadowFrameLayout;
import com.sonaive.v2ex.util.UIUtils;

import static com.sonaive.v2ex.util.LogUtils.makeLogTag;
/**
 * Created by liutao on 12/1/14.
 */
public class NavDrawerTestActivity extends BaseActivity {

    private static final String TAG = makeLogTag(NavDrawerTestActivity.class);
    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mButterBar;
    private NavDrawerTestFragment mFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browse_images);
        mButterBar = findViewById(R.id.butter_bar);
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);

        registerHideableHeaderView(findViewById(R.id.headerbar));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        enableActionBarAutoHide((CollectionView) findViewById(R.id.images_collection_view));
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();

        mFrag = (NavDrawerTestFragment) getFragmentManager().findFragmentById(R.id.images_fragment);
        if (mFrag != null) {
            // configure images fragment's top clearance to take our overlaid controls (Action Bar
            // ) into account.
            int actionBarSize = UIUtils.calculateActionBarSize(this);
            mDrawShadowFrameLayout.setShadowTopOffset(actionBarSize);
            mFrag.setContentTopClearance(actionBarSize
                    + getResources().getDimensionPixelSize(R.dimen.explore_grid_padding));
        }
        updateFragContentTopClearance();

    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        if (mFrag != null) {
            return mFrag.canCollectionViewScrollUp();
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
        return NAVDRAWER_ITEM_SETTINGS;
    }

    // Updates the Sessions fragment content top clearance to take our chrome into account
    private void updateFragContentTopClearance() {
        mFrag = (NavDrawerTestFragment) getFragmentManager().findFragmentById(
                R.id.images_fragment);
        if (mFrag == null) {
            return;
        }


        final boolean butterBarVisible = mButterBar != null
                && mButterBar.getVisibility() == View.VISIBLE;

        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        int butterBarClearance = butterBarVisible
                ? getResources().getDimensionPixelSize(R.dimen.butter_bar_height) : 0;
        int gridPadding = getResources().getDimensionPixelSize(R.dimen.explore_grid_padding);

        setProgressBarTopWhenActionBarShown(actionBarClearance + butterBarClearance);
        mDrawShadowFrameLayout.setShadowTopOffset(actionBarClearance + butterBarClearance);
        mFrag.setContentTopClearance(actionBarClearance + butterBarClearance + gridPadding);
    }
}
