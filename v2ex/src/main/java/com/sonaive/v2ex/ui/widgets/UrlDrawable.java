package com.sonaive.v2ex.ui.widgets;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;


public class UrlDrawable extends Drawable implements Drawable.Callback {
    private GlideDrawable drawable;

    public UrlDrawable() {
        super();
    }

    @Override
    public void setAlpha(int alpha) {
        if (drawable != null) {
            drawable.setAlpha(alpha);
        }
    }

    public void setDrawable(GlideDrawable drawable) {
        if (this.drawable != null) {
            this.drawable.setCallback(null);
        }
        drawable.setCallback(this);
        this.drawable = drawable;

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (drawable != null) {
            drawable.setColorFilter(cf);
        }
    }

    @Override
    public int getOpacity() {
        if (drawable != null) {
            return drawable.getOpacity();
        }
        return 0;
    }


    @Override
    public void draw(Canvas canvas) {
        // override the draw to facilitate refresh function later
        if (drawable != null) {
            Paint p = new Paint();
            p.setColor(Color.GREEN);
            canvas.drawRect(drawable.getBounds(), p);
            drawable.draw(canvas);

            if (!drawable.isRunning()) {
                drawable.start();
            }
        }
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (getCallback() == null) {
            return;
        }
        getCallback().invalidateDrawable(drawable);
    }

    @Override
    public void scheduleDrawable(Drawable drawable, Runnable runnable, long l) {
        if (getCallback() == null) {
            return;
        }
        getCallback().scheduleDrawable(drawable, runnable, l);
    }

    @Override
    public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
        if (getCallback() == null) {
            return;
        }
        getCallback().unscheduleDrawable(drawable, runnable);
    }
}