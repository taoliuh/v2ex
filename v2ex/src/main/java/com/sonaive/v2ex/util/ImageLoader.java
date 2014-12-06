/*
 * Copyright 2014 Google Inc.
 * Copyright 2014 so_naive. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sonaive.v2ex.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestListener;
import com.sonaive.v2ex.R;

import static com.sonaive.v2ex.util.LogUtils.makeLogTag;

public class ImageLoader {
    private static final String TAG = makeLogTag(ImageLoader.class);

    private final RequestManager requestManager;
    private final CenterCrop mCenterCrop;
    private final FitCenter mFitCenter;

    private int mPlaceHolderResId = -1;

    /**
     * Construct a standard ImageLoader object.
     */
    public ImageLoader(Context context) {
        requestManager = Glide.with(context);
        mCenterCrop = new CenterCrop(Glide.get(context).getBitmapPool());
        mFitCenter = new FitCenter(Glide.get(context).getBitmapPool());
    }

    /**
     * Construct an ImageLoader with a default placeholder drawable.
     */
    public ImageLoader(Context context, int placeHolderResId) {
        this(context);
        mPlaceHolderResId = placeHolderResId;
    }

    /**
     * Load an image from a url into an ImageView using the default placeholder
     * drawable if available.
     * @param url The web URL of an image.
     * @param imageView The target ImageView to load the image into.
     * @param requestListener A listener to monitor the request result.
     */
    public void loadImage(String url, ImageView imageView, RequestListener<String, Bitmap> requestListener) {
        loadImage(url, imageView, requestListener, null, false);
    }

    /**
     * Load an image from a url into an ImageView using the given placeholder drawable.
     *
     * @param url The web URL of an image.
     * @param imageView The target ImageView to load the image into.
     * @param requestListener A listener to monitor the request result.
     * @param placholderOverride A placeholder to use in place of the default placholder.
     */
    public void loadImage(String url, ImageView imageView, RequestListener<String, Bitmap> requestListener,
            Drawable placholderOverride) {
        loadImage(url, imageView, requestListener, placholderOverride, false /*crop*/);
    }

    /**
     * Load an image from a url into an ImageView using the default placeholder
     * drawable if available.
     * @param url The web URL of an image.
     * @param imageView The target ImageView to load the image into.
     * @param requestListener A listener to monitor the request result.
     * @param placeholderOverride A drawable to use as a placeholder for this specific image.
     *                            If this parameter is present, {@link #mPlaceHolderResId}
     *                            if ignored for this request.
     */
    public void loadImage(String url, ImageView imageView, RequestListener<String, Bitmap> requestListener,
                Drawable placeholderOverride, boolean crop) {
        BitmapRequestBuilder request = beginImageLoad(url, requestListener, crop)
                .animate(R.anim.image_fade_in);
        if (placeholderOverride != null) {
            request.placeholder(placeholderOverride);
        } else if (mPlaceHolderResId != -1) {
            request.placeholder(mPlaceHolderResId);
        }
        request.into(imageView);
    }

    public BitmapRequestBuilder beginImageLoad(String url,
            RequestListener<String, Bitmap> requestListener, boolean crop) {
        return requestManager.load(url)
                .asBitmap()
                .listener(requestListener)
                .transform(crop ? mCenterCrop : mFitCenter);
    }

    /**
     * Load an image from a url into the given image view using the default placeholder if
     * available.
     * @param url The web URL of an image.
     * @param imageView The target ImageView to load the image into.
     */
    public void loadImage(String url, ImageView imageView) {
        loadImage(url, imageView, false /*crop*/);
    }

    /**
     * Load an image from a url into an ImageView using the default placeholder
     * drawable if available.
     * @param url The web URL of an image.
     * @param imageView The target ImageView to load the image into.
     * @param crop True to apply a center crop to the image.
     */
    public void loadImage(String url, ImageView imageView, boolean crop) {
        loadImage(url, imageView, null, null, crop);
    }
}
