package com.generalprocessingunit.droid;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.generalprocessingunit.processing.BaseFunctionality;
import com.generalprocessingunit.processing.MandelbrotMusicCore;
import processing.core.PApplet;

public class MandelbrotMusic extends PApplet implements BaseFunctionality {

    MandelbrotMusicCore mm;

    private ScaleGestureDetector mScaleDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setup() {
        size(displayWidth, displayHeight, OPENGL);
        orientation(LANDSCAPE);

        mm = new MandelbrotMusicCore(this, this);


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

    }

    @Override
    public void mute() {

    }

    @Override
    public void unmute() {

    }
}
