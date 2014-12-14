/*
 * Copyright 2013 Google Inc.
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
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.sonaive.v2ex.provider.V2exContract;
import com.sonaive.v2ex.util.AccountUtils;

/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {

    private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    public static void createSyncAccount(Context context) {

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = AccountUtils.getActiveAccount(context);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(account, null, null);
        // Inform the system that this account supports sync
        ContentResolver.setIsSyncable(account, V2exContract.CONTENT_AUTHORITY, 1);
        // Inform the system that this account is eligible for auto sync when the network is up
        ContentResolver.setSyncAutomatically(account, V2exContract.CONTENT_AUTHORITY, true);

        Bundle extras = new Bundle();

        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        extras.putBoolean(SyncAdapter.EXTRA_SYNC_REMOTE, true);

        // Recommend a schedule for automatic synchronization. The system may modify this based
        // on other scheduled syncs and network utilization.
        ContentResolver.addPeriodicSync(
                account, V2exContract.CONTENT_AUTHORITY, extras, SYNC_FREQUENCY);
    }
}
