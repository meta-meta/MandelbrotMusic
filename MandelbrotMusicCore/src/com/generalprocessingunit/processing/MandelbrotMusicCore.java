package com.generalprocessingunit.processing;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MandelbrotMusicCore {
    private BaseFunctionality base;
    private PApplet p5;

    /* Rendering state */
    private int maxMandelbrotIters = 1024;
    private int hilbertN = 128; // hilbertN must be power of 2 in order to be square

    private int getHilbertDMax() {
        return hilbertN * hilbertN;
    }

    private List<HilbertWithMandelbrot> hilbertCoordsAndMandelbrotVals = new ArrayList<>();
    private double zoom = 1f;
    private double panX = 0f, panY = 0f;
    private boolean redrawHilbertMandelbrot;
    private boolean renderAsHilbertCurve = true;


    /* Border size and color */
    private static final int STROKE_WEIGHT_COEF = 1;
    private static final int STROKE_COLOR_LINEARIZED_VIEW = 64;
    private static final int STROKE_COLOR_HILBERT_VIEW = 64;
    private float strokeWeightHilbert = 0;
    private float strokeWeightLinearized = 0;


    /* Cursors */
    private List<Cursor> cursors = new ArrayList<>();
    private static final int CURSOR_STROKE_COLOR_SEPARATION = 64;
    private static final int CURSOR_ALPHA = 128;

    private List<Integer> notes;

    private int tick = 0;
    private int millisAtTick = 0;
    private float tickDelay = 100;
    private boolean playing = false;

    public MandelbrotMusicCore(BaseFunctionality base, PApplet p5) {
        this.base = base;
        this.p5 = p5;
        setup();
    }

    private void setup() {
        p5.background(0);
        p5.colorMode(PApplet.HSB);
        generateAndDrawHilbertMandelbrot(getHilbertDMax());
        setupCursors();
    }

    private void setupCursors() {
        for(int i = 1; i <= 10; i++) {
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

        strokeWeightHilbert = getStrokeWeight(p5.width / 2, hilbertCoordsAndMandelbrotVals.size());

        drawHilbertMandelbrot(p5, p5.width / 2, p5.height, hilbertCoordsAndMandelbrotVals, maxMandelbrotIters, hilbertN, strokeWeightHilbert);

        p5.pushMatrix();
        p5.translate(p5.width / 2, 0);
        drawLinearizedValues(p5.width / 2, p5.height, hilbertCoordsAndMandelbrotVals, maxMandelbrotIters);
        p5.popMatrix();

        generateNoteList();  // TODO: only generate if maxMandelbrotIters increased
    }

    private void drawLinearizedValues(int w, int h, List<HilbertWithMandelbrot> coordsWithVals, int maxIters) {
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
        float largestPossibleSide = PApplet.sqrt(maxA / dMax);

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
        int s = w / (int)PApplet.sqrt(numVals);
        return getStrokeWeight(s);
    }

    private static float getStrokeWeight(int sideLength) {
        return sideLength > 2 ? STROKE_WEIGHT_COEF * PApplet.max(1, PApplet.min(10, sideLength / 20f)) : 0;
    }

    private static void drawHilbertMandelbrot(PApplet p5, int w, int h, List<HilbertWithMandelbrot> coordsWithVals, int maxIters, int hilbertN, float strokeWeight) {
        p5.strokeWeight(strokeWeight);
        p5.stroke(STROKE_COLOR_HILBERT_VIEW);

        for(HilbertWithMandelbrot coordWithVal : coordsWithVals) {
            setFillColorForMandelbrotCoord(p5, coordWithVal, maxIters);
            drawHilbertCoordinate(p5, w, h, coordWithVal.coordinate, hilbertN);
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

    public void draw() {
        if (redrawHilbertMandelbrot) {
            p5.background(0);
            if (renderAsHilbertCurve) {
                generateAndDrawHilbertMandelbrot(getHilbertDMax());
            } else {
                Mandelbrot.draw(p5, p5.width, p5.height, panX, panY, zoom, maxMandelbrotIters);
            }

            redrawHilbertMandelbrot = false;
        }

        if(playing) {
            base.unmute();

            if(p5.millis() - millisAtTick > tickDelay) {
                for(Cursor c : cursors) {
                    c.draw();
                }
                tick++;
                millisAtTick = p5.millis();
            }
        } else {
            base.mute();
        }
    }

    public boolean togglePlaying() {
        return playing = !playing;
    }

    public boolean toggleRenderAsHilbertCurve() {
        redrawHilbertMandelbrot = true;
        return renderAsHilbertCurve = !renderAsHilbertCurve;
    }

    public void increaseHilbertN() {
        hilbertN *= 2;
        redrawHilbertMandelbrot = true;
    }

    public void decreaseHilbertN() {
        hilbertN = hilbertN > 1 ? hilbertN / 2 : hilbertN;
        redrawHilbertMandelbrot = true;
    }

    public void decreaseMandelbrotIters() {
        maxMandelbrotIters = maxMandelbrotIters > 2 ? maxMandelbrotIters / 2 : maxMandelbrotIters;
        redrawHilbertMandelbrot = true;
    }

    public void increaseMandelbrotIters() {
        maxMandelbrotIters = maxMandelbrotIters * 2;
        redrawHilbertMandelbrot = true;
    }

    public void increaseZoom() {
        zoom *= 2;
        redrawHilbertMandelbrot = true;
    }

    public void decreaseZoom() {
        zoom = zoom > 1 ? zoom / 2 : zoom;
        redrawHilbertMandelbrot = true;
    }

    public void panUp() {
        panY += 0.1f / zoom;
        redrawHilbertMandelbrot = true;
    }

    public void panDown() {
        panY -= 0.1f / zoom;
        redrawHilbertMandelbrot = true;
    }

    public void panLeft() {
        panX += 0.1f / zoom;
        redrawHilbertMandelbrot = true;
    }

    public void panRight() {
        panX -= 0.1f / zoom;
        redrawHilbertMandelbrot = true;
    }

    public void increasePlaybackSpeed(float amount) {
        tickDelay = tickDelay - amount > 0 ? tickDelay - amount : tickDelay;
    }

    public void decreasePlaybackSpeed(float amount) {
        tickDelay += amount;
    }

    private class Cursor {
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

        void draw() {
            if (tick - ticksAtLastPlay < ticksPerRest) {
                return;
            }

            int maxD = hilbertCoordsAndMandelbrotVals.size();
            int s = getSideLengthForLinearizedMap(maxD, p5.width / 2, p5.height);

            // previous cursor position
            erasePreviousCursor(p5, maxD, s);

            HilbertWithMandelbrot coordAndVal = hilbertCoordsAndMandelbrotVals.get(d % maxD);
            int val = coordAndVal.mandelbrotVal;

            base.playNote(id, notes.get(val % notes.size()), val == maxMandelbrotIters);

            p5.blendMode(PApplet.ADD);
            p5.fill(255, CURSOR_ALPHA);
            p5.strokeWeight(strokeWeightHilbert);
            p5.stroke((CURSOR_STROKE_COLOR_SEPARATION * id) % 255, 255, 255);
            drawHilbertCoordinate(p5, p5.width / 2, p5.height, coordAndVal.coordinate, hilbertN);

            p5.pushMatrix();
            p5.translate(p5.width / 2, 0);
            p5.strokeWeight(strokeWeightLinearized);
            drawLinearizedMandelbrotCoord(p5, p5.width / 2, s, d % maxD);
            p5.popMatrix();
            p5.blendMode(PApplet.REPLACE);

            ticksAtLastPlay = tick;
            d++;
        }

        private void erasePreviousCursor(PApplet p5, int maxD, int s) {
            int prevD = (maxD + d - 1) % maxD;
            HilbertWithMandelbrot prevCoordAndVal = hilbertCoordsAndMandelbrotVals.get(prevD);

            setFillColorForMandelbrotCoord(p5, prevCoordAndVal, maxMandelbrotIters);
            p5.strokeWeight(strokeWeightHilbert);
            p5.stroke(STROKE_COLOR_HILBERT_VIEW);
            drawHilbertCoordinate(p5, p5.width / 2, p5.height, prevCoordAndVal.coordinate, hilbertN);

            p5.pushMatrix();
            p5.translate(p5.width / 2, 0);
            p5.strokeWeight(strokeWeightLinearized);
            p5.stroke(STROKE_COLOR_LINEARIZED_VIEW);
            drawLinearizedMandelbrotCoord(p5, p5.width / 2, s, prevD);
            p5.popMatrix();
        }
    }

    private class HilbertWithMandelbrot {
        Vec coordinate;
        int mandelbrotVal;

        HilbertWithMandelbrot(Vec coordinate, int mandelbrotVal) {
            this.coordinate = coordinate;
            this.mandelbrotVal = mandelbrotVal;
        }
    }

}
