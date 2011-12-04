package org.nsdev.glitchskills;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.view.View;
import android.widget.ListView;

public class ClickableListFragment extends ListFragment {

    private GlitchSkillsActivity glitchSkillsActivity;
    private final int requestCode;

    public ClickableListFragment() {
        requestCode = 0;
    }
    
    public ClickableListFragment(GlitchSkillsActivity glitchSkillsActivity, int requestCode)
    {
        this.glitchSkillsActivity = glitchSkillsActivity;
        this.requestCode  = requestCode;
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        glitchSkillsActivity.onListItemClick(l, v, position, requestCode);
    }
    
    @Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        glitchSkillsActivity = (GlitchSkillsActivity)activity;
    }
}