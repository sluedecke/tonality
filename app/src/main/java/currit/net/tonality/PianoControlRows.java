package currit.net.tonality;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import currit.net.tonality.databinding.FragmentPianoControlRowsBinding;


public class PianoControlRows extends PianoControlFragment {
    private FragmentPianoControlRowsBinding binding;

    public PianoControlRows() {
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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_piano_control_rows, container, false);
        return binding.getRoot();
    }
}
