package org.nsdev.glitchskills.content;

import org.nsdev.glitchskills.Constants;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class GlitchSkillsProvider extends ContentProvider
{

    private static final String TAG = "GlitchSkillsProvider";

    public GlitchSkillsProvider()
    {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        return "application/json";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

    @Override
    public boolean onCreate()
    {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        String path = uri.getPath();
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        
        String value = getContext().getSharedPreferences("CachedResponses", Context.MODE_PRIVATE).getString(path, null);
        
        MatrixCursor cursor = new MatrixCursor(new String[] { "response" });
        if (value != null)
        {
            cursor.addRow(new Object[] { value });
            return cursor;
        }
        
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        Log.e(TAG, String.format("update: %s %s %s %s", uri, values, selection, selectionArgs));
        
        String path = uri.getPath();
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        
        if (path.length() > 0)
        {
            if (Constants.DEBUG) Log.d(TAG, "Handling path: "+path);
        
            String response = values.getAsString("response");
            getContext().getSharedPreferences("CachedResponses", Context.MODE_PRIVATE)
                .edit()
                .putString(path, response)
                .commit();
            
            if (Constants.DEBUG) Log.d(TAG, "Cached response.");
            
            Intent intent = new Intent();
            intent.setAction(Constants.CONTENT_UPDATED_ACTION);
            intent.putExtra(Constants.REQUEST_METHOD, path);
            intent.putExtra(Constants.URI, uri);
            getContext().sendBroadcast(intent);
            
            if (Constants.DEBUG) Log.d(TAG, "Fired broadcast intent.");
            
            return 1;
        }
        
        return 0;
    }

}
