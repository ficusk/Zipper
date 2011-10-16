package com.android.ficus.zipper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class ImportActivity extends Activity {

    private EditText mJsonDataView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.json_import);

        mJsonDataView = (EditText) findViewById(R.id.json_data);

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String jsonData = mJsonDataView.getText().toString();
                if (ClipperzCard.from(jsonData) == null) {
                    Toast.makeText(ImportActivity.this, R.string.parse_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Files.write(
                            new File(getFilesDir(), ZipperActivity.JSON_DATA_FILE), jsonData);
                } catch (IOException e) {
                    Toast.makeText(ImportActivity.this, R.string.save_error,
                            Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

}
