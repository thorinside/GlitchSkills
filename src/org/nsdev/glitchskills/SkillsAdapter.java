package org.nsdev.glitchskills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.github.droidfu.widgets.WebImageView;

public class SkillsAdapter extends BaseAdapter
{

    private static final int SECONDS_PER_DAY = 60 * 60 * 24;
    private static final int SECONDS_PER_HOUR = 60 * 60;
    private static final int SECONDS_PER_MINUTE = 60;

    Context context;
    
    private static class NameIndexEntry 
    {
        private String name;
        private int originalIndex;
        
        public NameIndexEntry(String name, int originalIndex)
        {
            super();
            setName(name);
            setOriginalIndex(originalIndex);
        }

        public void setName(String name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return name;
        }
        
        public void setOriginalIndex(int originalIndex)
        {
            this.originalIndex = originalIndex;
        }
        
        public int getOriginalIndex()
        {
            return originalIndex;
        }
    }

    LayoutInflater vi;
    
    public static class SkillsListEntry {
        int entryType = -1;
        SkillsCategory category;
        int categoryIndex;
        String entryText;
    }
    
    public static class SkillsCategory implements Comparable<SkillsCategory> {
        final boolean isTitleVisible;
        final String title;
        final JSONObject obj;
        final JSONArray jsonArray;
        final private ArrayList<NameIndexEntry> nameIndex = new ArrayList<NameIndexEntry>();
        final int order;
        final int action;
        final boolean hasAction;
        
        public SkillsCategory(JSONObject obj, boolean isTitleVisible, String title, boolean hasAction, int action, int order)
        {
            this.order = order;
            this.jsonArray = obj.names();
            this.obj = obj;
            this.title = title;
            this.isTitleVisible = isTitleVisible;
            this.hasAction = hasAction;
            this.action = action;
            
            if (jsonArray != null)
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    try
                    {
                        String name = (String)jsonArray.get(i);
                        nameIndex.add(new NameIndexEntry(name, i));
                    } 
                    catch (JSONException ex)
                    {
                    }
                }
            }
            
            Collections.sort(nameIndex, new Comparator<NameIndexEntry>()
            {
                @Override
                public int compare(NameIndexEntry a, NameIndexEntry b)
                {
                    return a.getName().compareTo(b.getName());
                }
            });
            
        }

        public int getCount()
        {
            if (jsonArray == null)
                return 0;

            return jsonArray.length();
        }
        
        public JSONObject getSkill(int index) throws JSONException
        {
            String name = (String)jsonArray.get(nameIndex.get(index).getOriginalIndex());
            if (name != null)
            {
                return (JSONObject)obj.get(name);
            }
            return null;
        }

        @Override
        public int compareTo(SkillsCategory another)
        {
            if (order < another.order) return -1;
            if (order > another.order) return 1;
            return 0;
        }
    }
    
    private ArrayList<SkillsCategory> skillsCategories = new ArrayList<SkillsCategory>();
    
    private ArrayList<SkillsListEntry> entries = new ArrayList<SkillsListEntry>();

    public SkillsAdapter(Context context)
    {
        super();
        this.context = context;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void reset(boolean notify)
    {
        entries.clear();
        skillsCategories.clear();
        if (notify) notifyDataSetChanged();
    }
    
    public void addSkillsCategory(SkillsCategory category)
    {
        skillsCategories.add(category);
        Collections.sort(skillsCategories);
        rebuildEntries();
        notifyDataSetChanged();
    }
    
    private void rebuildEntries()
    {
        entries.clear();
        
        for (SkillsCategory category: skillsCategories)
        {
            if (category.getCount() > 0)
            {
                if (category.isTitleVisible) {
                    SkillsListEntry entry = new SkillsListEntry();
                    entry.entryType = 0;
                    entry.category = category;
                    entry.entryText = category.title;
                    entries.add(entry);
                }
                for (int i = 0; i < category.getCount(); i++) {
                    SkillsListEntry entry = new SkillsListEntry();
                    entry.entryType = 1;
                    entry.category = category;
                    entry.categoryIndex = i;
                    entries.add(entry);
                }
            }
        }
    }

    @Override
    public int getCount()
    {
        return entries.size();
    }
    
    public JSONObject getSkill(int index)
    {
        SkillsListEntry entry = (SkillsListEntry)getItem(index);
        if (entry.entryType == 0) throw new RuntimeException("Not a skill entry!");
        try
        {
            return entry.category.getSkill(entry.categoryIndex);
        }
        catch (JSONException e)
        {
        }
        return null;
    }
    
    @Override
    public Object getItem(int index)
    {
        if (entries.isEmpty()) return null;
        return entries.get(index);
    }

    @Override
    public long getItemId(int index)
    {
        return getItem(index).hashCode();
    }

    static class ViewHolder
    {
        public TextView title;
        public TextView description;
        public WebImageView icon;
        public ProgressBar progress;
        public TextView timeToLearn;
        public TextView timeToLearnLabel;
    }
    
    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public int getItemViewType(int position)
    {
        return entries.get(position).entryType;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public boolean isEnabled(int position)
    {
        SkillsListEntry entry = (SkillsListEntry)getItem(position);
        if (entry != null)
        {
            if (entry.category.hasAction) return true;
        }
        return false;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent)
    {
        View v = convertView;
        SkillsListEntry entry = (SkillsListEntry)getItem(index);
        
        if (entry == null) return null;
        
        if (entry.entryType == 0)
        {
            if (v == null)
            {
                v = vi.inflate(R.layout.skills_list_header, null);
                ViewHolder holder = new ViewHolder();
                holder.title = (TextView)v.findViewById(R.id.text1);
                v.setTag(holder);
            }
            
            ViewHolder holder = (ViewHolder)v.getTag();
            holder.title.setText(entry.entryText);
        }
        else
        {
            if (v == null)
            {
                v = vi.inflate(R.layout.skills_list_item, null);
                ViewHolder holder = new ViewHolder();
                holder.title = (TextView)v.findViewById(R.id.skill_title);
                holder.description = (TextView)v.findViewById(R.id.skill_description);
                holder.icon = (WebImageView)v.findViewById(R.id.skill_icon);
                holder.progress = (ProgressBar)v.findViewById(R.id.skill_progress);
                holder.timeToLearn = (TextView)v.findViewById(R.id.skill_time_to_learn);
                holder.timeToLearnLabel = (TextView)v.findViewById(R.id.skill_time_to_learn_label);
                v.setTag(holder);
            }
    
            try
            {
                JSONObject o = entry.category.getSkill(entry.categoryIndex);
                ViewHolder holder = (ViewHolder)v.getTag();
                holder.title.setText(o.getString("name"));
                holder.description.setText(o.getString("description"));
                holder.icon.setImageUrl(o.getString("icon_100"));
                holder.icon.loadImage();
    
                int timeRemaining = o.optInt("time_remaining");
                
                boolean isUnlearnSkill = o.has("unlearn_quest_removal");
    
                if (timeRemaining != 0 || isUnlearnSkill)
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
                        
                        if (isUnlearnSkill && timeRemaining != 0)
                        {
                            o.put("time_start", System.currentTimeMillis() / 1000L);
                            o.put("time_complete", System.currentTimeMillis() / 1000L + timeRemaining);
                        }
                        else if (isUnlearnSkill && timeRemaining == 0)
                        {
                            remaining = total;
                            elapsed = 0;
                        }
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
                e1.printStackTrace();
            }
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
