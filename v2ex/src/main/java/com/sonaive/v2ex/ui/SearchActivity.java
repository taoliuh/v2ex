package com.sonaive.v2ex.ui;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.ui.widgets.DrawShadowFrameLayout;
import com.sonaive.v2ex.ui.widgets.OnQueryListener;
import com.sonaive.v2ex.ui.widgets.SimpleSearchView;
import com.sonaive.v2ex.util.UIUtils;

import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 1/1/15.
 */
public class SearchActivity extends BaseActivity implements OnQueryListener {
    private static final String TAG = makeLogTag(SearchActivity.class);
    public static final int EXTRA_SEARCH_FEEDS = 1;
    public static final int EXTRA_SEARCH_NODES = 2;
    private static final String ARG_ACTION_SEARCH = "action_search";
    private static final String ARG_SHOW_SEARCH_FRG = "show_search_frg";

    /** The handler message for updating the search query. */
    private static final int MESSAGE_QUERY_UPDATE = 1;
    /** The delay before actual requerying in millisecs. */
    private static final int QUERY_UPDATE_DELAY_MILLIS = 2000;

    FragmentManager fm;
    FeedsFragment mFeedsFragment = null;
    NodesFragment mNodesFragment = null;
    SearchFragment mSearchFragment = null;
    SimpleSearchView searchView;

    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private FrameLayout feedsFrgContainer;
    private FrameLayout nodesFrgContainer;
    private FrameLayout searchFrgContainer;
    private View mButterBar;
    private boolean isShowSearchFragment = true;
    private int actionSearch;
    String mQuery = "";


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_QUERY_UPDATE) {
                Bundle args = (Bundle) msg.obj;
                if (args != null) {
                    String keyword = args.getString("keyword");
                    if (mSearchFragment == null) {
                        mSearchFragment = (SearchFragment) getFragmentManager().findFragmentByTag("search_fragment");
                    }
                    mSearchFragment.updateLocalRecords(keyword);
                }
            }
        }
    };

    public static Intent getCallingIntent(Context context, int extra) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(ARG_ACTION_SEARCH, extra);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState != null) {
            actionSearch = savedInstanceState.getInt(ARG_ACTION_SEARCH);
            isShowSearchFragment = savedInstanceState.getBoolean(ARG_SHOW_SEARCH_FRG);
        } else {
            actionSearch = getIntent().getIntExtra(ARG_ACTION_SEARCH, 0);
        }

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_up_grey);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        feedsFrgContainer = (FrameLayout) findViewById(R.id.feeds_fragment_container);
        nodesFrgContainer = (FrameLayout) findViewById(R.id.nodes_fragment_container);
        searchFrgContainer = (FrameLayout) findViewById(R.id.search_fragment_container);
        mButterBar = findViewById(R.id.butter_bar);
        feedsFrgContainer.setVisibility(View.INVISIBLE);
        nodesFrgContainer.setVisibility(View.INVISIBLE);
        searchFrgContainer.setVisibility(View.VISIBLE);

        fm = getFragmentManager();

        String query = getIntent().getStringExtra(SearchManager.QUERY);
        query = query == null ? "" : query;
        mQuery = query;

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_ACTION_SEARCH, actionSearch);
        outState.putBoolean(ARG_SHOW_SEARCH_FRG, isShowSearchFragment);
    }

    @Override
    protected Toolbar getActionBarToolbar() {
        Toolbar actionBarToolbar = super.getActionBarToolbar();
        searchView = (SimpleSearchView) actionBarToolbar.findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryListener(this);
            if (actionSearch == EXTRA_SEARCH_FEEDS) {
                searchView.setHint(R.string.hint_search_title);
            } else {
                searchView.setHint(R.string.hint_search_nodes);
            }
        }
        return actionBarToolbar;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFeedsFragment = (FeedsFragment) getFragmentManager().findFragmentById(R.id.feeds_fragment);
        mNodesFragment = (NodesFragment) getFragmentManager().findFragmentById(R.id.nodes_fragment);
        mSearchFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
        checkShowNoNetworkButterBar();
        updateFragContentTopClearance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNetworkChange() {
        checkShowNoNetworkButterBar();
    }

    // Updates the Feeds fragment content top clearance to take our chrome into account
    private void updateFragContentTopClearance() {
        if (mSearchFragment == null || mFeedsFragment == null || mNodesFragment == null) {
            return;
        }
        final boolean butterBarVisible = mButterBar != null
                && mButterBar.getVisibility() == View.VISIBLE;
        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        int butterBarClearance = butterBarVisible
                ? getResources().getDimensionPixelSize(R.dimen.butter_bar_height) : 0;
        mDrawShadowFrameLayout.setShadowTopOffset(actionBarClearance + butterBarClearance);
        mFeedsFragment.setContentTopClearance(actionBarClearance + butterBarClearance, isActionBarShown());
        mSearchFragment.setContentTopClearance(actionBarClearance + butterBarClearance);
        mNodesFragment.setContentTopClearance(actionBarClearance + butterBarClearance);
    }

    private void onQuery(final String s) {

        if (s.trim().isEmpty()) {
            if (actionSearch == EXTRA_SEARCH_FEEDS) {
                fm.beginTransaction().show(mSearchFragment).hide(mFeedsFragment).commit();
                feedsFrgContainer.setVisibility(View.INVISIBLE);
            } else if (actionSearch == EXTRA_SEARCH_NODES) {
                fm.beginTransaction().show(mSearchFragment).hide(mNodesFragment).commit();
                nodesFrgContainer.setVisibility(View.INVISIBLE);
            }
            searchFrgContainer.setVisibility(View.VISIBLE);
            isShowSearchFragment = true;
        } else {
            Bundle args = new Bundle();
            args.putString("keyword", s);
            args.putInt("menu", ITEM_SEARCH);
            requestQueryUpdate(args);
            if (actionSearch == EXTRA_SEARCH_FEEDS) {
                fm.beginTransaction().show(mFeedsFragment).hide(mSearchFragment).commit();
                feedsFrgContainer.setVisibility(View.VISIBLE);
                if (null != mFeedsFragment) {
                    mFeedsFragment.requestQueryUpdate(args);
                }
            } else if (actionSearch == EXTRA_SEARCH_NODES) {
                fm.beginTransaction().show(mNodesFragment).hide(mSearchFragment).commit();
                nodesFrgContainer.setVisibility(View.VISIBLE);
                if (null != mNodesFragment) {
                    mNodesFragment.requestQueryUpdate(args);
                }
            }
            searchFrgContainer.setVisibility(View.INVISIBLE);
            isShowSearchFragment = false;
        }
        updateFragContentTopClearance();
    }

    void requestQueryUpdate(Bundle arguments) {
        mHandler.removeMessages(MESSAGE_QUERY_UPDATE);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_QUERY_UPDATE, arguments),
                QUERY_UPDATE_DELAY_MILLIS);
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
            return true;
        } else {
            if (mButterBar.getVisibility() == View.VISIBLE) {
                mButterBar.setVisibility(View.GONE);
                updateFragContentTopClearance();
            }
            return false;
        }
    }

    @Override
    public void onSubmit(String s) {
        onQuery(s);
    }

    @Override
    public void onClear() {
        onQuery("");
    }

    @Override
    public void onQueryTextChange(String s) {
        onQuery(s);
    }

    public void onEvent(String keyword) {
        if (searchView != null) {
            searchView.setText(keyword);
        }
    }
}
