package net.currit.tonality;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import net.currit.tonality.databinding.PopupScaleBinding;


public class PianoControlScale extends Fragment {

    private static final String PREF_CIRCLEOFFIFTHSSELECTOR = "circleoffifths_selector";
    private static final Boolean PREF_CIRCLEOFFIFTHSSELECTOR_DEFAULT = true;

    private TonalityMainActivity activity;
    private PopupWindow popup;

    private Button rootNoteButton;
    private Button scaleNameButton;

    private String[] noteNames;
    private String[] scaleNames;

    private boolean useCircleOfFifthSelector;
    private TonalityPianoView piano;

    public PianoControlScale() {
        // Required empty public constructor
    }

    public void setPiano(TonalityPianoView piano) {
        this.piano = piano;
        rootNoteButton.setText(noteNames[piano.rootNote]);
        rootNoteButton.setEnabled(piano.scale != 0);
        scaleNameButton.setText(scaleNames[piano.scale]);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TonalityMainActivity)
            activity = (TonalityMainActivity) context;
        noteNames = getResources().getStringArray(R.array.noteNames);
        scaleNames = getResources().getStringArray(R.array.scaleNames);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_piano_control_scale, container, false);

        // setup root note selection dialogue
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        useCircleOfFifthSelector = sp.getBoolean(PREF_CIRCLEOFFIFTHSSELECTOR, PREF_CIRCLEOFFIFTHSSELECTOR_DEFAULT);
        setupNoteSelector(view);

        // setup scale dialog
        scaleNameButton = view.findViewById(R.id.button_scale);
        scaleNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.title_scale_name);

                builder.setItems(scaleNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        piano.setScale(item);
                        scaleNameButton.setText(scaleNames[item]);
                        rootNoteButton.setEnabled(item != 0);
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return view;
    }

    public void toggleCircleOfFifthsSelector() {
        useCircleOfFifthSelector = !useCircleOfFifthSelector;

        // store in preferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(PREF_CIRCLEOFFIFTHSSELECTOR, useCircleOfFifthSelector);
        editor.apply();

        setupNoteSelector(getView());
    }

    private void setupNoteSelector(View rootView) {

        // setup root note dialog
        rootNoteButton = rootView.findViewById(R.id.button_root_note);

        if (useCircleOfFifthSelector) {
            // configure scale popup
            final PopupScaleBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.popup_scale, null, false);
            popup = new PopupWindow(binding.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            binding.setPopup(popup);
            binding.setHandler(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popup.setElevation(20);
            }
            rootNoteButton = rootView.findViewById(R.id.button_root_note);
            rootNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (popup.isShowing())
                        popup.dismiss();
                    else {
                        // late binding of piano, because during onCreateView it is not yet known to us
                        binding.setPiano(piano);

                        // TODO: 2019-05-27 close with back
                        popup.showAtLocation(rootNoteButton, Gravity.CENTER, 0, 0);
                    }
                }
            });
        } else {
            rootNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.title_root_note);

                    builder.setItems(noteNames, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            piano.setRoot(item);
                            rootNoteButton.setText(noteNames[item]);
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }

    public boolean isUseCircleOfFifthSelector() {
        return useCircleOfFifthSelector;
    }

    public void setRoot(int newRoot) {
        piano.setRoot(newRoot);
        popup.dismiss();
        rootNoteButton.setText(noteNames[piano.rootNote]);
    }
}
