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
package com.sonaive.v2ex.util;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.sonaive.v2ex.R;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;
/**
 * Account and login utilities. This class manages a local shared preferences object
 * that stores which account is currently active.
 *
 * Created by liutao on 12/11/14.
 */
public class AccountUtils {
    private static final String TAG = makeLogTag(AccountUtils.class);

    private static final String PREF_ACTIVE_ACCOUNT = "chosen_account";

    // these names are are prefixes; the account is appended to them
    private static final String PREFIX_PREF_ID = "id_";
    private static final String PREFIX_PREF_AVATAR_MINI = "avatar_mini_";
    private static final String PREFIX_PREF_AVATAR_NORMAL = "avatar_mini_";
    private static final String PREFIX_PREF_AVATAR_LARGE = "avatar_large_";

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean hasActiveAccount(final Context context) {
        return !TextUtils.isEmpty(getActiveAccountName(context));
    }

    public static String getActiveAccountName(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString(PREF_ACTIVE_ACCOUNT, null);
    }

    public static Account getActiveAccount(final Context context) {
        String account = getActiveAccountName(context);
        if (account != null) {
            return new Account(account, account);
        } else {
            String defaultName = context.getResources().getString(R.string.app_name);
            return new Account(defaultName, defaultName);
        }
    }

    public static boolean setActiveAccount(final Context context, final String accountName) {
        LOGD(TAG, "Set active account to: " + accountName);
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(PREF_ACTIVE_ACCOUNT, accountName).commit();
        return true;
    }

    private static String makeAccountSpecificPrefKey(Context ctx, String prefix) {
        return hasActiveAccount(ctx) ? makeAccountSpecificPrefKey(getActiveAccountName(ctx),
                prefix) : null;
    }

    private static String makeAccountSpecificPrefKey(String accountName, String prefix) {
        return prefix + accountName;
    }

    public static void setAccountId(final Context context, final String accountName, final String id) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(makeAccountSpecificPrefKey(accountName, PREFIX_PREF_ID),
                id).commit();
    }

    public static String getAccountId(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return hasActiveAccount(context) ? sp.getString(makeAccountSpecificPrefKey(context,
                PREFIX_PREF_ID), null) : null;
    }

    public static void setAvatarMini(final Context context, final String accountName, final String imageUrl) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(makeAccountSpecificPrefKey(accountName, PREFIX_PREF_AVATAR_MINI),
                imageUrl).commit();
    }

    public static String getAvatarMini(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return hasActiveAccount(context) ? sp.getString(makeAccountSpecificPrefKey(context,
                PREFIX_PREF_AVATAR_MINI), null) : null;
    }

    public static void setAvatarNormal(final Context context, final String accountName, final String imageUrl) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(makeAccountSpecificPrefKey(accountName, PREFIX_PREF_AVATAR_NORMAL),
                imageUrl).commit();
    }

    public static String getAvatarNormal(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return hasActiveAccount(context) ? sp.getString(makeAccountSpecificPrefKey(context,
                PREFIX_PREF_AVATAR_NORMAL), null) : null;
    }

    public static void setAvatarLarge(final Context context, final String accountName, final String imageUrl) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(makeAccountSpecificPrefKey(accountName, PREFIX_PREF_AVATAR_LARGE),
                imageUrl).commit();
    }

    public static String getAvatarLarge(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return hasActiveAccount(context) ? sp.getString(makeAccountSpecificPrefKey(context,
                PREFIX_PREF_AVATAR_LARGE), null) : null;
    }
}
