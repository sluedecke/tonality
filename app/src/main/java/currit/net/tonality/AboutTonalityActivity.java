package currit.net.tonality;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class AboutTonalityActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_about_tonality);

        WebView wv = (WebView) findViewById(R.id.about_tonality);
        wv.loadUrl("file:///android_asset/" + getResources().getString(R.string.about_tonality_file));
        wv.setBackgroundColor(Color.TRANSPARENT);

        // Hide UI
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // hide notification bar
    }

    @Override public void onBackPressed() {
        finish();
    }

    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
