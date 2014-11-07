package com.generalprocessingunit.processing;

import com.generalprocessingunit.io.OSC;
import processing.core.PApplet;

import java.awt.event.KeyEvent;


public class Main extends PApplet implements BaseFunctionality {
    static final boolean LOGGING = false;

    /* OSC */
    static final String OSC_INSTRUMENT_NOTE_ADDRESS = "/instrument/%s/note";
    static final String OSC_UNMUTE_ADDRESS = "/unmute";

    MandelbrotMusicCore mm;

    public Main() {
        super();
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"--present", Main.class.getCanonicalName()});
    }

    @Override
    public void setup() {
//		size(1050, 720, PApplet.OPENGL);
        size(displayWidth, displayHeight, PApplet.OPENGL);

        mm = new MandelbrotMusicCore(this, this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            mm.togglePlaying();
        }

        if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            if (e.isControlDown()) {
                mm.increaseHilbertN();
            } else {
                mm.increaseZoom();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            if (e.isControlDown()) {
                mm.decreaseHilbertN();
            } else {
                mm.decreaseZoom();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            mm.panUp();
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            mm.panDown();
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            mm.panLeft();
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            mm.panRight();
        }

        if (e.getKeyCode() == KeyEvent.VK_HOME) {
            mm.decreaseMandelbrotIters();
        }

        if (e.getKeyCode() == KeyEvent.VK_END) {
            mm.increaseMandelbrotIters();
        }

        if (e.getKeyCode() == KeyEvent.VK_H) {
            mm.toggleRenderAsHilbertCurve();
        }

        if (e.getKeyCode() == KeyEvent.VK_PERIOD) {
            float inc = 1f;

            if (e.isControlDown()) {
                inc /= 10;
            } else if (e.isShiftDown()) {
                inc *= 10;
            }

            mm.increasePlaybackSpeed(inc);
        }

        if (e.getKeyCode() == KeyEvent.VK_COMMA) {
            float dec = 1f;

            if (e.isControlDown()) {
                dec /= 10;
            } else if (e.isShiftDown()) {
                dec *= 10;
            }

            mm.decreasePlaybackSpeed(dec);
        }

        if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
            mm.decreaseNumCursors();
        }

        if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
            mm.increaseNumCursors();
        }

        super.keyPressed(e);
    }

    @Override
    public void draw() {
        mm.draw();
    }

    @Override
    public void playNote(int cursor, int note, boolean rest) {
        log("note", note);
        OSC.sendMsg(String.format(OSC_INSTRUMENT_NOTE_ADDRESS, cursor), rest ? -10000 : note);
    }

    @Override
    public void unmute() {
        OSC.sendMsg(OSC_UNMUTE_ADDRESS, 1);
    }

    @Override
    public void mute() {
        OSC.sendMsg(OSC_UNMUTE_ADDRESS, 0);
    }

    @Override
    public void log(Object msg) {
        if(LOGGING) {
            System.out.println(msg);
        }
    }

    @Override
    public void log(String label, Object msg) {
        if(LOGGING) {
            System.out.println(label + ": " + msg);
        }
    }
}
