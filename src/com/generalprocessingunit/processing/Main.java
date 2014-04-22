package com.generalprocessingunit.processing;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import processing.core.PApplet;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends PApplet {
    OSCPortOut oscPortOut;
    public static final String OSC_INSTRUMENT_NOTE_ADDRESS = "/instrument/%s/note";
    public static final String OSC_UNMUTE_ADDRESS = "/unmute";

    static int maxMandelbrotIters = 1024;
    static int hilbertN = 128; // hilbertN must be power of 2 in order to be square  \

    static int getHilbertDMax() {
        return hilbertN * hilbertN;
    }

    List<HilbertWithMandelbrot> hilbertCoordsAndMandelbrotVals = new ArrayList<>();
    double zoom = 1f;
    double panX = 0f, panY = 0f;

    static boolean redrawHilbertMandelbrot;
    static boolean renderAsHilbertCurve = true;

    public static final int STROKE_WEIGHT_COEF = 1;
    public static final int STROKE_COLOR_LINEARIZED_VIEW = 64;
    public static final int STROKE_COLOR_HILBERT_VIEW = 64;
    public static final int CURSOR_STROKE_COLOR_SEPARATION = 90;

    List<Integer> notes;
    List<Cursor> cursors = new ArrayList<>();
    float speed = 1;
    boolean playing = false;


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
            System.out.println("Couldn't send");
        }
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"--present", Main.class.getCanonicalName()});
    }

    @Override
    public void setup() {
//		size(1050, 720, PApplet.OPENGL);
        size(displayWidth, displayHeight, PApplet.OPENGL);

        background(0);
        colorMode(HSB);

//        drawMandelbrot(this, 0, 0, width, height, panX, panY, zoom, maxMandelbrotIters);
        generateAndDrawHilbertMandelbrot(getHilbertDMax());

        for(int i = 1; i <= 4; i++) {
            cursors.add(new Cursor(i, i * 3, 100 ));
        }
    }

    // TODO: create a more universal note sequence generating system to share amongst projects
    private void generateNoteList() {
        List<Integer> major = Arrays.asList(2, 2, 1, 2, 2, 2, 1);
        List<Integer> wholeTone = Arrays.asList(2);

        int n = 32;
//        notes = Arrays.asList(n);     throws unsupported operation when adding int. WTF??
        notes = new ArrayList<>();
        notes.add(n);
        for (int i = 0; i < maxMandelbrotIters && n < 64; i++) {
//            n += 1;
            n += major.get(i % major.size());
            notes.add(n);
        }
    }

    private void generateAndDrawHilbertMandelbrot(int dMax) {
        List<HilbertWithMandelbrot> coordsWithVals = generateHilbertMandelbrot(dMax);
        hilbertCoordsAndMandelbrotVals = coordsWithVals;

        drawHilbertMandelbrot(this, width / 2, height, coordsWithVals);

        pushMatrix();
        translate(width / 2, 0);
        drawLinearizedValues(this, width / 2, height, coordsWithVals);
        popMatrix();

        generateNoteList();  // TODO: only generate if maxMandelbrotIters increased
    }

    private static void drawLinearizedValues(PApplet p5, int w, int h, List<HilbertWithMandelbrot> coordsWithVals) {
        int dMax = coordsWithVals.size();
        int s = getSideLengthForLinearizedMap(dMax, w, h);
        for (int d = 0; d < dMax; d++) {
            p5.stroke(STROKE_COLOR_LINEARIZED_VIEW);
            p5.strokeWeight(getStrokeWeight(s));
            setFillColorForMandelbrotCoord(p5, coordsWithVals.get(d));

            drawLinearizedMandelbrotCoord(p5, w, s, d);
        }
    }

    private static void drawLinearizedMandelbrotCoord(PApplet p5, int w, int s, int d) {
        int x = d % (w / s);
        int y = (d * s) / w;

        // snake around instead of starting from the left for each row
        if(y % 2 == 1) {
            x = (w / s) - (x+1);
        }

        p5.rect(x * s, y * s, s, s);
    }

    private static int getSideLengthForLinearizedMap(int dMax, int w, int h) {
        float maxA = w * h;
        float largestPossibleSide = sqrt(maxA / dMax);

        int s = 0;
        for (int i = 1; i < w; i++) {
            s = w / i;
            if (s > largestPossibleSide) {
                continue;
            }

            int sqPerRow = w / s;
            int rows = dMax / sqPerRow;
            int leftover = dMax % sqPerRow;

            if (rows * s + (leftover > 0 ? s : 0) <= h) {
                break;
            }
        }
        return s;
    }

    private static void setFillColorForMandelbrotCoord(PApplet p5, HilbertWithMandelbrot coordWithVal) {
        p5.fill(Mandelbrot.getHue(coordWithVal.mandelbrotVal), 255, Mandelbrot.getBrightness(coordWithVal.mandelbrotVal, maxMandelbrotIters));
    }

    private static float getStrokeWeight(int w, int numVals) {
        int s = w / (int)sqrt(numVals);
        return getStrokeWeight(s);
    }

    private static float getStrokeWeight(int sideLength) {
        return sideLength > 2 ? STROKE_WEIGHT_COEF * max(1, min(10, sideLength / 20f)) : 0;
    }

    private static void drawHilbertMandelbrot(PApplet p5, int w, int h, List<HilbertWithMandelbrot> coordsWithVals) {
        p5.strokeWeight(getStrokeWeight(w, coordsWithVals.size()));
        p5.stroke(STROKE_COLOR_HILBERT_VIEW);

        for (int d = 0; d < coordsWithVals.size(); d++) {
            setFillColorForMandelbrotCoord(p5, coordsWithVals.get(d));
            drawHilbertCoordinate(p5, w, h, coordsWithVals.get(d).coordinate);
        }
    }

    private static void drawHilbertCoordinate(PApplet p5, int w, int h, Vec hilbertCoord) {
        float scaleX = w / (float) hilbertN;
        float scaleY = h / (float) hilbertN;
        p5.rect((float) (hilbertCoord.x * scaleX), (float) (hilbertCoord.y * scaleY), scaleX, scaleY);
    }

    private List<HilbertWithMandelbrot> generateHilbertMandelbrot(int dMax) {
        List<HilbertWithMandelbrot> coordsWithVals = new ArrayList<>();
        for (int d = 0; d < dMax; d++) {
            Vec v = Hilbert.distanceToVector(hilbertN, d);
            coordsWithVals.add(new HilbertWithMandelbrot(v, Mandelbrot.getValFromNormalizedCoordinates(v.x / hilbertN, v.y / hilbertN, panX, panY, zoom, maxMandelbrotIters)));
        }
        return coordsWithVals;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            playing = !playing;
        }

        if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            if (e.isControlDown()) {
                hilbertN *= 2;
            } else {
                zoom *= 2;
            }
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            if (e.isControlDown()) {
                hilbertN = hilbertN > 1 ? hilbertN / 2 : hilbertN;
            } else {
                zoom = zoom > 1 ? zoom / 2 : zoom;
            }
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            panY += 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            panY -= 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            panX += 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            panX -= 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_HOME) {
            maxMandelbrotIters = maxMandelbrotIters > 2 ? maxMandelbrotIters / 2 : maxMandelbrotIters;
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_END) {
            maxMandelbrotIters = maxMandelbrotIters * 2;
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_H) {
            renderAsHilbertCurve = !renderAsHilbertCurve;
            redrawHilbertMandelbrot = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_PERIOD) {
            float inc = 0.1f;

            if (e.isControlDown()) {
                inc /= 10;
            } else if (e.isShiftDown()) {
                inc *= 10;
            }

            speed += inc;
        }

        if (e.getKeyCode() == KeyEvent.VK_COMMA) {
            float dec = 0.1f;

            if (e.isControlDown()) {
                dec /= 10;
            } else if (e.isShiftDown()) {
                dec *= 10;
            }

            speed = speed - dec > 0 ? speed - dec : speed;
        }

        System.out.println("zoom: " + zoom + " panX: " + panX + " panY: " + panY);
        System.out.println("maxIters: " + maxMandelbrotIters);


        super.keyPressed(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void draw() {

        if (redrawHilbertMandelbrot) {
            background(0);
            if (renderAsHilbertCurve) {
                generateAndDrawHilbertMandelbrot(getHilbertDMax());
            } else {
                Mandelbrot.draw(this, width, height, panX, panY, zoom, maxMandelbrotIters);
            }

            redrawHilbertMandelbrot = false;
        }

        if(playing) {
            sendOscMsg(OSC_UNMUTE_ADDRESS, 1);
            for(Cursor c : cursors) {
                c.draw(this);
            }
        } else {
            sendOscMsg(OSC_UNMUTE_ADDRESS, 0);
        }
    }

    private void playNote(int cursor, int val) {
        System.out.println("note: " + val);
        sendOscMsg(String.format(OSC_INSTRUMENT_NOTE_ADDRESS, cursor), val == maxMandelbrotIters ? -100000 : notes.get(val % notes.size()));
    }

    class Cursor {
        int id;
        int d;
        int delay;
        int millisAtPlayed = 0;

        Cursor(int id, int delay) {
            this(id, 0, delay);
        }

        Cursor(int id, int d, int delay) {
            this.id = id;
            this.d = d;
            this.delay = delay;
        }

        void draw(PApplet p5) {
            if (millis() - millisAtPlayed < delay / speed) {
                return;
            }

            int maxD = hilbertCoordsAndMandelbrotVals.size();
            int s = getSideLengthForLinearizedMap(maxD, width / 2, height);

            HilbertWithMandelbrot coordAndVal = hilbertCoordsAndMandelbrotVals.get(d % maxD);
            int val = coordAndVal.mandelbrotVal;

            playNote(id, val);

            fill(255, 192);
            strokeWeight(getStrokeWeight(width / 2, maxD));
            stroke((CURSOR_STROKE_COLOR_SEPARATION * id) % 255, 255, 255);
            drawHilbertCoordinate(p5, width / 2, height, coordAndVal.coordinate);

            pushMatrix();
            translate(width / 2, 0);
            strokeWeight(getStrokeWeight(s));
            drawLinearizedMandelbrotCoord(p5, width / 2, s, d % maxD);
            popMatrix();

            // previous cursor position
            erasePreviousCursor(p5, maxD, s);

            millisAtPlayed = millis();
            d++;
        }

        private void erasePreviousCursor(PApplet p5, int maxD, int s) {
            int prevD = (maxD + d - 1) % maxD;
            HilbertWithMandelbrot prevCoordAndVal = hilbertCoordsAndMandelbrotVals.get(prevD);
            int prevVal = prevCoordAndVal.mandelbrotVal;
            Vec prevCoord = prevCoordAndVal.coordinate;

            setFillColorForMandelbrotCoord(p5, prevCoordAndVal);
            strokeWeight(getStrokeWeight(width / 2, maxD));
            stroke(STROKE_COLOR_HILBERT_VIEW);
            drawHilbertCoordinate(p5, width / 2, height, prevCoord);

            pushMatrix();
            translate(width / 2, 0);
            strokeWeight(getStrokeWeight(s));
            stroke(STROKE_COLOR_LINEARIZED_VIEW);
            drawLinearizedMandelbrotCoord(p5, width / 2, s, prevD);
            popMatrix();
        }
    }

    class HilbertWithMandelbrot {
        Vec coordinate;
        int mandelbrotVal;

        HilbertWithMandelbrot(Vec coordinate, int mandelbrotVal) {
            this.coordinate = coordinate;
            this.mandelbrotVal = mandelbrotVal;
        }
    }
}
