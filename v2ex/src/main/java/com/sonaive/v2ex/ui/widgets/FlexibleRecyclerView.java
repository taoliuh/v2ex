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
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.sonaive.v2ex.R;

/**
 * Created by liutao on 12/16/14.
 */
public class FlexibleRecyclerView extends RecyclerView {

    private int mContentTopClearance = 0;

    public FlexibleRecyclerView(Context context) {
        this(context, null, 0);
    }

    public FlexibleRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexibleRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            final TypedArray xmlArgs = context.obtainStyledAttributes(attrs,
                    R.styleable.FlexibleRecyclerView, defStyle, 0);
            mContentTopClearance = xmlArgs.getDimensionPixelSize(
                    R.styleable.FlexibleRecyclerView_topClearance, 0);
            xmlArgs.recycle();
        }
    }

    public void setContentTopClearance(int clearance) {
        if (mContentTopClearance != clearance) {
            mContentTopClearance = clearance;
            setPadding(getPaddingLeft(), mContentTopClearance,
                    getPaddingRight(), getPaddingBottom());
            getAdapter().notifyDataSetChanged();
        }
    }
}
