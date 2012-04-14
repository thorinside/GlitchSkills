package org.nsdev.glitchskills;

import org.json.JSONObject;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        mode.setTitle(title);
        this.glitchSkillsActivity.getSupportMenuInflater().inflate(R.menu.learn_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        if (item.getItemId() == R.id.learn)
        {
            this.glitchSkillsActivity.learnSkill(skill);
        }
        /*
        else if (item.getItemId() == R.id.queue)
        {
            this.glitchSkillsActivity.queueSkill(skill);
        }
        */
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        // TODO Auto-generated method stub
        
    }
}
