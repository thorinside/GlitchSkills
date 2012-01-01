package org.nsdev.glitchskills;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ContentHelper
{

    public static JSONObject getContent(Context context, String method)
    {
        Uri uri = Uri.parse("content://" + Constants.AUTHORITY + "/" + method);

        Cursor c = context.getContentResolver().query(uri, null, null, null, null);

        c.moveToFirst();
        String res = c.getString(c.getColumnIndex("response"));
        c.close();

        try
        {
            JSONObject response = (JSONObject)new JSONTokener(res).nextValue();
            return response;
        }
        catch (JSONException e)
        {
            if (Constants.DEBUG)
                e.printStackTrace();
        }

        return null;
    }
}
