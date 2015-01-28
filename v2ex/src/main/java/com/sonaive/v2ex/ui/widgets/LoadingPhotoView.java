package com.sonaive.v2ex.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sonaive.v2ex.R;
import com.sonaive.v2ex.widget.LoadingStatus;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by liutao on 12/29/14.
 */
public class LoadingPhotoView extends FrameLayout {
    private PhotoView mPhotoView;
    private View mProgressBar;

    public LoadingPhotoView(Context context) {
        this(context, null, 0);
    }

    public LoadingPhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View root = inflate(context, R.layout.loading_image_view, this);
        mPhotoView = (PhotoView) root.findViewById(R.id.photo_view);
        mProgressBar = root.findViewById(R.id.progress_bar);
    }

    public ImageView getImageView() {
        return mPhotoView;
    }

    public void setLoadingState(LoadingStatus loadingStatus) {
        if (loadingStatus == LoadingStatus.LOADING) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
