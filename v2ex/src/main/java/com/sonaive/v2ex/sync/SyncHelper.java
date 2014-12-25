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

package com.sonaive.v2ex.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.sync.api.Api;
import com.sonaive.v2ex.sync.api.FeedsApi;
import com.sonaive.v2ex.sync.api.NodesApi;
import com.sonaive.v2ex.sync.api.ReviewsApi;
import com.sonaive.v2ex.sync.api.UserIdentityApi;
import com.sonaive.v2ex.util.AccountUtils;

import java.io.IOException;
import java.net.UnknownHostException;

import de.greenrobot.event.EventBus;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.LOGE;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * A helper class for dealing with data synchronization.
 * All operations occur on the thread they're called from, so it's best to wrap
 * calls in an {@link android.os.AsyncTask}, or better yet, a
 * {@link android.app.Service}.
 *
 * Created by liutao on 12/6/14.
 */
public class SyncHelper {
    private static final String TAG = makeLogTag(SyncHelper.class);

    private Context mContext;

    private V2exDataHandler mDataHandler;

    public SyncHelper(Context context) {
        mContext = context;
        mDataHandler = new V2exDataHandler(mContext);
    }

    public static void requestManualSync(Context context, Bundle args) {
        Account account = AccountUtils.getActiveAccount(context);
        if (account != null) {
            LOGD(TAG, "Requesting manual sync for account " + account.name
                    +" args=" + args.toString());

            args.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            args.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            args.putBoolean(SyncAdapter.EXTRA_SYNC_REMOTE, false);

            AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
            accountManager.addAccountExplicitly(account, null, null);

            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, V2exContract.CONTENT_AUTHORITY, true);

            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, V2exContract.CONTENT_AUTHORITY, 1);

            boolean pending = ContentResolver.isSyncPending(account,
                    V2exContract.CONTENT_AUTHORITY);
            if (pending) {
                LOGD(TAG, "Warning: sync is PENDING. Will cancel.");
            }
            boolean active = ContentResolver.isSyncActive(account,
                    V2exContract.CONTENT_AUTHORITY);
            if (active) {
                LOGD(TAG, "Warning: sync is ACTIVE. Will cancel.");
            }

            if (pending || active) {
                LOGD(TAG, "Cancelling previously pending/active sync.");
                ContentResolver.cancelSync(account, V2exContract.CONTENT_AUTHORITY);
            }

            LOGD(TAG, "Requesting sync now.");
            ContentResolver.requestSync(account, V2exContract.CONTENT_AUTHORITY, args);
        } else {
            LOGD(TAG, "Can't request manual sync -- no chosen account.");
        }
    }

    /**
     * Attempts to perform data synchronization.
     *
     * @param syncResult (optional) the sync result object to update with statistics.
     * @param account    the account associated with this sync
     * @return Whether or not the synchronization made any changes to the data.
     */
    public boolean performSync(SyncResult syncResult, Account account, Bundle extras) {

        final boolean remoteSync = extras.getBoolean(SyncAdapter.EXTRA_SYNC_REMOTE, true);

        // remote sync consists of these operations, which we try one by one (and tolerate
        // individual failures on each)
        String[] apisToPerform = remoteSync ?
                new String[] { Api.API_TOPICS_LATEST, Api.API_TOPICS_HOT, Api.API_NODES_ALL} :
                new String[] { extras.getString(Api.ARG_API_NAME)};


        for (String api : apisToPerform) {
            try {
                sync(api, extras);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                EventBus.getDefault().postSticky(new ExceptionEvent(mContext.getString(R.string.err_io)));
                LOGE(TAG, "Error performing remote sync.");
                increaseIoExceptions(syncResult);
            }
        }

        int operations = mDataHandler.getContentProviderOperationsDone();
        if (syncResult != null && syncResult.stats != null) {
            syncResult.stats.numEntries += operations;
            syncResult.stats.numUpdates += operations;
        }

        LOGD(TAG, "SYNC STATS:\n" +
                " *  Account synced: " + (account == null ? "null" : account.name) + "\n" +
                " *  Content provider operations: " + operations);

        return true;
    }

    private void sync(String api, Bundle args) throws IOException {
        if (args == null) {
            return;
        }
        switch (api) {
            case Api.API_MEMBER: {
                UserIdentityApi userIdentityApi = new UserIdentityApi(mContext);
                String accountName = args.getString("accountName");
                userIdentityApi.verifyUserIdentity(accountName);
                break;
            }
            case Api.API_TOPICS_LATEST: {
                FeedsApi latestFeedsApi = new FeedsApi(mContext, FeedsApi.TYPE_LATEST);
                // save the remote data to the database
                mDataHandler.applyData(new Bundle[] {latestFeedsApi.sync(Api.HttpMethod.GET)});
                break;
            }
            case Api.API_TOPICS_HOT: {
                FeedsApi hotFeedsApi = new FeedsApi(mContext, FeedsApi.TYPE_HOT);
                mDataHandler.applyData(new Bundle[] {hotFeedsApi.sync(Api.HttpMethod.GET)});
                break;
            }
            case Api.API_NODES_ALL: {
                NodesApi allNodesApi = new NodesApi(mContext, NodesApi.TYPE_ALL);
                mDataHandler.applyData(new Bundle[] {allNodesApi.sync(Api.HttpMethod.GET)});
                break;
            }
            case Api.API_NODES_SPECIFIC: {
                NodesApi specificNodesApi = new NodesApi(mContext, NodesApi.TYPE_SPECIFIC, args);
                mDataHandler.applyData(new Bundle[] {specificNodesApi.sync(Api.HttpMethod.GET)});
                break;
            }
            case Api.API_REVIEWS: {
                ReviewsApi reviewsApi = new ReviewsApi(mContext, args);
                mDataHandler.applyData(new Bundle[] {reviewsApi.sync(Api.HttpMethod.GET)});
                break;
            }
        }

    }

    // Returns whether we are connected to the internet.
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void increaseIoExceptions(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numIoExceptions;
        }
    }

    private void increaseSuccesses(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numEntries;
            ++syncResult.stats.numUpdates;
        }
    }
}
