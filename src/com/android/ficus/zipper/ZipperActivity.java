package com.android.ficus.zipper;

import com.android.ficus.zipper.ClipperzCard.ClipperzField;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class ZipperActivity extends Activity {
    public static final String JSON_DATA_FILE = "clipperz.json";

    private ExpandableListView mListView;
    private ZipperAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mListView = (ExpandableListView) findViewById(R.id.main_list);

    }

    @Override
    protected void onResume() {
        super.onResume();
        String jsonData = Files.read(new File(getFilesDir(), JSON_DATA_FILE));
        if (jsonData == null) {
            goToImportActivity();
            return;
        }
        List<ClipperzCard> cards = ClipperzCard.from(jsonData);
        mAdapter = new ZipperAdapter(this, cards);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(new FieldClickListener());
    }

    private void goToImportActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ImportActivity.class);
        startActivity(intent);
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
            new File(getFilesDir(), JSON_DATA_FILE).delete();
            goToImportActivity();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

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