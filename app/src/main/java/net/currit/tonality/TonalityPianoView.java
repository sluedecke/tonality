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

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class TonalityPianoView extends PianoView {

    final static String PREF_SCALE = "scale";
    final static String PREF_SCALE_ROOT = "scale_root";
    final static String PREF_LABEL_INTERVALS = "label_intervals";
    final static String PREF_ROWS_TOP_DOWN = "rows_top_down";
    final static int PREF_SCALE_DEFAULT = 0;
    final static int PREF_SCALE_ROOT_DEFAULT = 0;
    final static boolean PREF_LABEL_INTERVALS_DEFAULT = true;
    final static boolean PREF_ROWS_TOP_DOWN_DEFAULT = true; // true = lower notes on top, false = lower notes on bottom

    protected int scale;
    protected int rootNote;
    protected boolean labelIntervals;
    protected boolean rowsTopDown;

    private Paint whiteScalePaint, blackScalePaint;
    private Paint whiteScalePaintRoot, blackScalePaintRoot;
    private Paint intervalWhitePaint, intervalBlackPaint;
    private Paint[] scaleColors;

    private String[] intervalNames;
    private String[] noteNames;

    public TonalityPianoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get orientation based configuration values for keyboard dimensions
        String orientation = getOrientationString();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        rows = sp.getInt("piano_rows" + orientation, 2);
        keys = sp.getInt("piano_keys" + orientation, 7);
        pitch = sp.getInt("piano_pitch" + orientation, 28);

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
        setScale(sp.getInt(PREF_SCALE, PREF_SCALE_DEFAULT), sp.getInt(PREF_SCALE_ROOT, PREF_SCALE_ROOT_DEFAULT));
        sustain = sp.getBoolean("sustain", false);
        labelnotes = sp.getBoolean("labelnotes", true);
        labelc = sp.getBoolean("labelc", true);
        labelIntervals = sp.getBoolean(TonalityPianoView.PREF_LABEL_INTERVALS, TonalityPianoView.PREF_LABEL_INTERVALS_DEFAULT);
        rowsTopDown = sp.getBoolean(TonalityPianoView.PREF_ROWS_TOP_DOWN, TonalityPianoView.PREF_ROWS_TOP_DOWN_DEFAULT);
        concert_a = 440; // set a default to avoid DIV0 errors
        try {
            concert_a = Integer.parseInt(sp.getString("concert_a", "440"));
        } catch (NumberFormatException e) {
            concert_a = 440;
        }

        // get interval and note names for labelling
        intervalNames = getResources().getStringArray(R.array.intervalNames);
        noteNames = getResources().getStringArray(R.array.noteNames);

        // finally set internal arrays and stuff according to configuration values
        updateParams(false);
    }

    private String getOrientationString() {
        return getContext().getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT
                ? "_portrait"
                : "_landscape";
    }

    /**
     * Replaces updateParams from PianoView to properly handle screen orientation
     *
     * @param inval if true, update preferences for rows, keys and pitch
     */
    public void updateParams(boolean inval) {
        pitches = new int[rows][keys];

        int p = 0;
        for (int i = 0; i < pitch; ++i) p += hasBlackRight(p) ? 2 : 1;
        for (int row = 0; row < rows; ++row) {
            int pitchrow = getPitchRow(row);
            for (int key = 0; key < keys; ++key) {
                pitches[pitchrow][key] = p;
                p += hasBlackRight(p) ? 2 : 1;
            }
        }

        if (inval) {
            String orientation = getOrientationString();

            SharedPreferences.Editor editor =
                    androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putInt("piano_rows" + orientation, rows);
            editor.putInt("piano_keys" + orientation, keys);
            editor.putInt("piano_pitch" + orientation, pitch);
            editor.apply();

            invalidate();
        }
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
     * @param canvas Canvas to draw the keyboard(s) on
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
        intervalWhitePaint.setTextSize(Util.maxTextSize("P1", whiteWidth / 4));
        intervalBlackPaint.setTextSize(Util.maxTextSize("P1", whiteWidth / 4));

        for (int row = 0; row < rows; ++row) {
            int pitchrow = getPitchRow(row);
            for (int key = 0; key < keys; ++key) {
                int x = whiteWidth * key;
                int y = whiteHeight * pitchrow;
                int p = pitches[pitchrow][key];

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

    public void toggleRowsTopDown() {
        rowsTopDown = !rowsTopDown;
        SharedPreferences.Editor editor =
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(PREF_ROWS_TOP_DOWN, rowsTopDown);
        editor.apply();
        updateParams(false);
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

    private int getPitchRow(int row) {
        return rowsTopDown ? row : (rows - row - 1);
    }

}