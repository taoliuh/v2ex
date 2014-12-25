/*
 * Copyright 2014 sonaive.com. All rights reserved.
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
package com.sonaive.v2ex.ui.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by liutao on 12/24/14.
 */
public class UrlImageParser implements Html.ImageGetter {
    Context context;
    TextView container;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param context
     * @param container
     */
    public UrlImageParser(Context context, TextView container) {
        this.context = context;
        this.container = container;
    }

    public Drawable getDrawable(String source) {
        UrlDrawable urlDrawable = new UrlDrawable();

        // get the actual source
        ImageGetterAsyncTask asyncTask =
                new ImageGetterAsyncTask(urlDrawable);

        asyncTask.execute(source);

        // return reference to UrlDrawable where I will change with actual image from
        // the src tag
        return urlDrawable;
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        UrlDrawable urlDrawable;

        public ImageGetterAsyncTask(UrlDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result == null) {
                return;
            }
            // set the correct bound according to the result from HTTP call
            urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());

            // change the reference of the current drawable to the result
            // from the HTTP call
            urlDrawable.drawable = result;

            // redraw the image by invalidating the container
            UrlImageParser.this.container.invalidate();

            // For ICS
            UrlImageParser.this.container.setHeight((UrlImageParser.this.container.getHeight()
                    + result.getIntrinsicHeight()));

            // Pre ICS
            UrlImageParser.this.container.setEllipsize(null);

            container.setText(container.getText());

        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        public Drawable fetchDrawable(String urlString) {

            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
            final float dpi = (int) metrics.density;
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");

                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();

                int scaledWidth = width;
                int scaledHeight = height;

                if (width > container.getMeasuredWidth()) {
                    scaledWidth = container.getMeasuredWidth();
                    scaledHeight = (int) (container.getMeasuredWidth() * height * 1.0f / width);
                }

                drawable.setBounds(0, 0, scaledWidth, scaledHeight);
                drawable.setVisible(true, true);

                return drawable;
            } catch (Exception e) {
                return null;
            }
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}
