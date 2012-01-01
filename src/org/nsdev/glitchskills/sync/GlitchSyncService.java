package org.nsdev.glitchskills.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service to handle Glitch Skill sync. It instantiates the syncadapter and returns its
 * IBinder.
 */
public class GlitchSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static GlitchSyncAdapter sSyncAdapter = null;

    /*
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new GlitchSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
