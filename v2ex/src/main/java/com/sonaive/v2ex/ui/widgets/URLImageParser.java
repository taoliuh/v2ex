package com.sonaive.v2ex.ui.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;

import static com.sonaive.v2ex.util.LogUtils.LOGD;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

public class URLImageParser implements Html.ImageGetter {

    private static final String TAG = makeLogTag(URLImageParser.class);

    private TextView container;

    public URLImageParser(TextView v) {
        this.container = v;
    }

    @Override
    public Drawable getDrawable(final String url) {
        final UrlDrawable urlDrawable = new UrlDrawable();
        final String source = url;

        LOGD(TAG, "Url is: " + url);
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) container.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        final float dpi = (int) metrics.density;

        Drawable.Callback callback = new Drawable.Callback() {

            @Override
            public void invalidateDrawable(Drawable drawable) {
                container.invalidate();
            }

            @Override
            public void scheduleDrawable(Drawable drawable, Runnable runnable, long l) {
            }

            @Override
            public void unscheduleDrawable(Drawable drawable, Runnable runnable) {

            }
        };
        container.setTag(container.getId(), callback);

        Glide.with(container.getContext()).load(source)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String s, Target<GlideDrawable> glideDrawableTarget, boolean b) {
                        LOGD(TAG, "Error in Glide listener");
                        if (e != null) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> glideDrawableTarget, boolean b, boolean b2) {
                        return false;
                    }
                }).
                into(new ViewTarget<TextView, GlideDrawable>(container) {
                    @Override
                    public void onResourceReady(GlideDrawable d, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        int width = (int) (d.getIntrinsicWidth() * dpi);
                        int height = (int) (d.getIntrinsicHeight() * dpi);

                        int scaledWidth = width;
                        int scaledHeight = height;

                        if (width > container.getMeasuredWidth()) {
                            scaledWidth = container.getMeasuredWidth();
                            scaledHeight = (int) (container.getMeasuredWidth() * height * 1.0f / width);
                        }

                        d.setBounds(0, 0, scaledWidth, scaledHeight);
                        d.setVisible(true, true);

                        urlDrawable.setBounds(0, 0, scaledWidth, scaledHeight);
                        urlDrawable.setDrawable(d);
                        urlDrawable.setCallback((Drawable.Callback) container.getTag(container.getId()));
                        LOGD(TAG, "Listener ended " + width + ", " + height + ", source: " + source + ", animated? " + d.isAnimated() + ", " + d.getClass().getSimpleName());

                        if (d instanceof GifDrawable) {
                            LOGD(TAG, "Gif drawable ! animated? " + d.isAnimated() + ", " + (d.getCallback() == null));
                            d.setLoopCount(GlideDrawable.LOOP_FOREVER);
                            d.start();
                        }

                        container.setText(container.getText());
                    }

                });
        return urlDrawable;
    }
}