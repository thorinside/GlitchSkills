package org.nsdev.glitchskills;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBar;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.app.ActionBar.TabListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ActionMode;
import android.support.v4.view.ActionMode.Callback;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;
import com.tinyspeck.android.GlitchSessionDelegate;

public class GlitchSkillsActivity extends FragmentActivity implements GlitchSessionDelegate, GlitchRequestDelegate,
        TabListener {

    static final boolean IS_HONEYCOMB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private static final String AUTH_CHECK = "auth.check";
    private static final String PLAYERS_INFO = "players.info";
    private static final String SKILLS_LIST_AVAILABLE = "skills.listAvailable";
    private static final String SKILLS_LIST_LEARNED = "skills.listLearned";
    private static final String SKILLS_LIST_LEARNING = "skills.listLearning";
    private static final String SKILLS_LEARN = "skills.learn";

    static final int DIALOG_LOGIN_FAIL_ID = 0;
    static final int DIALOG_REQUEST_FAIL_ID = 1;

    private Glitch glitch;

    Handler handler = new Handler();

    private ListFragment learningFragment;

    private ClickableListFragment availableFragment;

    private ListFragment learnedFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Always ignore the savedInstanceState otherwise
        // an old fragment may be visible instead of the new ones.
        super.onCreate(null);
        
        setContentView(R.layout.main);

        final ActionBar ab = getSupportActionBar();

        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);

        if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        learningFragment = new ListFragment();
        availableFragment = new ClickableListFragment(this);
        learnedFragment = new ListFragment();

        glitch = new Glitch("145-51ad5d3a7e58913e63707fc1cfdde3bda2ff39f3", "nosuchglitch://auth");

        Intent intent = getIntent();
        if (intent.hasCategory(Intent.CATEGORY_BROWSABLE)) {

            final Uri uri = intent.getData();

            if (uri != null) {
                glitch.handleRedirect(this, uri, this);
            }

        } else {

            glitch.authorize(this, "write", this);

        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.root, learningFragment);
        ft.add(R.id.root, availableFragment);
        ft.add(R.id.root, learnedFragment);
        ft.commit();

        ab.addTab(ab.newTab().setText("Learning").setTabListener(this));
        ab.addTab(ab.newTab().setText("Available").setTabListener(this));
        ab.addTab(ab.newTab().setText("Learned").setTabListener(this));
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        if (l.getAdapter() instanceof SkillsAdapter) {
            try {
                final JSONObject skill = ((SkillsAdapter)l.getAdapter()).getSkill(position);

                if (skill != null) {
                    final String title = String.format("Are you sure you want to learn the %s skill?",
                            skill.getString("name"));

                    if (IS_HONEYCOMB) {
                        startActionMode(new LearnSkillActionMode(title, skill));
                    } else {
                        // Show a confirmation dialog
                        Builder confirmationDialogBuilder = new AlertDialog.Builder(GlitchSkillsActivity.this);
                        confirmationDialogBuilder.setMessage(title);
                        confirmationDialogBuilder.setCancelable(false);
                        confirmationDialogBuilder.setNegativeButton(android.R.string.no, null);
                        confirmationDialogBuilder.setPositiveButton(android.R.string.yes, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                learnSkill(skill);
                            }
                        });
                        confirmationDialogBuilder.create().show();
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void glitchLoginSuccess() {
        GlitchRequest request = glitch.getRequest(AUTH_CHECK);
        request.execute(this);
    }

    @Override
    public void glitchLoginFail() {
        showDialog(DIALOG_LOGIN_FAIL_ID);
    }

    @Override
    public void glitchLoggedOut() {
        Log.e("GSA", "Not logged in.");
        showDialog(DIALOG_LOGIN_FAIL_ID);
    }

    @Override
    public void requestFinished(GlitchRequest request) {
        
        if (isFinishing()) return;
        
        if (request == null)
            return;
        if (request.method == null)
            return;
        if (request.response == null)
            return;

        String method = request.method;
        JSONObject response = request.response;
        
        if (response.has("error"))
        {
            String error = response.optString("error");
            if (error.equals("invalid_token")) {
                Toast.makeText(getApplicationContext(), "Invalid Token", Toast.LENGTH_LONG).show();
            }

            if (clearAuthorizationToken())
            {
                // Reauthorize
                glitch.authorize(this, "write", this);
            }

            return;
        }

        if (method.equals(AUTH_CHECK)) {
            
        } else if (method.equals(PLAYERS_INFO)) {

            String playerName = response.optString("player_name");
            Toast.makeText(getApplicationContext(), String.format("Hello %s", playerName), Toast.LENGTH_LONG).show();

        } else if (method.equals(SKILLS_LIST_LEARNED)) {

            try {
                learnedFragment.setListShown(true);
                learnedFragment.setEmptyText("You must be new here, you haven't learned anything yet. Noob!");
                if (response.has("skills") && !response.isNull("skills")) {
                    JSONObject skills = response.getJSONObject("skills");
                    learnedFragment.setListAdapter(new SkillsAdapter(this, skills));
                } else {
                    availableFragment.setListAdapter(new SkillsAdapter(this));
                }
            } catch (JSONException e) {
            }

        } else if (method.equals(SKILLS_LIST_AVAILABLE)) {

            try {
                availableFragment.setListShown(true);
                availableFragment.setEmptyText("You seem to have learned all there is to know. Show off!");
                if (response.has("skills") && !response.isNull("skills")) {
                    JSONObject skills = response.getJSONObject("skills");
                    availableFragment.setListAdapter(new SkillsAdapter(this, skills));
                } else {
                    availableFragment.setListAdapter(new SkillsAdapter(this));
                }
            } catch (JSONException e) {
            }

        } else if (method.equals(SKILLS_LIST_LEARNING)) {

            try {
                learningFragment.setListShown(true);
                learningFragment.setEmptyText("You should be learning something instead of just sitting around!");
                if (response.has("learning") && !response.isNull("learning")) {
                    JSONObject skills = response.getJSONObject("learning");
                    learningFragment.setListAdapter(new SkillsAdapter(this, skills));
                } else {
                    learningFragment.setListAdapter(new SkillsAdapter(this));
                }

            } catch (JSONException e) {
            }

        }
    }

    public boolean clearAuthorizationToken() {
        // Clear the token
        SharedPreferences prefs = getSharedPreferences("glitchskills.auth", Context.MODE_PRIVATE);
        if (prefs.contains("redirectUri"))
        {
            prefs.edit().remove("redirectUri").commit();
            return true;
        }
        return false;
    }

    @Override
    public void requestFailed(GlitchRequest request) {
        showDialog(DIALOG_REQUEST_FAIL_ID);
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;

        switch (id) {
            case DIALOG_LOGIN_FAIL_ID:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Login failure!").setCancelable(false)
                        .setPositiveButton("Darn", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();

                break;

            case DIALOG_REQUEST_FAIL_ID:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Request failure!").setCancelable(false)
                        .setPositiveButton("Argh!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder1.create();

                break;
            default:
                dialog = null;
        }

        return dialog;
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        if (tab.getText().equals("Learning")) {
            glitch.getRequest(SKILLS_LIST_LEARNING).execute(this);
        } else if (tab.getText().equals("Learned")) {
            glitch.getRequest(SKILLS_LIST_LEARNED).execute(this);
        } else if (tab.getText().equals("Available")) {
            glitch.getRequest(SKILLS_LIST_AVAILABLE).execute(this);
        }
    }
    
    String lastTab = null;

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        
        if (ft == null)
            ft = getSupportFragmentManager().beginTransaction();

        boolean isForward = true;
        
        if (lastTab != null && lastTab.equals("Learned"))
        {
            isForward = false;
        } 
        else if (lastTab != null && (lastTab.equals("Available") && tab.getText().equals("Learning")))
        {
            isForward = false;
        }
        
        if (!isForward) {
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        
        if (tab.getText().equals("Learning")) {
            glitch.getRequest(SKILLS_LIST_LEARNING).execute(this);
            ft.show(learningFragment);
            ft.hide(availableFragment);
            ft.hide(learnedFragment);
        } else if (tab.getText().equals("Learned")) {
            glitch.getRequest(SKILLS_LIST_LEARNED).execute(this);
            ft.hide(learningFragment);
            ft.hide(availableFragment);
            ft.show(learnedFragment);
        } else if (tab.getText().equals("Available")) {
            glitch.getRequest(SKILLS_LIST_AVAILABLE).execute(this);
            ft.hide(learningFragment);
            ft.show(availableFragment);
            ft.hide(learnedFragment);
        }

        ft.commit();
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
       lastTab = tab.getText().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // set up a listener for the refresh item
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.menu_item_log_out:
            {
                clearAuthorizationToken();
                finish();
                return true;
            }
            
            case R.id.menu_item_refresh:
            {
                String tabName = getSupportActionBar().getSelectedTab().getText().toString();
                
                if (tabName.equals("Learning")) {
                    glitch.getRequest(SKILLS_LIST_LEARNING).execute(this);
                } else if (tabName.equals("Learned")) {
                    glitch.getRequest(SKILLS_LIST_LEARNED).execute(this);
                } else if (tabName.equals("Available")) {
                    glitch.getRequest(SKILLS_LIST_AVAILABLE).execute(this);
                }

                return true;
            }
        }
        
        
        return super.onOptionsItemSelected(item);
    }

    public void learnSkill(JSONObject skill) {
        String tsid = skill.optString("class_tsid");

        if (tsid != null) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("skill_class", tsid);
            GlitchRequest req = glitch.getRequest(SKILLS_LEARN, params);
            req.execute(GlitchSkillsActivity.this);

            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    glitch.getRequest(SKILLS_LIST_LEARNING).execute(GlitchSkillsActivity.this);
                    glitch.getRequest(SKILLS_LIST_AVAILABLE).execute(GlitchSkillsActivity.this);
                }
            }, 250);
        }
    }

    private class LearnSkillActionMode implements Callback {

        private final String title;
        private final JSONObject skill;

        private LearnSkillActionMode(String title, JSONObject skill) {
            this.title = title;
            this.skill = skill;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.yes) {
                learnSkill(skill);
            }
            mode.finish();
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            mode.setTitle(title);
            getMenuInflater().inflate(R.menu.learn_menu, menu);

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }

}
