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

package com.sonaive.v2ex.sync;

import android.accounts.Account;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;

import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * A helper class for dealing with conference data synchronization.
 * All operations occur on the thread they're called from, so it's best to wrap
 * calls in an {@link android.os.AsyncTask}, or better yet, a
 * {@link android.app.Service}.
 *
 * Created by liutao on 12/6/14.
 */
public class SyncHelper {
    private static final String TAG = makeLogTag("SyncHelper");

    private Context mContext;

    public SyncHelper(Context context) {
        mContext = context;
    }

    // TODO
    public boolean performSync(SyncResult syncResult, Account account, Bundle extras) {
        return false;
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
