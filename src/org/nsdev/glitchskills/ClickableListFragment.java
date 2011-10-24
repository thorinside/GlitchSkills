package org.nsdev.glitchskills;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.view.View;
import android.widget.ListView;

public class ClickableListFragment extends ListFragment {

    private GlitchSkillsActivity glitchSkillsActivity;

    public ClickableListFragment() {
    }
    
    ClickableListFragment(GlitchSkillsActivity glitchSkillsActivity) {
        this.glitchSkillsActivity = glitchSkillsActivity;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        glitchSkillsActivity.onListItemClick(l, v, position, id);
    }

    @Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        glitchSkillsActivity = (GlitchSkillsActivity)activity;
    }

}