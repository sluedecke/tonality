package currit.net.tonality;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;


public class PianoControlScale extends PianoControlFragment {

    TonalityMainActivity activity;
    Button rootNote;
    Button scaleName;

    String[] noteNames;
    String[] scaleNames;


    public PianoControlScale() {
        // Required empty public constructor
    }

    @Override
    public void setPiano(TonalityPianoView piano) {
        super.setPiano(piano);
        rootNote.setText(noteNames[piano.rootNote]);
        scaleName.setText(scaleNames[piano.scale]);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TonalityMainActivity)
            activity = (TonalityMainActivity) context;
        noteNames = getResources().getStringArray(R.array.noteNames);
        scaleNames = getResources().getStringArray(R.array.scaleNames);
        // TODO: 2019-05-26 set scaleNames based on orientation
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_piano_control_scale, container, false);

        // setup root note dialog
        rootNote = view.findViewById(R.id.button_root_note);
        rootNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.title_root_note);

                builder.setItems(noteNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        piano.setScale(piano.scale, item);
                        rootNote.setText(noteNames[item]);
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        // setup scale dialog
        scaleName = view.findViewById(R.id.button_scale);
        scaleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.title_scale_name);

                builder.setItems(scaleNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        piano.setScale(item, piano.rootNote);
                        scaleName.setText(scaleNames[item]);
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return view;
    }
}
