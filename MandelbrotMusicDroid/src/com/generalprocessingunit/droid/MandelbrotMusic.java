package com.generalprocessingunit.droid;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.example.MandelbrotMusicDroid.R;
import com.generalprocessingunit.processing.BaseFunctionality;
import com.generalprocessingunit.processing.MandelbrotMusicCore;
import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;
import processing.core.PApplet;

import java.io.File;
import java.io.IOException;

public class MandelbrotMusic extends PApplet implements BaseFunctionality {

    MandelbrotMusicCore mm;

    private ScaleGestureDetector mScaleDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            initPd();
        } catch (IOException e) {
            Log.e("pd", "pd failed to init: "+e.getCause());
            exit();
        }
    }

    private void initPd() throws IOException {
        AudioParameters.init(this);
        int srate = Math.max(44100, AudioParameters.suggestSampleRate());
        PdAudio.initAudio(srate, 0, 2, 1, true);

        File dir = getFilesDir();
        File patchFile = new File(dir, "Mandelbrot.pd");
        IoUtils.extractZipResource(getResources().openRawResource(R.raw.patch), dir, true);
        PdBase.openPatch(patchFile.getAbsolutePath());
    }

    @Override
    protected void onStart() {
        super.onStart();
        PdAudio.startAudio(this);
    }

    @Override
    protected void onStop() {
        PdAudio.stopAudio();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        PdAudio.release();
        PdBase.release();
        super.onDestroy();
    }

    @Override
    public void setup() {
        size(displayWidth, displayHeight, OPENGL);
        orientation(LANDSCAPE);

        mm = new MandelbrotMusicCore(this, this);
        mm.togglePlaying();

        Looper.prepare();
        mScaleDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());
        getSurfaceView().setOnTouchListener(new TouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
                mm.panLeft();
            }

            @Override
            public void onSwipeLeft() {
                mm.panRight();
            }

            @Override
            public void onSwipeUp() {
                mm.panDown();
            }

            @Override
            public void onSwipeDown() {
                mm.panUp();
            }

            @Override
            public void onDblTap() {
                mm.increaseZoom();
            }
        });
    }

    @Override
    public void draw() {
        mm.draw();
    }

//    float prevX, prevY;
//


    @Override
    public boolean surfaceTouchEvent(MotionEvent e) {
        mScaleDetector.onTouchEvent(e);

//        float x = e.getX();
//        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {

                }
                break;
        }

//        prevX = x;
//        prevY = y;

//        return true;

        return super.surfaceTouchEvent(e);
    }

    int millisAtScale = 0;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d("Scale", ""+detector.getScaleFactor());
//            mScaleFactor *= detector.getScaleFactor();
//
//            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            if(detector.getScaleFactor() > 1 && millis() - millisAtScale > 500) {
                mm.increaseZoom();
                millisAtScale = millis();
            }

            if(detector.getScaleFactor() < 1 && millis() - millisAtScale > 500) {
                mm.decreaseZoom();
                millisAtScale = millis();
            }

            return true;
        }
    }

    @Override
    public void log(String label, Object msg) {

    }

    @Override
    public void log(Object msg) {

    }

    @Override
    public void playNote(int cursor, int note, boolean rest) {
        PdBase.sendList("instrument", cursor, "volume", rest ? 0 : 80);
        PdBase.sendList("instrument", cursor, "pan", (cursor % 2 == 0) ? -1 : 1 );
        PdBase.sendList("instrument", cursor, "oscillator", cursor % 3);
        PdBase.sendList("instrument", cursor, "note", note + 30);
    }

    @Override
    public void mute() {
        PdBase.sendFloat("volume", 0f);
    }

    @Override
    public void unmute() {
        PdBase.sendFloat("volume", 95f);
    }
}
