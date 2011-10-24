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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

/**
 * The activity for importing the Clipperz JSON data.
 */
public class ImportActivity extends Activity {
    /** The text field for pasting the JSON data into. */
    private EditText mJsonDataView;

    /** The text field containing the password to encrypt the card file with. */
    private EditText mPasswordView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.json_import);

        mJsonDataView = (EditText) findViewById(R.id.json_data);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String jsonData = mJsonDataView.getText().toString();

                // If the data pasted in by the user can't be parsed, show a toast
                // and do nothing.
                if (ClipperzCard.from(jsonData) == null) {
                    Toast.makeText(ImportActivity.this, R.string.parse_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Otherwise, write the JSON data to our local file and finish.
                try {
                    Files.writeEncrypted(
                            Files.getJsonDataFile(ImportActivity.this),
                            jsonData,
                            mPasswordView.getText().toString());
                } catch (IOException e) {
                    Toast.makeText(ImportActivity.this, R.string.save_error,
                            Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

}
