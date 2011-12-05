package org.nsdev.glitchskills;

import org.json.JSONObject;
import android.support.v4.view.ActionMode;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ActionMode.Callback;

class CancelUnlearningActionMode implements Callback
{
    private final GlitchSkillsActivity glitchSkillsActivity;
    private final String title;
    private final JSONObject skill;

    CancelUnlearningActionMode(GlitchSkillsActivity glitchSkillsActivity, String title, JSONObject skill)
    {
        this.glitchSkillsActivity = glitchSkillsActivity;
        this.title = title;
        this.skill = skill;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        if (item.getItemId() == R.id.cancel_unlearn)
        {
            this.glitchSkillsActivity.cancelUnlearnSkill(skill);
        }
        mode.finish();
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {

        mode.setTitle(title);
        this.glitchSkillsActivity.getMenuInflater().inflate(R.menu.cancel_unlearn_menu, menu);

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
