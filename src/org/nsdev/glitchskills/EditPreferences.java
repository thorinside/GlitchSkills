package org.nsdev.glitchskills;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActionBar;

public class EditPreferences extends PreferenceActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
    }

}
