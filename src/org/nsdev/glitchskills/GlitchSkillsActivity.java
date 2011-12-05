package org.nsdev.glitchskills;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONException;
import org.json.JSONObject;
import org.nsdev.glitchskills.LearningWidget.UpdateService;
import org.nsdev.glitchskills.SkillsAdapter.SkillsCategory;
import org.nsdev.glitchskills.db.DatabaseHelper;
import org.nsdev.glitchskills.db.QueuedSkill;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.github.droidfu.cachefu.ImageCache;
import com.github.droidfu.imageloader.ImageLoader;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;
import com.tinyspeck.android.GlitchSessionDelegate;

public class GlitchSkillsActivity extends FragmentActivity implements GlitchSessionDelegate, GlitchRequestDelegate, TabListener
{
    static final String TAG = "GlitchSkills";

    static final boolean IS_HONEYCOMB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private static final String AUTH_CHECK = "auth.check";
    private static final String PLAYERS_INFO = "players.info";
    private static final String SKILLS_LIST_AVAILABLE = "skills.listAvailable";
    @SuppressWarnings("unused")
    private static final String SKILLS_LIST_LEARNED = "skills.listLearned";
    static final String SKILLS_LIST_LEARNING = "skills.listLearning";
    static final String SKILLS_LIST_UNLEARNING = "skills.listUnlearning";
    private static final String SKILLS_LIST_UNLEARNABLE= "skills.listUnlearnable";
    private static final String SKILLS_LEARN = "skills.learn";
    private static final String SKILLS_UNLEARN = "skills.unlearn";
    private static final String SKILLS_CANCEL_UNLEARN = "skills.cancelUnlearning";
    
    private static final int ACTION_LEARN_OR_QUEUE = 1;
    private static final int ACTION_CANCEL_UNLEARNING = 2;
    private static final int ACTION_UNLEARN = 3;
    
    static final int DIALOG_LOGIN_FAIL_ID = 0;
    static final int DIALOG_REQUEST_FAIL_ID = 1;


    private final boolean DEBUG = true;

    private Glitch glitch;
    Handler handler = new Handler();
    private ListFragment studyingFragment;
    private ClickableListFragment learnableFragment;
    private ClickableListFragment unlearnableFragment;
    private DatabaseHelper databaseHelper;
    
    private AtomicInteger updatingLearnableFragment = new AtomicInteger();
    private AtomicInteger updatingUnlearnableFragment = new AtomicInteger();;
    private AtomicInteger updatingStudyingFragment = new AtomicInteger();;

