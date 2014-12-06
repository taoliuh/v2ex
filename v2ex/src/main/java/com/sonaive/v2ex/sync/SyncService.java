/*
 * Copyright 2014 Google Inc. All rights reserved.
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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;
/**
 * Service to handle sync requests.
 *
 * <p>This service is invoked in response to Intents with action android.content.SyncAdapter, and
 * returns a Binder connection to SyncAdapter.
 *
 * <p>For performance, only one sync adapter will be initialized within this application's context.
 *
 * <p>Note: The SyncService itself is not notified when a new sync occurs. It's role is to
 * manage the lifecycle of our SyncAdapter and provide a handle to said SyncAdapter to the
 * OS on request.
 *
 * Service that handles sync. It simply instantiates a SyncAdapter and returns its IBinder.
 */
public class SyncService extends Service {

    private static final String TAG = makeLogTag(SyncService.class);

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        LOGD(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), false);
            }
        }
    }

    /**
     * Logging-only destructor.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LOGD(TAG, "Service destroyed");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
