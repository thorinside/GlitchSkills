package org.nsdev.glitchskills;

import org.json.JSONObject;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SkillsTreeFragment extends Fragment
{
    
    SkillTreeView skillTree;
    JSONObject learnedResponse;
    JSONObject availableResponse;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.skill_tree_layout, null);
        skillTree = (SkillTreeView)v.findViewById(R.id.skillTreeView1);
        if (learnedResponse != null) skillTree.setLearned(learnedResponse);
        if (availableResponse != null) skillTree.setAvailable(availableResponse);
        return v;
    }

    public void setLearned(JSONObject response)
    {
        learnedResponse = response;
        if (skillTree != null) skillTree.setLearned(response);
    }

    public void setAvailable(JSONObject response)
    {
        availableResponse = response;
        if (skillTree != null) skillTree.setAvailable(response);
    }
    
}
