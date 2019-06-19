package net.currit.tonality;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;
import androidx.databinding.DataBindingUtil;

import net.currit.tonality.databinding.ActivityTonalityMainBinding;
import net.currit.tonality.databinding.PopupSizingBinding;
import mn.tck.semitone.PianoEngine;

public class TonalityMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityTonalityMainBinding activityBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_tonality_main, null, false);
        activityBinding.setPiano(activityBinding.piano);

        setContentView(activityBinding.getRoot());

        PianoEngine.create(this);

        // initialize our PianoView
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int concert_a;
        try {
            concert_a = Integer.parseInt(sp.getString("concert_a", "440"));
        } catch (NumberFormatException e) {
            concert_a = 440;
        }
        activityBinding.piano.setup(concert_a,
                sp.getBoolean("sustain", false),
                sp.getBoolean("labelnotes", true),
                sp.getBoolean("labelc", true)
        );

        // setup scale UI elements
        final PianoControlScale scaleController;
        scaleController = (PianoControlScale) getSupportFragmentManager().findFragmentById(R.id.piano_control_scale);
        scaleController.setPiano(activityBinding.piano);

        // configure popup_sizing popup
        PopupSizingBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.popup_sizing, null, false);
        final PopupWindow popup = new PopupWindow(binding.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popup.setElevation(20);
        }
        binding.setPiano(activityBinding.piano);
        binding.setPopup(popup);
        final View sizingButton = findViewById(R.id.button_sizing);
        sizingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popup.isShowing())
                    popup.dismiss();
                else
                    // TODO: 2019-05-27 close with back
                    popup.showAtLocation(sizingButton, Gravity.CENTER, 0, 0);
            }
        });


        // configure menu/more button
        final View moreButton = findViewById(R.id.button_more);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(TonalityMainActivity.this, moreButton);
                popupMenu.inflate(R.menu.tonality_menu);
                // TODO: 2019-05-28 divider do not show ...
                MenuCompat.setGroupDividerEnabled(popupMenu.getMenu(), true);

                // enable/disable menu entries
                Menu m = popupMenu.getMenu();
                m.findItem(R.id.menu_switch_labelnotes).setChecked(activityBinding.piano.getLabelNotes());
                m.findItem(R.id.menu_switch_labelc).setChecked(activityBinding.piano.getLabelC()).setEnabled(activityBinding.piano.getLabelNotes());
                m.findItem(R.id.menu_switch_circleoffifths).setChecked(scaleController.isUseCircleOfFifthSelector());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_about:
                                startActivity(new Intent(getApplicationContext(), AboutTonalityActivity.class));
                                return true;
                            case R.id.menu_switch_labelnotes:
                                activityBinding.piano.toggleLabelNotes();
                                return true;
                            case R.id.menu_switch_labelc:
                                activityBinding.piano.toggleLabelC();
                                return true;
                            case R.id.menu_switch_circleoffifths:
                                scaleController.toggleCircleOfFifthsSelector();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        // Hide UI
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // hide notification bar
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PianoEngine.destroy();
    }
}
