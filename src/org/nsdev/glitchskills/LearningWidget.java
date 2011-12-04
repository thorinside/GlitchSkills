package org.nsdev.glitchskills;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import org.json.JSONObject;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;
import com.tinyspeck.android.GlitchSessionDelegate;

public class LearningWidget extends AppWidgetProvider
{
    private boolean alarmSet = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, UpdateService.class);
        updateIntent.setAction("NetworkUpdate");
        context.startService(updateIntent);

        if (!alarmSet)
        {
            alarmSet = true;

            // Slow update
            final Intent intent = new Intent(context, UpdateService.class);
            intent.setAction("NetworkUpdate");
            final PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
            final AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pending);
            long interval = 1000 * 60 * 15;
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pending);

            // Fast update
            final Intent intent2 = new Intent(context, UpdateService.class);
            intent2.setAction("FastUpdate");
            final PendingIntent pending2 = PendingIntent.getService(context, 0, intent2, 0);
            alarm.cancel(pending2);
            long interval2 = 1000 * 60;
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval2, pending2);
        }
    }

    public static class UpdateService extends Service
    {
        private static JSONObject cachedSkill;
        private static Bitmap cachedIconBitmap;

        private final class LoadIconAsyncTask extends AsyncTask<String, Object, Bitmap>
        {
            private final Context context;
            private final RemoteViews updateViews;

            private LoadIconAsyncTask(Context context, RemoteViews updateViews)
            {
                this.context = context;
                this.updateViews = updateViews;
            }

            @Override
            protected Bitmap doInBackground(String... args)
            {
                String iconUrl = args[0];

                if (iconUrl != null)
                {
                    Bitmap b = null;
                    try
                    {
                        b = BitmapFactory.decodeStream(((java.io.InputStream)new java.net.URL(iconUrl).getContent()));
                        return b;
                    }
                    catch (MalformedURLException e)
                    {
                    }
                    catch (IOException e)
                    {
                    }
                    catch (NullPointerException e)
                    {
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result)
            {
                updateIconAndUpdateAppWidget(context, updateViews, result);
                
                if (cachedIconBitmap != null)
                {
                    cachedIconBitmap.recycle();
                }
                cachedIconBitmap = result;
            }
        }

        @Override
        public IBinder onBind(Intent arg0)
        {
            return null;
        }

        @Override
        public void onStart(Intent intent, int startId)
        {
            if (intent == null) return;
            
            String action = intent.getAction();
            
            if ("FinishedNotification".equals(action))
            {
                String text = intent.getStringExtra("text");
                NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification(R.drawable.ic_learning_finished, text, System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                notification.defaults = 0;
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.ledARGB = 0xFF00FF00;
                notification.ledOnMS = 250;
                notification.ledOffMS = 250;
                notification.vibrate = new long[] { 0L, 200L, 100L, 200L, 100L, 200L, 100L, 200L, 100L };
                
                CharSequence contentTitle = "Glitch Skills";
                CharSequence contentText = text;
                Intent notificationIntent = new Intent(this, GlitchSkillsActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, notification.flags);
                notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);

                nm.notify(0, notification);
            }
            else
            {
                buildUpdate(this, "NetworkUpdate".equals(action));
            }
        }

        public void buildUpdate(final Context context, boolean useNetwork)
        {
            final RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_learning);

            Intent intent = new Intent(context, GlitchSkillsActivity.class);
            intent.setAction("org.nsdev.apps.glitchskills.LEARN");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.skill_icon, pendingIntent);

            final Glitch glitch = new Glitch("145-51ad5d3a7e58913e63707fc1cfdde3bda2ff39f3", "nosuchglitch://auth");
            if (!glitch.hasAuthToken(context))
            {
                updateViews.setImageViewResource(R.id.skill_icon, R.drawable.icon);
                updateViews.setTextViewText(R.id.skill_title, context.getResources().getText(R.string.app_name));
                updateViews.setTextViewText(R.id.skill_description, context.getString(R.string.widget_unknown_glitch));
                
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                {
                    updateViews.setViewVisibility(R.id.skill_progress, View.INVISIBLE);
                    updateViews.setViewVisibility(R.id.skill_time_to_learn, View.INVISIBLE);
                    updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.INVISIBLE);
                }
                
                ComponentName thisWidget = new ComponentName(context, LearningWidget.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                manager.updateAppWidget(thisWidget, updateViews);

                return;
            }

            if (useNetwork || cachedSkill == null)
            {
                glitch.authorize(context, "read", new GlitchSessionDelegate()
                {

                    @Override
                    public void glitchLoginSuccess()
                    {

                        GlitchRequest req = glitch.getRequest("skills.listLearning");
                        req.execute(new GlitchRequestDelegate()
                        {

                            @Override
                            public void requestFinished(GlitchRequest request)
                            {

                                JSONObject response = request.response;
                                if (response == null)
                                    return;

                                if (response.optInt("ok") == 0)
                                    return;

                                JSONObject learning = request.response.optJSONObject("learning");
                                if (learning == null)
                                {
                                    // No skills are currently learning
                                    updateNotLearning(glitch);

                                    return;
                                }

                                String name = learning.names().optString(0);
                                if (name == null) return;
                                final JSONObject skill = learning.optJSONObject(name);

                                updateSkillText(updateViews, skill);
                                
                                resetLearningFinishedNotification(context, skill);
                                
                                LoadIconAsyncTask iconTask = new LoadIconAsyncTask(context, updateViews);
                                iconTask.execute(skill.optString("icon_100"));
                            }

                            private void resetLearningFinishedNotification(Context ctx, JSONObject skill)
                            {
                                if (skill != null && skill.has("name") && skill.has("time_complete"))
                                {
                                    String text = String.format(ctx.getString(R.string.widget_skill_s_finished_learning), skill.optString("name"));
                                    Intent alarm = new Intent(ctx, UpdateService.class);
                                    alarm.setAction("FinishedNotification");
                                    alarm.putExtra("text", text);
                                    PendingIntent pend = PendingIntent.getService(ctx, 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
                                    AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                                    am.cancel(pend);
                                    
                                    long timeComplete = skill.optLong("time_complete") * 1000;
                                    
                                    // Use for debugging notifications only
                                    // timeComplete = System.currentTimeMillis() + 2000;
                                    
                                    Date d = new Date(timeComplete);
                                    Log.i("GlitchSkills", "Scheduled notification at "+d.toLocaleString());
                                    
                                    am.set(AlarmManager.RTC_WAKEUP, timeComplete, pend);
                                } 
                            }

                            @Override
                            public void requestFailed(GlitchRequest request)
                            {
                            }

                            private void updateNotLearning(Glitch glitch)
                            {
                                GlitchRequest request = glitch.getRequest("players.info");
                                request.execute(new GlitchRequestDelegate()
                                {

                                    @Override
                                    public void requestFinished(GlitchRequest request)
                                    {
                                        JSONObject response = request.response;
                                        if (response == null)
                                            return;

                                        if (response.optInt("ok") == 0)
                                            return;

                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                                        {
                                            updateViews.setViewVisibility(R.id.skill_progress, View.INVISIBLE);
                                            updateViews.setViewVisibility(R.id.skill_time_to_learn, View.INVISIBLE);
                                            updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.INVISIBLE);
                                        }

                                        updateViews.setTextViewText(R.id.skill_title, response.optString("player_name"));
                                        updateViews.setTextViewText(R.id.skill_description, getString(R.string.widget_not_learning));

                                        LoadIconAsyncTask iconTask = new LoadIconAsyncTask(context, updateViews);
                                        iconTask.execute(response.optString("avatar_url"));
                                    }

                                    @Override
                                    public void requestFailed(GlitchRequest arg0)
                                    {
                                    }

                                });
                            }

                        });

                    }

                    @Override
                    public void glitchLoginFail()
                    {
                    }

                    @Override
                    public void glitchLoggedOut()
                    {
                    }
                });
            }
            else if (cachedSkill != null)
            {
                // Just display the cached values.
                updateSkillText(updateViews, cachedSkill);
                if (cachedIconBitmap != null)
                    updateIconAndUpdateAppWidget(context, updateViews, cachedIconBitmap);
            }
        }

        public void updateSkillText(final RemoteViews updateViews, JSONObject skill)
        {
            cachedSkill = skill;
            
            updateViews.setTextViewText(R.id.skill_title, skill.optString("name"));
            updateViews.setTextViewText(R.id.skill_description, skill.optString("description"));

            int timeRemaining = skill.optInt("time_remaining");

            if (timeRemaining != 0)
            {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                {
                    updateViews.setViewVisibility(R.id.skill_progress, View.VISIBLE);
                    updateViews.setViewVisibility(R.id.skill_time_to_learn, View.VISIBLE);
                    updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.VISIBLE);
                }
                
                long timeStart = skill.optLong("time_start");
                long timeComplete = skill.optLong("time_complete");
                
                long currentTime = System.currentTimeMillis() / 1000L;
                
                if (timeComplete < (currentTime + 60)) 
                {
                    // We must be nearly done. Clear the cache.
                    if (cachedIconBitmap != null) 
                    {
                        cachedIconBitmap.recycle();
                        cachedIconBitmap = null;
                    }
                    
                    cachedSkill = null;
                    return;
                }
                
                int elapsed = (int)(currentTime - timeStart);
                int total = (int)(timeComplete - timeStart);
                int remaining = total - elapsed;
                
                updateViews.setProgressBar(R.id.skill_progress, total, elapsed, false);
                updateViews.setTextViewText(R.id.skill_time_to_learn, SkillsAdapter.formatDuration(remaining, false));
            }
            else
            {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                {
                    updateViews.setViewVisibility(R.id.skill_progress, View.INVISIBLE);
                    updateViews.setViewVisibility(R.id.skill_time_to_learn, View.INVISIBLE);
                    updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.INVISIBLE);
                }
            }
        }

        public void updateIconAndUpdateAppWidget(Context context, RemoteViews updateViews, Bitmap iconBitmap)
        {
            if (iconBitmap != null)
                updateViews.setBitmap(R.id.skill_icon, "setImageBitmap", iconBitmap);

            ComponentName thisWidget = new ComponentName(context, LearningWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, updateViews);
        }
    }
}
