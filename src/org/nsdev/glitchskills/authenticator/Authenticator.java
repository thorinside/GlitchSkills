/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nsdev.glitchskills.authenticator;

import org.nsdev.glitchskills.Constants;
import org.nsdev.glitchskills.R;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This class is an implementation of AbstractAccountAuthenticator that is remarkably empty.
 */
class Authenticator extends AbstractAccountAuthenticator
{
    // Authentication Service context
    private static final String TAG = "Authenticator";
    private final Context mContext;

    public Authenticator(Context context)
    {
        super(context);
        mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
    {
        if (Constants.DEBUG)
            Log.e(TAG, "addAccount");

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options)
    {
        if (Constants.DEBUG)
            Log.e(TAG, "confirmCredentials");

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
    {
        if (Constants.DEBUG)
            Log.e(TAG, "editProperties");

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions)
    {
        if (Constants.DEBUG)
            Log.e(TAG, "getAuthToken");

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthTokenLabel(String authTokenType)
    {
        if (Constants.DEBUG)
            Log.e(TAG, "getAuthTokenLabel");

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
    {
        if (Constants.DEBUG)
            Log.e(TAG, "hasFeatures");

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions)
    {
        if (Constants.DEBUG)
            Log.e(TAG, "updateCredentials");

        throw new UnsupportedOperationException();
    }

}
