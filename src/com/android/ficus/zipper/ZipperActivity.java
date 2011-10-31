/*
 * Copyright (C) 2011 Ficus Kirkpatrick
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

package com.android.ficus.zipper;

import com.android.ficus.zipper.ClipperzCard.ClipperzField;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * The main activity that displays all the Clipperz cards.
 */
public class ZipperActivity extends Activity {
    /** List view of all cards; card is a group, each field is a child. */
    private ExpandableListView mListView;

    /** Our adapter for populating the ListView. */
    private ZipperAdapter mAdapter;

    private static String sCurrentPassword = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mListView = (ExpandableListView) findViewById(R.id.main_list);
    }

    private void loadData() {
        // TODO: Don't read and parse every time in onResume, use startActivityForResult
        // to open the import activity. Clear password in onActivityResult.
        File jsonDataFile = Files.getJsonDataFile(this);
        if (!jsonDataFile.exists()) {
            goToImportActivity();
            return;
        }

        // TODO: Bypass this if there is no password set?
        if (sCurrentPassword == null) {
            showDialog(PASSWORD_DIALOG);
            return;
        }

        String jsonData = Files.readEncrypted(jsonDataFile, sCurrentPassword);
        if (jsonData == null) {
            sCurrentPassword = null;
            showDialog(PASSWORD_DIALOG);
            return;
        }

        List<ClipperzCard> cards = ClipperzCard.from(jsonData);
        mAdapter = new ZipperAdapter(this, cards);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(new FieldClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadData();
    }

    private static final int PASSWORD_DIALOG = 1;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case PASSWORD_DIALOG:
                return makePasswordDialog();
            default:
                return super.onCreateDialog(id);
        }
    }

    /**
     * Makes the password dialog shown when we have saved data that needs to be decrypted.
     */
    private Dialog makePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set up our custom view.
        View passwordEntry = LayoutInflater.from(this).inflate(R.layout.password_dialog, null);
        builder.setView(passwordEntry);
        builder.setTitle(R.string.password_dialog_title);

        // OK button - set current password and try loading data. If data load
        // fails, the activity will just re-show the password dialog.
        Button okButton = (Button) passwordEntry.findViewById(R.id.ok_button);
        final EditText password = (EditText) passwordEntry.findViewById(R.id.password);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sCurrentPassword = password.getText().toString();
                password.setText("");
                dismissDialog(PASSWORD_DIALOG);
                loadData();
            }
        });

        // "Reset data" button - go to the import activity to reimport.
        Button resetDataButton = (Button) passwordEntry.findViewById(R.id.reset_data_button);
        resetDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToImportActivity();
                dismissDialog(PASSWORD_DIALOG);
            }
        });

        // Not cancelable; only OK and Reset do anything useful.
        builder.setCancelable(false);

        return builder.create();
    }

    private static final int REQUEST_IMPORT = 1;

    private void goToImportActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ImportActivity.class);
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK) {
            // Set the current password to the one the user just chose
            // in the import activity. onResume() will reload the data.
            sCurrentPassword = data.getStringExtra(ImportActivity.PASSWORD_EXTRA);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.clear_data:
            // Delete the data file on disk and go to the import activity.
            Files.getJsonDataFile(this).delete();
            goToImportActivity();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A ClickListener for fields that copies their value to the clipboard and displays a toast.
     */
    private class FieldClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                int childPosition, long id) {
            ClipperzField field = (ClipperzField) mAdapter.getChild(groupPosition, childPosition);
            ClipboardManager manager =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setText(field.value);
            Toast.makeText(ZipperActivity.this, R.string.copied_to_clipboard,
                    Toast.LENGTH_SHORT).show();
            return true;
        }

    }
}