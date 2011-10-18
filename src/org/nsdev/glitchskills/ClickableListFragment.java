package org.nsdev.glitchskills;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ClickableListFragment extends ListFragment {

    private GlitchSkillsActivity glitchSkillsActivity;

    public ClickableListFragment() {
    }
    
    /**
     * @param glitchSkillsActivity
     */
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
        // TODO Auto-generated method stub
        super.onAttach(activity);
        
        glitchSkillsActivity = (GlitchSkillsActivity)activity;
        Log.e("F","onAttach: "+glitchSkillsActivity);
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        
        Log.e("F","onStart");
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.e("F","onResume");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.e("F","onPause");
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.e("F","onStop");
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
        Log.e("F","onDetach");
   }
    
    
    
    

}