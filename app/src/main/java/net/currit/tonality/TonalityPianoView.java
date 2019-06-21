package net.currit.tonality;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import mn.tck.semitone.PianoView;
import mn.tck.semitone.Util;

public class TonalityPianoView extends PianoView {

    final static String PREF_SCALE = "scale";
    final static String PREF_SCALE_ROOT = "scale_root";
    final static String PREF_LABEL_INTERVALS = "label_intervals";
    final static int PREF_SCALE_DEFAULT = 0;
    final static int PREF_SCALE_ROOT_DEFAULT = 0;
    final static boolean PREF_LABEL_INTERVALS_DEFAULT = true;

    protected int scale = PREF_SCALE_DEFAULT;
    protected int rootNote = PREF_SCALE_ROOT_DEFAULT;
    protected boolean labelIntervals = PREF_LABEL_INTERVALS_DEFAULT;

    private Paint whiteScalePaint, blackScalePaint;
    private Paint whiteScalePaintRoot, blackScalePaintRoot;
    private Paint intervalWhitePaint, intervalBlackPaint;
    private Paint[] scaleColors;

    private String[] intervalNames;
    private String[] noteNames;

    public TonalityPianoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // setup colors
        whiteScalePaint = new Paint();
        whiteScalePaint.setColor(ContextCompat.getColor(getContext(), R.color.whiteScale));
        blackScalePaint = new Paint();
        blackScalePaint.setColor(ContextCompat.getColor(getContext(), R.color.blackScale));
        whiteScalePaintRoot = new Paint();
        whiteScalePaintRoot.setColor(ContextCompat.getColor(getContext(), R.color.whiteScaleRoot));
        blackScalePaintRoot = new Paint();
        blackScalePaintRoot.setColor(ContextCompat.getColor(getContext(), R.color.blackScaleRoot));
        intervalWhitePaint = new Paint();
        intervalWhitePaint.setColor(ContextCompat.getColor(getContext(), R.color.intervalWhiteLabel));
        intervalBlackPaint = new Paint();
        intervalBlackPaint.setColor(ContextCompat.getColor(getContext(), R.color.intervalBlackLabel));

        // initialize from preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        setScale(sp.getInt(PREF_SCALE, PREF_SCALE_DEFAULT), sp.getInt(PREF_SCALE_ROOT, PREF_SCALE_ROOT_DEFAULT));
        sustain = sp.getBoolean("sustain", false);
        labelnotes = sp.getBoolean("labelnotes", true);
        labelc = sp.getBoolean("labelc", true);
        labelIntervals = sp.getBoolean(TonalityPianoView.PREF_LABEL_INTERVALS, TonalityPianoView.PREF_LABEL_INTERVALS_DEFAULT);
        concert_a = 440; // set a default to avoid DIV0 errors
        try {
            concert_a = Integer.parseInt(sp.getString("concert_a", "440"));
        } catch (NumberFormatException e) {
            concert_a = 440;
        }

