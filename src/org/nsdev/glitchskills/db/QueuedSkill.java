package org.nsdev.glitchskills.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class QueuedSkill
{
    @DatabaseField(id = true)
    String id;
    
    @DatabaseField
    int position;
    
    @DatabaseField
    boolean isUnlearning;
    
    QueuedSkill() {
    }

    public QueuedSkill(String id, int position)
    {
        this.id = id;
        this.position = position;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }
}
