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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The activity for importing card data.
 */
public class ImportActivity extends Activity {
    private static final boolean DEBUG = false;

    public static final String PASSWORD_EXTRA = "password";

    /** The text field for pasting the data into. */
    private EditText mDataView;

    /** The text field containing the password to encrypt the card file with. */
    private EditText mPasswordView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_data);

        mDataView = (EditText) findViewById(R.id.data);
        mPasswordView = (EditText) findViewById(R.id.password);

        // Set up the show/hide password checkbox.
        setupShowPasswordCheckbox();

        // Set up the Save button.
        setupSaveButton();

        // Check to see if the user already has valid data in the clipboard.
        checkClipboardForData();
    }

    private void checkClipboardForData() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final CharSequence clipboardText = clipboard.getText();

        // Just bail if there's no text in the clipboard.
        if (clipboardText == null) {
            return;
        }

        int useDataMessageId = -1;
        if (ClipperzFormat.from(clipboardText.toString()) != null) {
            useDataMessageId = R.string.use_clipboard_data_message_clipperz;
        } else if (LastPassFormat.from(clipboardText.toString()) != null) {
            useDataMessageId = R.string.use_clipboard_data_message_lastpass;
        } else {
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.use_clipboard_data_title)
            .setMessage(useDataMessageId)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDataView.setText(clipboardText);
                }
            })
            .setNegativeButton(R.string.no, null)
            .create();
        dialog.show();
    }

    private void setupShowPasswordCheckbox() {
        CheckBox passwordCheckBox = (CheckBox) findViewById(R.id.show_password_checkbox);
        passwordCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Get the existing input type, minus any password flags.
                int newType = mPasswordView.getInputType();
                newType &= ~InputType.TYPE_TEXT_VARIATION_PASSWORD;
                newType &= ~InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

                // Pick which password flag to set.
                int whichPasswordType = isChecked
                        ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : InputType.TYPE_TEXT_VARIATION_PASSWORD;

                // Add it to the flag set and set the new type.
                newType |= whichPasswordType;
                mPasswordView.setInputType(newType);
            }
        });
    }

    private void setupSaveButton() {
        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = mDataView.getText().toString();
                String password = mPasswordView.getText().toString();

                // If the data pasted in by the user can't be parsed, show a toast
                // and do nothing.
                if (Card.from(data) == null) {
                    Toast.makeText(ImportActivity.this, R.string.parse_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Otherwise, write the data to our local file and finish.
                try {
                    Files.writeEncrypted(
                            Files.getCardDataFile(ImportActivity.this),
                            data, password);
                } catch (IOException e) {
                    Toast.makeText(ImportActivity.this, R.string.save_error,
                            Toast.LENGTH_SHORT).show();
                }
                Intent result = new Intent();
                result.putExtra(PASSWORD_EXTRA, password);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!DEBUG) {
            return false;
        }
        @SuppressWarnings("unused") // shut up compiler when DEBUG == false

        MenuItem useClipperzItem = menu.add("Use Clipperz data");
        useClipperzItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mDataView.setText(getDebugData(R.raw.debug_json));
                return true;
            }
        });

        MenuItem useLastpassItem = menu.add("Use LastPass data");
        useLastpassItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mDataView.setText(getDebugData(R.raw.debug_lastpass));
                return true;
            }
        });

        return true;
    }

    private String getDebugData(int resId) {
        try {
            InputStream debugData = getResources().openRawResource(resId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = debugData.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
            }
            return new String(baos.toByteArray());
        } catch (IOException e) {
            return "";
        }
    }
}
