package org.nsdev.glitchskills;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nsdev.glitchskills.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SkillTreeView extends View
{
    public class SkillBlock
    {
        String classId;
        int left;
        int top;
        int spriteX;
        int spriteY;
        boolean available = false;
        boolean have = false;
    }

    ArrayList<SkillBlock> blocks = new ArrayList<SkillBlock>();

    HashMap<String, SkillBlock> blockIndex = new HashMap<String, SkillBlock>();

    Bitmap sprites;
    Bitmap check;

    private float scale;

    public SkillTreeView(Context context)
    {
        super(context);
        initialize(context);
    }

    public SkillTreeView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initialize(context);
    }

    public SkillTreeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize(context);
    }

    private void initialize(Context context)
    {
        XmlResourceParser parser = context.getResources().getXml(R.xml.skill_tree);

        try
        {
            int eventType = -1;
            while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if ("skill".equals(parser.getName()))
                    {
                        SkillBlock block = new SkillBlock();
                        block.classId = parser.getAttributeValue(null, "classId");
                        block.left = parser.getAttributeIntValue(null, "left", 0);
                        block.top = parser.getAttributeIntValue(null, "top", 0);
                        block.spriteX = parser.getAttributeIntValue(null, "spriteX", 0);
                        block.spriteY = parser.getAttributeIntValue(null, "spriteY", 0);
                        blocks.add(block);

                        blockIndex.put(block.classId, block);
                    }
                }
            }
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        sprites = BitmapFactory.decodeResource(getResources(), R.drawable.skill_table_73379);
        check = BitmapFactory.decodeResource(getResources(), R.drawable.check_tiny_stroke);
    }

    @Override
    public void draw(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(0xFF00FFFF);
        paint.setStrokeWidth(1);
        paint.setStyle(Style.STROKE);

        canvas.scale(scale, scale);

        for (SkillBlock block: blocks)
        {
            if (block.available || block.have)
                paint.setAlpha(0xFF);
            else
                paint.setAlpha(0x7F);

            Rect toRect = new Rect(block.left, block.top, block.left + 40, block.top + 40);
            canvas.drawBitmap(sprites, new Rect(block.spriteX, block.spriteY, block.spriteX + 40, block.spriteY + 40), toRect, paint);

            Rect availRect = new Rect(block.left + 2, block.top + 2, block.left + 42, block.top + 42);
            if (block.available)
            {
                canvas.drawRect(availRect, paint);
            }
            if (block.have)
            {
                Rect checkRect = new Rect(block.left + 40 - 12, block.top + 40 - 12, block.left + 40, block.top + 40);
                canvas.drawBitmap(check, new Rect(0, 0, 12, 12), checkRect, paint);
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // int availableWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableHeight = (int)(MeasureSpec.getSize(heightMeasureSpec) * 8 / 10f);

        int width = 0;
        int height = 0;

        for (SkillBlock block: blocks)
        {
            if (width < block.left + 43)
                width = block.left + 43;
            if (height < block.top + 43)
                height = block.top + 43;
        }

        scale = (float)availableHeight / (float)height;

        setMeasuredDimension((int)(width * scale), (int)(height * scale));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);
        return true;
    }

    public void setLearned(JSONObject response)
    {
        if (Constants.DEBUG)
            Log.e("SkillTreeView", "Got Learned Response");

        for (SkillBlock block: blocks)
        {
            block.have = false;
        }

        // Go through the skills, and update our data structure.
        try
        {
            JSONObject skills = response.getJSONObject("skills");
            JSONArray names = skills.names();
            for (int i = 0; i < names.length(); i++)
            {
                String id = names.getString(i);

                SkillBlock block = blockIndex.get(id);
                if (block != null)
                {
                    block.have = true;
                }
            }
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            if (Constants.DEBUG)
                e.printStackTrace();
        }

        postInvalidate();

    }

    public void setAvailable(JSONObject response)
    {
        if (Constants.DEBUG)
            Log.e("SkillTreeView", "Got Available Response");

        for (SkillBlock block: blocks)
        {
            block.available = false;
        }

        try
        {
            JSONObject skills = response.getJSONObject("skills");
            JSONArray names = skills.names();
            for (int i = 0; i < names.length(); i++)
            {
                String id = names.getString(i);

                SkillBlock block = blockIndex.get(id);
                if (block != null)
                {
                    block.available = true;
                }
            }
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            if (Constants.DEBUG)
                e.printStackTrace();
        }

        postInvalidate();
    }

}