    private Runnable learningTimeUpdateHandler = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                studyingFragment.getListView().invalidateViews();
                handler.postDelayed(learningTimeUpdateHandler, 1000);
            }
            catch (Throwable ex)
            {
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.main);

        ImageLoader.initialize(getApplicationContext());
        ImageLoader.getImageCache().enableDiskCache(getApplicationContext(), ImageCache.DISK_CACHE_INTERNAL);

        final ActionBar ab = getSupportActionBar();

        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayHomeAsUpEnabled(false);

        // Always ignore the savedInstanceState otherwise
        // an old fragment may be visible instead of the new ones.
        super.onCreate(null);

        if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS)
        {
            ab.setDisplayShowTitleEnabled(false);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        studyingFragment = new ClickableListFragment(this);
        learnableFragment = new ClickableListFragment(this);
        unlearnableFragment = new ClickableListFragment(this);

        final FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {

            @Override
            public Fragment getItem(int position)
            {
                if (DEBUG)
                    Log.d(TAG, "Asked for position " + position);
                switch (position)
                {
                    case 0:
                        return studyingFragment;
                    case 1:
                        return learnableFragment;
                    case 2:
                        return unlearnableFragment;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount()
            {
                return 3;
            }

        };

        fragmentPager = new ViewPager(GlitchSkillsActivity.this);
        fragmentPager.setAdapter(adapter);
        fragmentPager.setId(999);

        fragmentPager.setOnPageChangeListener(new OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int position)
            {
                ab.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });

        Fragment rootFragment = new Fragment()
        {

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, @SuppressWarnings("hiding") Bundle savedInstanceState)
            {
                return fragmentPager;
            }

        };

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.root, rootFragment);
        ft.add(fragmentPager.getId(), studyingFragment);
        ft.add(fragmentPager.getId(), learnableFragment);
        ft.add(fragmentPager.getId(), unlearnableFragment);
        ft.commit();

        ab.addTab(ab.newTab().setText("Studying").setTabListener(this));
        ab.addTab(ab.newTab().setText("Learn").setTabListener(this));
        ab.addTab(ab.newTab().setText("Unlearn").setTabListener(this));

        glitch = new Glitch("145-51ad5d3a7e58913e63707fc1cfdde3bda2ff39f3", "nosuchglitch://auth");
        Intent intent = getIntent();
        handleIntent(intent);

    }

    public void handleIntent(Intent intent)
    {
        if (intent.hasCategory(Intent.CATEGORY_BROWSABLE))
        {

            final Uri uri = intent.getData();

            if (uri != null)
            {
                glitch.handleRedirect(this, uri, this);
            }

        }
        else
        {

            glitch.authorize(this, "write", this);

        }

    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    public void onListItemClick(final JSONObject skill, int action)
    {
        if (skill != null)
        {
            final String title = String.format("%s", skill.optString("name"));

            if (IS_HONEYCOMB)
            {
                if (action == ACTION_LEARN_OR_QUEUE)
                    startActionMode(new LearnSkillActionMode(this, title, skill));
                else if (action == ACTION_UNLEARN)
                    startActionMode(new UnlearnSkillActionMode(this, title, skill));
                else if (action == ACTION_CANCEL_UNLEARNING)
                    startActionMode(new CancelUnlearningActionMode(this, title, skill));
            }
            else
            {
                if (action == ACTION_LEARN_OR_QUEUE)
                {
                    // Show a confirmation dialog
                    Builder confirmationDialogBuilder = new AlertDialog.Builder(GlitchSkillsActivity.this);
                    confirmationDialogBuilder.setMessage(title);
                    confirmationDialogBuilder.setCancelable(true);
                    confirmationDialogBuilder.setNegativeButton(android.R.string.cancel, null);
                    /*
                    confirmationDialogBuilder.setNegativeButton(R.string.queue, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            queueSkill(skill);
                        }
                    });
                    */
                    confirmationDialogBuilder.setPositiveButton(R.string.learn, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            learnSkill(skill);
                        }
                    });
                    confirmationDialogBuilder.create().show();
                }
                else if (action == ACTION_UNLEARN)
                {
                    // Show a confirmation dialog
                    Builder confirmationDialogBuilder = new AlertDialog.Builder(GlitchSkillsActivity.this);
                    confirmationDialogBuilder.setMessage(title);
                    confirmationDialogBuilder.setCancelable(true);
                    confirmationDialogBuilder.setNegativeButton(android.R.string.cancel, null);
                    confirmationDialogBuilder.setPositiveButton(R.string.unlearn, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            unlearnSkill(skill);
                        }
                    });
                    confirmationDialogBuilder.create().show();
                }
                else if (action == ACTION_CANCEL_UNLEARNING)
                {
                    // Show a confirmation dialog
                    Builder confirmationDialogBuilder = new AlertDialog.Builder(GlitchSkillsActivity.this);
                    confirmationDialogBuilder.setMessage(title);
                    confirmationDialogBuilder.setCancelable(true);
                    confirmationDialogBuilder.setNegativeButton(android.R.string.cancel, null);
                    confirmationDialogBuilder.setPositiveButton(R.string.cancel_unlearn, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            cancelUnlearnSkill(skill);
                        }
                    });
                    confirmationDialogBuilder.create().show();
                }
            }
        }
    }

    @Override
    public void glitchLoginSuccess()
    {
        GlitchRequest request = glitch.getRequest(AUTH_CHECK);
        request.execute(this);
    }

    @Override
    public void glitchLoginFail()
    {
        showDialog(DIALOG_LOGIN_FAIL_ID);
    }

    @Override
    public void glitchLoggedOut()
    {
        if (DEBUG)
            Log.e(TAG, "Not logged in.");
        showDialog(DIALOG_LOGIN_FAIL_ID);
    }

    @Override
    public void requestFinished(GlitchRequest request)
    {

        if (isFinishing())
            return;

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
            if (error.equals("invalid_token"))
            {
                Toast.makeText(getApplicationContext(), "Invalid Token", Toast.LENGTH_LONG).show();
            }
            else if (method.equals(SKILLS_UNLEARN))
            {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                return;
            }
            else if (method.equals(SKILLS_LEARN))
            {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                return;
            }

            if (clearAuthorizationToken())
            {
                // Reauthorize
                glitch.authorize(this, "write", this);
            }

            return;
        }

        if (method.equals(AUTH_CHECK))
        {
            performRefresh(getSupportActionBar().getSelectedNavigationIndex());
        }
        else if (method.equals(PLAYERS_INFO))
        {
            String playerName = response.optString("player_name");
            Toast.makeText(getApplicationContext(), String.format("Hello %s", playerName), Toast.LENGTH_LONG).show();
        }
        else if (method.equals(SKILLS_LIST_AVAILABLE))
        {
            updateListFragment(learnableFragment, "skills", "You seem to have learned all there is to know. Show off!", response, null, true, ACTION_LEARN_OR_QUEUE, 0);
            updatingLearnableFragment.decrementAndGet();
        }
        else if (method.equals(SKILLS_LIST_LEARNING))
        {
            updateListFragment(studyingFragment, "learning", "You're not learning or unlearning anything. An idle magic rock is not a happy magic rock.", response, "Learning", false, 0, 0);
            updatingStudyingFragment.decrementAndGet();
        }
        else if (method.equals(SKILLS_LIST_UNLEARNING))
        {
            updateListFragment(studyingFragment, "unlearning", "You're not learning or unlearning anything. An idle magic rock is not a happy magic rock.", response, "Unlearning", true, ACTION_CANCEL_UNLEARNING, 1);
            updatingStudyingFragment.decrementAndGet();
        }
        else if (method.equals(SKILLS_LIST_UNLEARNABLE))
        {
            updateListFragment(unlearnableFragment, "skills", "There's absolutely nothing to unlearn when you don't know anything in the first place! Or, perhaps you need to learn to unlearn?", response, null, true, ACTION_UNLEARN, 0);
            updatingUnlearnableFragment.decrementAndGet();
        }
        else if (method.equals(SKILLS_LEARN))
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    performRefresh(1);
                    resetAdapter(studyingFragment, true);
                    resetAdapter(unlearnableFragment, true);
                    updateAppWidget();
                }
            });
        }
        else if (method.equals(SKILLS_UNLEARN))
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    performRefresh(2);
                    resetAdapter(studyingFragment, true);
                    resetAdapter(learnableFragment, true);
                    updateAppWidget();
                }
            });
        }
        else if (method.equals(SKILLS_CANCEL_UNLEARN))
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    resetAdapter(studyingFragment, true);
                    performRefresh(0);
                    resetAdapter(unlearnableFragment, true);
                    updateAppWidget();
                }
            });
        }
    }

    private void updateListFragment(ListFragment listFragment, String listName, String emptyListMessage, JSONObject response, String title, boolean hasAction, int action, int order)
    {
        try
        {
            synchronized(listFragment)
            {
                if (listFragment.getListAdapter() == null)
                {
                    listFragment.setListAdapter(new SkillsAdapter(this));
                }
                
                SkillsAdapter adapter = (SkillsAdapter)listFragment.getListAdapter();
                
                listFragment.setListShown(true);
                listFragment.setEmptyText(emptyListMessage);
                if (response.has(listName) && !response.isNull(listName))
                {
                    JSONObject skills = response.getJSONObject(listName);
                    SkillsCategory category = new SkillsCategory(skills, title != null, title, hasAction, action, order);
                    adapter.addSkillsCategory(category);
                }
            }
        }
        catch (JSONException e)
        {
        }
        catch (IllegalStateException e)
        {
        }
    }

    public boolean clearAuthorizationToken()
    {
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
    public void requestFailed(GlitchRequest request)
    {
        // Need to ensure these get decremented, otherwise
        // we may never be able to get the values if the network goes
        // down.
        if (request.method.equals(SKILLS_LIST_AVAILABLE))
        {
            updatingLearnableFragment.decrementAndGet();
        }
        else if (request.method.equals(SKILLS_LIST_LEARNING))
        {
            updatingStudyingFragment.decrementAndGet();
        }
        else if (request.method.equals(SKILLS_LIST_UNLEARNING))
        {
            updatingStudyingFragment.decrementAndGet();
        }
        else if (request.method.equals(SKILLS_LIST_UNLEARNABLE))
        {
            updatingUnlearnableFragment.decrementAndGet();
        }
        
        showDialog(DIALOG_REQUEST_FAIL_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        Dialog dialog;

        switch (id)
        {
            case DIALOG_LOGIN_FAIL_ID:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Login failure!").setCancelable(false).setPositiveButton("Darn", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(@SuppressWarnings("hiding") DialogInterface dialog, @SuppressWarnings("hiding") int id)
                    {
                        dialog.cancel();
                    }
                });
                dialog = builder.create();

                break;

            case DIALOG_REQUEST_FAIL_ID:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Request failure!").setCancelable(false).setPositiveButton("Argh!", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(@SuppressWarnings("hiding") DialogInterface dialog, @SuppressWarnings("hiding") int id)
                    {
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
    public void onTabReselected(Tab tab, FragmentTransaction ft)
    {
    }

    private ViewPager fragmentPager;

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft)
    {
        if (glitch == null)
            return;

        if (DEBUG) Log.d(TAG, "onTabSelected");
        
        fragmentPager.setCurrentItem(tab.getPosition(), true);
        if (isAdapterNull(tab.getPosition()))
            performRefresh(tab.getPosition());
        
        if (tab.getPosition() == 0)
            handler.postDelayed(learningTimeUpdateHandler, 0);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft)
    {
        if (tab.getPosition() == 0)
            handler.removeCallbacks(learningTimeUpdateHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.menu_item_log_out:
            {
                clearAuthorizationToken();
                updateAppWidget();
                finish();
                return true;
            }

            case R.id.menu_item_refresh:
            {
                performRefresh(getSupportActionBar().getSelectedNavigationIndex());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public synchronized void performRefresh(int position)
    {
        if (position == 0 && updatingStudyingFragment.get() == 0)
        {
            if (DEBUG) Log.d(TAG, "Updating Studying Fragment");
            updatingStudyingFragment.set(2);
            
            resetAdapter(studyingFragment, false);
            glitch.getRequest(SKILLS_LIST_LEARNING).execute(this);
            glitch.getRequest(SKILLS_LIST_UNLEARNING).execute(this);
        }
        else if (position == 1 && updatingLearnableFragment.get() == 0)
        {
            if (DEBUG) Log.d(TAG, "Updating Learnable Fragment");
            updatingLearnableFragment.set(1);
            
            resetAdapter(learnableFragment, false);
            glitch.getRequest(SKILLS_LIST_AVAILABLE).execute(this);
        }
        else if (position == 2 && updatingUnlearnableFragment.get() == 0)
        {
            if (DEBUG) Log.d(TAG, "Updating Unlearnable Fragment");
            updatingUnlearnableFragment.set(1);
            
            resetAdapter(unlearnableFragment, false);
            glitch.getRequest(SKILLS_LIST_UNLEARNABLE).execute(this);
        }
    }
    
    private boolean isAdapterNull(int position)
    {
        switch(position)
        {
            case 0:
                return studyingFragment.getListAdapter() == null;
            case 1:
                return learnableFragment.getListAdapter() == null;
            case 2:
                return unlearnableFragment.getListAdapter() == null;
        }
        
        return false;
    }

    private void resetAdapter(ListFragment listFragment, boolean kill)
    {
        SkillsAdapter adapter = (SkillsAdapter)listFragment.getListAdapter();
        if (adapter != null)
        {
            adapter.reset(false);
            try
            {
                listFragment.setListShown(false);
            } catch (IllegalStateException ex) {
            }
            
            if (kill)
            {
                listFragment.setListAdapter(null);
            }
        }
    }

    public void unlearnSkill(JSONObject skill)
    {
        String tsid = skill.optString("class_tsid");

        if (tsid != null)
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put("skill_class", tsid);
            GlitchRequest req = glitch.getRequest(SKILLS_UNLEARN, params);
            req.execute(GlitchSkillsActivity.this);
        }
    }
    
    public void cancelUnlearnSkill(JSONObject skill)
    {
        String tsid = skill.optString("class_tsid");

        if (tsid != null)
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put("skill_class", tsid);
            GlitchRequest req = glitch.getRequest(SKILLS_CANCEL_UNLEARN, params);
            req.execute(GlitchSkillsActivity.this);
        }
    }

    public void learnSkill(JSONObject skill)
    {
        String tsid = skill.optString("class_tsid");

        if (tsid != null)
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put("skill_class", tsid);
            GlitchRequest req = glitch.getRequest(SKILLS_LEARN, params);
            req.execute(GlitchSkillsActivity.this);
        }
    }

    public void queueSkill(JSONObject skill)
    {
        if (DEBUG)
            Log.i(TAG, "Queueing skill.");

        String skillId = skill.optString("class_tsid");

        try
        {
            Dao<QueuedSkill, Integer> dao = getHelper().getQueuedSkillDao();
            List<QueuedSkill> queuedSkills = dao.queryForAll();

            boolean alreadyLearning = false;

            for (QueuedSkill q: queuedSkills)
            {
                if (q.getId().equals(skillId))
                {
                    alreadyLearning = true;
                }
            }

            if (alreadyLearning)
            {
                Toast.makeText(getApplicationContext(), "You've already queued that skill, silly.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                QueuedSkill queuedSkill = new QueuedSkill(skillId, queuedSkills.size());
                dao.create(queuedSkill);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    public void updateAppWidget()
    {
        final Intent intent = new Intent(GlitchSkillsActivity.this, UpdateService.class);
        intent.setAction("NetworkUpdate");
        intent.putExtra("ClearCache", true);
        startService(intent);
    }

    private DatabaseHelper getHelper()
    {
        if (databaseHelper == null)
        {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        handler.removeCallbacks(learningTimeUpdateHandler);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (getSupportActionBar().getSelectedTab().getPosition() == 0)
            handler.postDelayed(learningTimeUpdateHandler, 0);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        handler.removeCallbacks(learningTimeUpdateHandler);

        if (databaseHelper != null)
        {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}
