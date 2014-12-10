/*
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

package com.sonaive.v2ex.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.sonaive.v2ex.R;

/**
 * Bug still exist. See http://stackoverflow.com/questions/8235080/fragments-dialogfragment-and-screen-rotation
 * When screen orientation changes, and you call setRetainInstance(true) in function onCreate, the dialog will dismiss.
 *
 * Created by liutao on 12/8/14.
 */
public class SignInDialogFragment extends DialogFragment implements TextView.OnEditorActionListener {

    private AlertDialog dialog;

    EditText accountNameEditText;
    EditText passwordEditText;

    private String mAccountName = "";
    private String mPassword = "";

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SignInDialogListener {
        public void onDialogPositiveClick(String accountName, String password);
        public void onDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    SignInDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SignInDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mAccountName = savedInstanceState.getString("account_name");
            mPassword = savedInstanceState.getString("password");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_signin, null);
        accountNameEditText = (EditText) view.findViewById(R.id.account_name);
        passwordEditText = (EditText) view.findViewById(R.id.password);
        passwordEditText.setOnEditorActionListener(this);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        if (mListener != null) {
                            mListener.onDialogPositiveClick(mAccountName, mPassword);
                        }
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        dialog = builder.create();
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        validateSignInButton();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("account_name", mAccountName);
        outState.putString("password", mPassword);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            if (validateSignInButton()) {
                if (mListener != null) {
                    mListener.onDialogPositiveClick(mAccountName, mPassword);
                }
                this.dismiss();
            }
            return true;
        }
        return false;
    }

    private boolean validateSignInButton() {

        accountNameEditText.setText(mAccountName);
        passwordEditText.setText(mPassword);

        accountNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mAccountName = s.toString();
                if (TextUtils.isEmpty(mAccountName)) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    if (TextUtils.isEmpty(mPassword)) {
                        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPassword = s.toString();
                if (TextUtils.isEmpty(mPassword)) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    if (TextUtils.isEmpty(mAccountName)) {
                        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
        });

        if (!TextUtils.isEmpty(accountNameEditText.getText().toString()) && !TextUtils.isEmpty(passwordEditText.getText().toString())) {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        }
        return dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled();
    }
}