package org.nsdev.glitchskills;

import org.nsdev.glitchskills.SkillsAdapter.SkillsListEntry;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.view.View;
import android.widget.ListView;

public class ClickableListFragment extends ListFragment {

    private GlitchSkillsActivity glitchSkillsActivity;

    public ClickableListFragment() {
    }
    
    public ClickableListFragment(GlitchSkillsActivity glitchSkillsActivity)
    {
        this.glitchSkillsActivity = glitchSkillsActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        SkillsAdapter adapter = (SkillsAdapter)l.getAdapter();
        SkillsListEntry entry = (SkillsListEntry)adapter.getItem(position);
        glitchSkillsActivity.onListItemClick(adapter.getSkill(position), entry.category.action);
    }
    
    @Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        glitchSkillsActivity = (GlitchSkillsActivity)activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getListView().setFastScrollEnabled(true);
    }
}