package org.nsdev.glitchskills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SkillsAdapter extends BaseAdapter implements ListAdapter
{

    private static final int SECONDS_PER_DAY = 60 * 60 * 24;
    private static final int SECONDS_PER_HOUR = 60 * 60;
    private static final int SECONDS_PER_MINUTE = 60;

    JSONArray jsonArray;
    JSONObject obj;
    Context context;

    LayoutInflater vi;

    public SkillsAdapter(Context context, JSONObject obj)
    {
        super();
        this.jsonArray = obj.names();
        this.obj = obj;
        this.context = context;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public SkillsAdapter(Context context)
    {
        this.context = context;
        this.jsonArray = null;
        this.obj = null;
    }

    @Override
    public int getCount()
    {
        if (jsonArray == null)
            return 0;
        else
            return jsonArray.length();
    }

    public JSONObject getSkill(int index) throws JSONException
    {
        String name = (String)getItem(index);
        if (name != null)
        {
            return (JSONObject)obj.get(name);
        }

        return null;
    }

    @Override
    public Object getItem(int index)
    {
        try
        {
            return jsonArray.get(index);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int index)
    {
        try
        {
            return jsonArray.get(index).hashCode();
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }

    static class ViewHolder
    {
        public TextView title;
        public TextView description;
        public LoaderImageView icon;
        public ProgressBar progress;
        public TextView timeToLearn;
        public TextView timeToLearnLabel;
        public Bitmap bitmap;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            v = vi.inflate(R.layout.skills_list_item, null);
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView)v.findViewById(R.id.skill_title);
            holder.description = (TextView)v.findViewById(R.id.skill_description);
            holder.icon = (LoaderImageView)v.findViewById(R.id.skill_icon);
            holder.progress = (ProgressBar)v.findViewById(R.id.skill_progress);
            holder.timeToLearn = (TextView)v.findViewById(R.id.skill_time_to_learn);
            holder.timeToLearnLabel = (TextView)v.findViewById(R.id.skill_time_to_learn_label);
            v.setTag(holder);
        }

        try
        {
            JSONObject o = getSkill(index);
            ViewHolder holder = (ViewHolder)v.getTag();
            holder.title.setText(o.getString("name"));
            holder.description.setText(o.getString("description"));
            holder.icon.setImageDrawable(o.getString("icon_100"));

            int timeRemaining = o.optInt("time_remaining");

            if (timeRemaining != 0)
            {
                holder.progress.setVisibility(View.VISIBLE);
                holder.timeToLearn.setVisibility(View.VISIBLE);
                holder.timeToLearnLabel.setVisibility(View.VISIBLE);

                long timeStart = o.optLong("time_start");
                long timeComplete = o.optLong("time_complete");

                int elapsed;
                int total;
                int remaining;

                if (timeStart != 0 && timeComplete != 0)
                {
                    long currentTime = System.currentTimeMillis() / 1000L;

                    elapsed = (int)(currentTime - timeStart);
                    total = (int)(timeComplete - timeStart);
                    remaining = total - elapsed;
                }
                else
                {
                    total = o.getInt("total_time");
                    elapsed = total - timeRemaining;
                    remaining = timeRemaining;
                }

                holder.progress.setMax(total);
                holder.progress.setProgress(elapsed);
                holder.timeToLearn.setText(formatDuration(remaining, true));
            }
            else
            {
                holder.progress.setVisibility(View.GONE);
                holder.timeToLearn.setVisibility(View.GONE);
                holder.timeToLearnLabel.setVisibility(View.GONE);
            }
        }
        catch (JSONException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return v;
    }

    static StringBuffer formatBuffer = new StringBuffer();

    public synchronized static String formatDuration(int seconds, boolean includeSeconds)
    {
        int days = 0;
        int hours = 0;
        int minutes = 0;

        StringBuffer buffer = formatBuffer;
        buffer.setLength(0);

        if (seconds > SECONDS_PER_DAY)
        {
            days = seconds / SECONDS_PER_DAY;
            seconds = seconds - (days * SECONDS_PER_DAY);
            buffer.append(days);
            buffer.append(days > 1 ? " days" : " day");
        }
        if (seconds > SECONDS_PER_HOUR)
        {
            hours = seconds / SECONDS_PER_HOUR;
            seconds = seconds - (hours * SECONDS_PER_HOUR);
            if (days > 0)
                buffer.append(" ");
            buffer.append(hours);
            buffer.append(" hr");
        }
        if (seconds > SECONDS_PER_MINUTE)
        {
            minutes = seconds / SECONDS_PER_MINUTE;
            seconds = seconds - (minutes * SECONDS_PER_MINUTE);
            if (hours > 0 || days > 0)
                buffer.append(" ");
            buffer.append(minutes);
            buffer.append(" min");
        }
        if (seconds > 0 && includeSeconds)
        {
            if (minutes > 0 || hours > 0 || days > 0)
                buffer.append(" ");
            buffer.append(seconds);
            buffer.append(" sec");
        }
        return buffer.toString();
    }
}
