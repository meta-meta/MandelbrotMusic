package com.generalprocessingunit.processing;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import processing.core.PApplet;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends PApplet {
    static final boolean LOGGING = false;

    /* OSC */
    OSCPortOut oscPortOut;
    static final String OSC_INSTRUMENT_NOTE_ADDRESS = "/instrument/%s/note";
    static final String OSC_UNMUTE_ADDRESS = "/unmute";


    /* Rendering state */
    int maxMandelbrotIters = 1024;
    int hilbertN = 128; // hilbertN must be power of 2 in order to be square  \

    int getHilbertDMax() {
        return hilbertN * hilbertN;
    }

    List<HilbertWithMandelbrot> hilbertCoordsAndMandelbrotVals = new ArrayList<>();
    double zoom = 1f;
    double panX = 0f, panY = 0f;
    boolean redrawHilbertMandelbrot;
    boolean renderAsHilbertCurve = true;


    /* Border size and color */
    static final int STROKE_WEIGHT_COEF = 1;
    static final int STROKE_COLOR_LINEARIZED_VIEW = 64;
    static final int STROKE_COLOR_HILBERT_VIEW = 64;
    float strokeWeightHilbert = 0;
    float strokeWeightLinearized = 0;


    /* Cursors */
    List<Cursor> cursors = new ArrayList<>();
    static final int CURSOR_STROKE_COLOR_SEPARATION = 64;
    static final int CURSOR_ALPHA = 128;

    List<Integer> notes;

    int tick = 0;
    int millisAtTick = 0;
    float tickDelay = 100;
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

        background(0);
        colorMode(HSB);

        generateAndDrawHilbertMandelbrot(getHilbertDMax());

        setupCursors();
    }

    private void setupCursors() {
        for(int i = 1; i <= 20; i++) {
            cursors.add(new Cursor(i, i * 3, i));
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
        hilbertCoordsAndMandelbrotVals = generateHilbertMandelbrot(dMax);

        strokeWeightHilbert = getStrokeWeight(width / 2, hilbertCoordsAndMandelbrotVals.size());

        drawHilbertMandelbrot(this, width / 2, height, hilbertCoordsAndMandelbrotVals, maxMandelbrotIters, hilbertN, strokeWeightHilbert);

        pushMatrix();
        translate(width / 2, 0);
        drawLinearizedValues(this, width / 2, height, hilbertCoordsAndMandelbrotVals, maxMandelbrotIters);
        popMatrix();

        generateNoteList();  // TODO: only generate if maxMandelbrotIters increased
    }

    private void drawLinearizedValues(PApplet p5, int w, int h, List<HilbertWithMandelbrot> coordsWithVals, int maxIters) {
        int dMax = coordsWithVals.size();
        int s = getSideLengthForLinearizedMap(dMax, w, h);
        strokeWeightLinearized = getStrokeWeight(s);
        for (int d = 0; d < dMax; d++) {
            p5.stroke(STROKE_COLOR_LINEARIZED_VIEW);
            p5.strokeWeight(strokeWeightLinearized);
            setFillColorForMandelbrotCoord(p5, coordsWithVals.get(d), maxIters);

            drawLinearizedMandelbrotCoord(p5, w, s, d);
        }
    }

    private static void drawLinearizedMandelbrotCoord(PApplet p5, int w, int s, int d) {
        int valsPerRow = w / s;

        int x = d % valsPerRow;
        int y = d / valsPerRow;

        // snake around instead of starting from the left for each row
        if(y % 2 == 1) {
            x = valsPerRow - (x + 1);
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

    private static void setFillColorForMandelbrotCoord(PApplet p5, HilbertWithMandelbrot coordWithVal, int maxIters) {
        p5.fill(Mandelbrot.getHue(coordWithVal.mandelbrotVal), 255, Mandelbrot.getBrightness(coordWithVal.mandelbrotVal, maxIters));
    }

    private static float getStrokeWeight(int w, int numVals) {
        int s = w / (int)sqrt(numVals);
        return getStrokeWeight(s);
    }

    private static float getStrokeWeight(int sideLength) {
        return sideLength > 2 ? STROKE_WEIGHT_COEF * max(1, min(10, sideLength / 20f)) : 0;
    }

    private static void drawHilbertMandelbrot(PApplet p5, int w, int h, List<HilbertWithMandelbrot> coordsWithVals, int maxIters, int hilbertN, float strokeWeight) {
        p5.strokeWeight(strokeWeight);
        p5.stroke(STROKE_COLOR_HILBERT_VIEW);

        for (int d = 0; d < coordsWithVals.size(); d++) {
            setFillColorForMandelbrotCoord(p5, coordsWithVals.get(d), maxIters);
            drawHilbertCoordinate(p5, w, h, coordsWithVals.get(d).coordinate, hilbertN);
        }
    }

    private static void drawHilbertCoordinate(PApplet p5, int w, int h, Vec hilbertCoord, int hilbertN) {
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
            float inc = 1f;

            if (e.isControlDown()) {
                inc /= 10;
            } else if (e.isShiftDown()) {
                inc *= 10;
            }

            tickDelay = tickDelay - inc > 0 ? tickDelay - inc : tickDelay;
        }

        if (e.getKeyCode() == KeyEvent.VK_COMMA) {
            float dec = 1f;

            if (e.isControlDown()) {
                dec /= 10;
            } else if (e.isShiftDown()) {
                dec *= 10;
            }

            tickDelay += dec;
        }

        log("zoom: " + zoom + " panX: " + panX + " panY: " + panY);
        log("maxIters", maxMandelbrotIters);


        super.keyPressed(e);
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

            if(millis() - millisAtTick > tickDelay) {
                for(Cursor c : cursors) {
                    c.draw(this);
                }
                tick++;
                millisAtTick = millis();
            }
        } else {
            sendOscMsg(OSC_UNMUTE_ADDRESS, 0);
        }
    }

    private void playNote(int cursor, int val) {
        log("note", val);
        sendOscMsg(String.format(OSC_INSTRUMENT_NOTE_ADDRESS, cursor), val == maxMandelbrotIters ? -100000 : notes.get(val % notes.size()));
    }

    private static void log(Object msg) {
        if(LOGGING) {
            System.out.println(msg);
        }
    }
    private static void log(String label, Object msg) {
        if(LOGGING) {
            System.out.println(label + ": " + msg);
        }
    }

    class Cursor {
        int id;
        int d;
        int ticksPerRest;
        int ticksAtLastPlay = 0;

        Cursor(int id, int ticksPerRest) {
            this(id, 0, ticksPerRest);
        }

        Cursor(int id, int d, int ticksPerRest) {
            this.id = id;
            this.d = d;
            this.ticksPerRest = ticksPerRest;
        }

        void draw(PApplet p5) {
            if (tick - ticksAtLastPlay < ticksPerRest) {
                return;
            }

            int maxD = hilbertCoordsAndMandelbrotVals.size();
            int s = getSideLengthForLinearizedMap(maxD, width / 2, height);

            // previous cursor position
            erasePreviousCursor(p5, maxD, s);

            HilbertWithMandelbrot coordAndVal = hilbertCoordsAndMandelbrotVals.get(d % maxD);
            int val = coordAndVal.mandelbrotVal;

            playNote(id, val);

            blendMode(ADD);
            fill(255, CURSOR_ALPHA);
            strokeWeight(strokeWeightHilbert);
            stroke((CURSOR_STROKE_COLOR_SEPARATION * id) % 255, 255, 255);
            drawHilbertCoordinate(p5, width / 2, height, coordAndVal.coordinate, hilbertN);

            pushMatrix();
            translate(width / 2, 0);
            strokeWeight(strokeWeightLinearized);
            drawLinearizedMandelbrotCoord(p5, width / 2, s, d % maxD);
            popMatrix();
            blendMode(REPLACE);

            ticksAtLastPlay = tick;
            d++;
        }

        private void erasePreviousCursor(PApplet p5, int maxD, int s) {
            int prevD = (maxD + d - 1) % maxD;
            HilbertWithMandelbrot prevCoordAndVal = hilbertCoordsAndMandelbrotVals.get(prevD);

            setFillColorForMandelbrotCoord(p5, prevCoordAndVal, maxMandelbrotIters);
            strokeWeight(strokeWeightHilbert);
            stroke(STROKE_COLOR_HILBERT_VIEW);
            drawHilbertCoordinate(p5, width / 2, height, prevCoordAndVal.coordinate, hilbertN);

            pushMatrix();
            translate(width / 2, 0);
            strokeWeight(strokeWeightLinearized);
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
