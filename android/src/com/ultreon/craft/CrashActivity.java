package com.ultreon.craft;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class CrashActivity extends AppCompatActivity {
    @UnknownNullability
    private EditText editText;
    @UnknownNullability
    private ClipboardManager clipboard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_crash);

        this.editText = this.findViewById(R.id.crashLog);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            String crashLog = extras.getString("CrashLog");
            this.editText.setHorizontallyScrolling(true);
            this.editText.setHorizontalScrollBarEnabled(true);
            this.editText.setVerticalScrollBarEnabled(true);
            this.editText.setMovementMethod(new ScrollingMovementMethod());
            this.editText.setText(crashLog);
        }

        ActionBar supportActionBar = this.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Game Crashed :(");
        }

        this.clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.menu_crash, menu);
        MenuItem item = menu.findItem(R.id.action_copy_crashlog);
        item.setOnMenuItemClickListener(item1 -> {
            this.editText.getText();
            ClipData clip = ClipData.newPlainText("Crash Log", this.editText.getText());
            this.clipboard.setPrimaryClip(clip);
            return true;
        });
        return true;
    }
}