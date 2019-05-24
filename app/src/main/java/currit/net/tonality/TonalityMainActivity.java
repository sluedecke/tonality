package currit.net.tonality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import mn.tck.semitone.PianoEngine;

public class TonalityMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_tonality_main);

        PianoEngine.create(this);

        TonalityPianoView piano = findViewById(R.id.piano);

        // initialize our PianoView
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int concert_a;
        try {
            concert_a = Integer.parseInt(sp.getString("concert_a", "440"));
        } catch (NumberFormatException e) {
            concert_a = 440;
        }

        piano.setup(concert_a,
                sp.getBoolean("sustain", false),
                sp.getBoolean("labelnotes", true),
                sp.getBoolean("labelc", true)
        );

        PianoControlFragment cf = (PianoControlFragment)getSupportFragmentManager().findFragmentById(R.id.piano_control_keys);
        cf.setPiano(piano);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PianoEngine.destroy();
    }
}
