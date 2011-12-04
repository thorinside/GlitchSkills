package org.nsdev.glitchskills;

import org.json.JSONObject;
import android.support.v4.view.ActionMode;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ActionMode.Callback;

class LearnSkillActionMode implements Callback
{
    private final GlitchSkillsActivity glitchSkillsActivity;
    private final String title;
    private final JSONObject skill;

    LearnSkillActionMode(GlitchSkillsActivity glitchSkillsActivity, String title, JSONObject skill)
    {
        this.glitchSkillsActivity = glitchSkillsActivity;
        this.title = title;
        this.skill = skill;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        if (item.getItemId() == R.id.learn)
        {
            this.glitchSkillsActivity.learnSkill(skill);
        }
        else if (item.getItemId() == R.id.queue)
        {
            this.glitchSkillsActivity.queueSkill(skill);
        }
        mode.finish();
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        mode.setTitle(title);
        this.glitchSkillsActivity.getMenuInflater().inflate(R.menu.learn_menu, menu);

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
