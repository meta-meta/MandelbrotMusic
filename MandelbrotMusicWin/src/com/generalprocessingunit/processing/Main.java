package com.generalprocessingunit.processing;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import processing.core.PApplet;

import java.awt.event.KeyEvent;
import java.util.Arrays;


public class Main extends PApplet implements BaseFunctionality {
    static final boolean LOGGING = false;

    /* OSC */
    OSCPortOut oscPortOut;
    static final String OSC_INSTRUMENT_NOTE_ADDRESS = "/instrument/%s/note";
    static final String OSC_UNMUTE_ADDRESS = "/unmute";

    MandelbrotMusic mandelbrotMusic;

    public Main() {
        super();

        try {
            oscPortOut = new OSCPortOut();
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    private void sendOscMsg(String address, int note) {
        OSCMessage msg = new OSCMessage(address, Arrays.asList((Object) note));
        try {
            oscPortOut.send(msg);
        } catch (Exception e) {
            log("error", "Couldn't send");
        }
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"--present", Main.class.getCanonicalName()});
    }

    @Override
    public void setup() {
//		size(1050, 720, PApplet.OPENGL);
        size(displayWidth, displayHeight, PApplet.OPENGL);

        mandelbrotMusic = new MandelbrotMusic(this, this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            mandelbrotMusic.togglePlaying();
        }

        if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            if (e.isControlDown()) {
                mandelbrotMusic.increaseHilbertN();
            } else {
                mandelbrotMusic.increaseZoom();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            if (e.isControlDown()) {
                mandelbrotMusic.decreaseHilbertN();
            } else {
                mandelbrotMusic.decreaseZoom();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            mandelbrotMusic.panUp();
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            mandelbrotMusic.panDown();
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            mandelbrotMusic.panLeft();
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            mandelbrotMusic.panRight();
        }

        if (e.getKeyCode() == KeyEvent.VK_HOME) {
            mandelbrotMusic.decreaseMandelbrotIters();
        }

        if (e.getKeyCode() == KeyEvent.VK_END) {
            mandelbrotMusic.increaseMandelbrotIters();
        }

        if (e.getKeyCode() == KeyEvent.VK_H) {
            mandelbrotMusic.toggleRenderAsHilbertCurve();
        }

        if (e.getKeyCode() == KeyEvent.VK_PERIOD) {
            float inc = 1f;

            if (e.isControlDown()) {
                inc /= 10;
            } else if (e.isShiftDown()) {
                inc *= 10;
            }

            mandelbrotMusic.increasePlaybackSpeed(inc);
        }

        if (e.getKeyCode() == KeyEvent.VK_COMMA) {
            float dec = 1f;

            if (e.isControlDown()) {
                dec /= 10;
            } else if (e.isShiftDown()) {
                dec *= 10;
            }

            mandelbrotMusic.decreasePlaybackSpeed(dec);
        }

        super.keyPressed(e);
    }

    @Override
    public void draw() {
        mandelbrotMusic.draw();
    }

    public void playNote(int cursor, int note, boolean rest) {
        log("note", note);
        sendOscMsg(String.format(OSC_INSTRUMENT_NOTE_ADDRESS, cursor), rest ? note : -10000);
    }

    @Override
    public void unmute() {
        sendOscMsg(OSC_UNMUTE_ADDRESS, 1);
    }

    @Override
    public void mute() {
        sendOscMsg(OSC_UNMUTE_ADDRESS, 0);
    }

    public void log(Object msg) {
        if(LOGGING) {
            System.out.println(msg);
        }
    }
    public void log(String label, Object msg) {
        if(LOGGING) {
            System.out.println(label + ": " + msg);
        }
    }


}
