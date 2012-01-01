package org.nsdev.glitchskills.sync;

import java.io.IOException;
import java.util.ArrayList;
import org.nsdev.glitchskills.Constants;
import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class GlitchSyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String TAG = "GlitchSyncAdapter";

    private final AccountManager mAccountManager;

    public GlitchSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    public GlitchSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, final ContentProviderClient provider, SyncResult syncResult)
    {
        if (Constants.DEBUG) Log.e(TAG, String.format("onPerformSync %s %s %s %s %s", account, extras, authority, provider, syncResult));

        try
        {
            String authtoken = mAccountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);

            Glitch g = new Glitch(Constants.API_KEY, Constants.AUTH_URI);
            g.accessToken = authtoken;

            ArrayList<GlitchRequest> requests = new ArrayList<GlitchRequest>();
            
            requests.add(g.getRequest(Constants.SKILLS_LIST_LEARNING));
            requests.add(g.getRequest(Constants.SKILLS_LIST_UNLEARNING));
            requests.add(g.getRequest(Constants.SKILLS_LIST_AVAILABLE));
            requests.add(g.getRequest(Constants.SKILLS_LIST_LEARNED));
            requests.add(g.getRequest(Constants.SKILLS_LIST_UNLEARNABLE));
            requests.add(g.getRequest(Constants.PLAYERS_INFO));

            GlitchRequestDelegate handler = new GlitchRequestDelegate()
            {

                @Override
                public void requestFinished(GlitchRequest request)
                {
                    if (Constants.DEBUG) Log.d(TAG, "Operation finished: " + request.method);

                    ContentValues values = new ContentValues();
                    try
                    {
                        values.put("response", request.response.toString());
                        provider.update(Uri.parse("content://org.nsdev.glitchskills/" + request.method), values, null, null);
                    }
                    catch (RemoteException e)
                    {
                        if (Constants.DEBUG) Log.e(TAG, "RemoteException unhandled", e);
                    }
                }

                @Override
                public void requestFailed(GlitchRequest request)
                {
                    if (Constants.DEBUG) Log.w(TAG, "Operation failed: " + request.method);
                }
            };
            
            for(GlitchRequest request: requests)
                request.execute(handler);

        }
        catch (OperationCanceledException e)
        {
            if (Constants.DEBUG) Log.e(TAG, "OperationCancelledException", e);
        }
        catch (AuthenticatorException e)
        {
            if (Constants.DEBUG) Log.e(TAG, "AuthenticatorException", e);
        }
        catch (IOException e)
        {
            if (Constants.DEBUG) Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        }
    }
}
