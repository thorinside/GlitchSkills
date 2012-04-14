package org.nsdev.glitchskills;

import org.json.JSONObject;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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
        this.glitchSkillsActivity.getSupportMenuInflater().inflate(R.menu.unlearn_menu, menu);

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
