package org.nsdev.glitchskills.authenticator;

import org.nsdev.glitchskills.Constants;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service to handle Account authentication. It instantiates the authenticator
 * and returns its IBinder.
 */
public class AuthenticationService extends Service {
    private static final String TAG = "AuthenticationService";
    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        if (Constants.DEBUG) Log.e(TAG, "SyncAdapter Authentication Service started.");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        if (Constants.DEBUG) Log.e(TAG, "SyncAdapter Authentication Service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (Constants.DEBUG) Log.e(TAG,
                "getBinder()...  returning the AccountAuthenticator binder for intent "
                    + intent);
        return mAuthenticator.getIBinder();
    }
}
