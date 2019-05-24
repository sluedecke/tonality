package currit.net.tonality;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import currit.net.tonality.databinding.FragmentPianoControlKeysBinding;


public class PianoControlKeys extends PianoControlFragment {
    private FragmentPianoControlKeysBinding binding;

    public PianoControlKeys() {
        // Required empty public constructor
    }

    @Override
    public void setPiano(TonalityPianoView piano) {
        super.setPiano(piano);
        binding.setPiano(piano);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_piano_control_keys, container, false);
        View root = binding.getRoot();

        root.findViewById(R.id.rows_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (piano.rows < 5) ++piano.rows;
                piano.updateParams(true);
            }
        });
        root.findViewById(R.id.rows_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (piano.rows > 1) --piano.rows;
                piano.updateParams(true);
            }
        });

        return root;
    }
}