        // get interval and note names for labelling
        intervalNames = getResources().getStringArray(R.array.intervalNames);
        noteNames = getResources().getStringArray(R.array.noteNames);
    }

    public void setRoot(int newRoot) {
        setScale(this.scale, newRoot);
    }

    public void setScale(int newScale) {
        setScale(newScale, this.rootNote);
    }

    public void setScale(int newScale, int newRoot) {
        scale = newScale;
        rootNote = newRoot;
        if (scale > 0) {
            // get scale array from resources and adjust colors used for drawing the keyboard accordingly
            TypedArray ta = getResources().obtainTypedArray(R.array.scales);
            int[] scaleArray = getResources().getIntArray(ta.getResourceId(scale, 0));
            scaleColors = new Paint[12];
            int realKey;
            for (int i = 0; i < scaleArray.length; i++) {
                realKey = (i + rootNote) % 12;
                scaleColors[realKey] = (scaleArray[i] == 0
                        ? (isBlack(realKey) ? blackPaint : whitePaint)
                        : (isBlack(realKey) ? (realKey == newRoot ? blackScalePaintRoot : blackScalePaint) : (realKey == newRoot ? whiteScalePaintRoot : whiteScalePaint)));
            }
            ta.recycle();
        } else {
            // set default colors if no scale is selected
            scaleColors = new Paint[]{whitePaint, blackPaint, whitePaint, blackPaint, whitePaint, whitePaint, blackPaint, whitePaint, blackPaint, whitePaint, blackPaint, whitePaint};
        }

        // store in preferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putInt(PREF_SCALE, scale);
        editor.putInt(PREF_SCALE_ROOT, rootNote);
        editor.apply();

        invalidate();
    }

    /**
     * Override the original pianoview onDraw to be able to highlight a scale
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth(), height = getHeight();

        whiteWidth = width / keys;
        whiteHeight = height / rows;
        blackWidth = whiteWidth * 2 / 3;
        blackHeight = whiteHeight / 2;
        // TODO: 2019-06-21 reduce text size in landscape by 30%
        blackPaint.setTextSize(Util.maxTextSize("G0", whiteWidth * 2 / 3));
        intervalWhitePaint.setTextSize(Util.maxTextSize("P1", whiteWidth * 1 / 4));
        intervalBlackPaint.setTextSize(Util.maxTextSize("P1", whiteWidth * 1 / 4));

        for (int row = 0; row < rows; ++row) {
            for (int key = 0; key < keys; ++key) {
                int x = whiteWidth * key, y = whiteHeight * row;
                int p = pitches[row][key];

                // key frame
                canvas.drawRect(x, y, x + whiteWidth, y + whiteHeight - YPAD, grey3Paint);
                // white key
                canvas.drawRect(x + OUTLINE, y + OUTLINE, x + whiteWidth - OUTLINE,
                        y + whiteHeight - OUTLINE * 2 - YPAD,
                        pressed[p] ? grey4Paint : scaleColors[p % 12]);

                // draw labels if labelnotes is true and either labelc is "off" or we are at a C note
                if (labelnotes && (!labelc || p % 12 == 0)) canvas.drawText(
                        noteNames[p % 12] + (p / 12 - 1),
                        x + whiteWidth / 2, y + whiteHeight * 4 / 5, blackPaint);

                // draw interval names on white keys
                if (labelIntervals) {
                    canvas.drawText(intervalNames[(p - rootNote) % 12],
                            x + whiteWidth / 2,
                            y + whiteHeight - 2 * YPAD,
                            intervalWhitePaint);
                }

                // black keys
                if (hasBlackLeft(p)) {
                    canvas.drawRect(
                            x, y,
                            x + blackWidth / 2, y + blackHeight,
                            pressed[p - 1] ? grey1Paint : scaleColors[(p - 1) % 12]);
                    // draw interval names on black keys
                    if (labelIntervals) {
                        canvas.drawText(intervalNames[(p - rootNote - 1) % 12],
                                x,
                                y + blackHeight - YPAD,
                                intervalBlackPaint);
                    }
                }
                if (hasBlackRight(p)) canvas.drawRect(
                        x + whiteWidth - blackWidth / 2, y,
                        x + whiteWidth, y + blackHeight,
                        pressed[p + 1] ? grey1Paint : scaleColors[(p + 1) % 12]);

            }
        }
    }


    //
    // Callbacks which can be used in Android data bindings
    //

    public void addRow(View v) {
        if (rows < 5) ++rows;
        updateParams(true);
    }

    public void removeRow(View v) {
        if (rows > 1) --rows;
        updateParams(true);
    }

    public void addKey(View v) {
        if (keys < 21) ++keys;
        updateParams(true);
    }

    public void removeKey(View v) {
        if (keys > 7) --keys;
        updateParams(true);
    }

    public void octaveLeft(View v) {
        pitch -= 7;
        if (pitch < 7) pitch = 7;
        updateParams(true);
    }

    public void pitchLeft(View v) {
        if (pitch < 49) ++pitch;
        updateParams(true);
    }

    public void pitchRight(View v) {
        if (pitch > 7) --pitch;
        updateParams(true);
    }

    public void octaveRight(View v) {
        pitch += 7;
        if (pitch > 49) pitch = 49;
        updateParams(true);
    }

    public void resetPiano(View v) {
        rows = 2;
        keys = 7;
        pitch = 28;
        updateParams(true);
    }

    public void toggleLabelNotes() {
        labelnotes = !labelnotes;
        SharedPreferences.Editor editor =
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean("labelnotes", labelnotes);
        editor.apply();
        invalidate();
    }

    public void toggleLabelC() {
        labelc = !labelc;
        SharedPreferences.Editor editor =
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean("labelc", labelc);
        editor.apply();
        invalidate();
    }

    public void toggleLabelIntervals() {
        labelIntervals = !labelIntervals;
        SharedPreferences.Editor editor =
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(PREF_LABEL_INTERVALS, labelIntervals);
        editor.apply();
        invalidate();
    }

    public boolean isLabelNotes() {
        return labelnotes;
    }

    public boolean isLabelC() {
        return labelc;
    }

    public int getScale() {
        return scale;
    }

    public int getRootNote() {
        return rootNote;
    }

    public boolean isLabelIntervals() {
        return labelIntervals;
    }

    private boolean isBlack(int p) {
        return p % 12 == 1 || p % 12 == 3 || p % 12 == 6 || p % 12 == 8 || p % 12 == 10;
    }

}