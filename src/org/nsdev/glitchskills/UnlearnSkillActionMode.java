package org.nsdev.glitchskills;

import org.json.JSONObject;
import android.support.v4.view.ActionMode;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ActionMode.Callback;

class UnlearnSkillActionMode implements Callback
{
    private final GlitchSkillsActivity glitchSkillsActivity;
    private final String title;
    private final JSONObject skill;

    UnlearnSkillActionMode(GlitchSkillsActivity glitchSkillsActivity, String title, JSONObject skill)
    {
        this.glitchSkillsActivity = glitchSkillsActivity;
        this.title = title;
        this.skill = skill;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        if (item.getItemId() == R.id.unlearn)
        {
            this.glitchSkillsActivity.unlearnSkill(skill);
        }
        mode.finish();
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {

        mode.setTitle(title);
        this.glitchSkillsActivity.getMenuInflater().inflate(R.menu.unlearn_menu, menu);

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        return false;
    }
}
