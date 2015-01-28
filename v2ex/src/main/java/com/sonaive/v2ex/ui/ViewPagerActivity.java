package com.sonaive.v2ex.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sonaive.v2ex.R;
import com.sonaive.v2ex.ui.widgets.HackyViewPager;
import com.sonaive.v2ex.ui.widgets.LoadingPhotoView;
import com.sonaive.v2ex.util.ImageLoader;
import com.sonaive.v2ex.widget.LoadingStatus;

import uk.co.senab.photoview.PhotoViewAttacher;

import static com.sonaive.v2ex.util.LogUtils.LOGV;
import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

/**
 * Created by liutao on 12/29/14.
 */
public class ViewPagerActivity extends BaseActivity {
    private static final String TAG = makeLogTag(ViewPagerActivity.class);
    private static final String ARG_POSTERS = "posters";
    private static final String ARG_CURRENT_ITEM = "current_item";

    private ViewPager mViewPager;
    private String[] posters;
    private int currentItem = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        setContentView(mViewPager);

        if (savedInstanceState != null) {
            posters = savedInstanceState.getStringArray(ARG_POSTERS);
            currentItem = savedInstanceState.getInt(ARG_CURRENT_ITEM);
        } else {
            posters = getIntent().getStringArrayExtra("posters");
            currentItem = getIntent().getIntExtra("current_item", 0);
        }

        mViewPager.setAdapter(new FullSizePosterPagerAdapter(this, posters));
        mViewPager.setCurrentItem(currentItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(ARG_POSTERS, posters);
        outState.putInt(ARG_CURRENT_ITEM, currentItem);
        super.onSaveInstanceState(outState);
    }

    static class FullSizePosterPagerAdapter extends PagerAdapter {

        final String[] posters;
        Context mContext;
        ImageLoader mImageLoader;

        public FullSizePosterPagerAdapter(Context context, String[] posters) {
            mContext = context;
            mImageLoader = new ImageLoader(context);
            this.posters = posters;
        }

        @Override
        public int getCount() {
            return posters.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final LoadingPhotoView photoView = new LoadingPhotoView(container.getContext());

            PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(photoView.getImageView());
            photoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v2) {
                    ((BaseActivity) mContext).finish();
                }
            });
            mImageLoader.loadImage(posters[position].trim(), photoView.getImageView(), new RequestListener<String, Bitmap>() {
                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    photoView.setLoadingState(LoadingStatus.LOAD_FAILED);
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    photoView.setLoadingState(LoadingStatus.FINISH);
                    return false;
                }
            });

            LOGV(TAG, "Loading image, position is: " + position + ", source is: " + posters[position]);

            // Now just add PhotoView to ViewPager and return it
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(photoView, 0, params);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
