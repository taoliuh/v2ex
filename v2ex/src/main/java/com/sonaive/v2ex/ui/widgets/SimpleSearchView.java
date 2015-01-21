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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sonaive.v2ex.R;

/**
 * Created by liutao on 1/4/15.
 */
public class SimpleSearchView extends FrameLayout {

    private OnQueryListener mListener;

    EditText searchBox;
    ImageButton clearButton;

    public SimpleSearchView(Context context) {
        this(context, null);
    }

    public SimpleSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.search_box, this);
        searchBox = (EditText) view.findViewById(R.id.editText);
        clearButton = (ImageButton) view.findViewById(R.id.clearButton);
        clearButton.setVisibility(View.GONE);
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchBox.setText("");
                if (mListener != null) {
                    mListener.onClear();
                }
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.length() > 0) {
                    clearButton.setVisibility(View.VISIBLE);
                } else {
                    clearButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mListener != null) {
                    mListener.onQueryTextChange(s.toString().trim());
                }
            }
        });
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (mListener != null) {
                        mListener.onSubmit(v.getText().toString().trim());
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void setOnQueryListener(OnQueryListener listener) {
        mListener = listener;
    }

    public void setHint(int hintResId) {
        if (searchBox != null) {
            searchBox.setHint(hintResId);
        }
    }

    public void setText(String text) {
        if (searchBox != null) {
            searchBox.setText(text);
        }
    }

    public String getText() {
        if (searchBox != null) {
            return searchBox.getText().toString().trim();
        } else {
            return "";
        }
    }
}
