package com.generalprocessingunit.processing;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import processing.core.PApplet;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends PApplet {
    boolean redrawHilbertMandelbrot;

    static int maxMandelbrotIters = 1024;
    static int hilbertN = 128; // hilbertN must be power of 2  \

    List<HilbertWithMandelbrot> hilbertCoordsAndMandelbrotVals = new ArrayList<>();
    float zoom = 1f;
    float shiftX = 0f, shiftY = 0f;

    int getHilbertDMax() {
        return hilbertN * hilbertN - 1;
    }


    OSCPortOut oscPortOut;
    public Main(){
        super();

        try
        {
            oscPortOut = new OSCPortOut();
        }
        catch (Exception e)
        {
            System.out.print(e);
        }
    }

    public static void main(String[] args){
		PApplet.main(new String[] { /*"--present",*/ Main.class.getCanonicalName() });
	}

    private void sendOscMsg(String address, int note) {
        OSCMessage msg = new OSCMessage(address, Arrays.asList((Object) note));
        try
        {
            oscPortOut.send(msg);
        }
        catch (Exception e)
        {
            System.out.println("Couldn't send");
        }
    }

    @Override
	public void setup() {		
		size(1050, 720, PApplet.OPENGL);

        background(0);
        colorMode(HSB);

//        drawMandelbrot(this, 0, 0, width, height, shiftX, shiftY, zoom, maxMandelbrotIters);

        generateAndDrawHilbertMandelbrot(getHilbertDMax());
    }

    private void generateNoteList()
    {
        List<Integer> major = Arrays.asList(2, 2, 1, 2, 2, 2, 1);
        List<Integer> wholeTone = Arrays.asList(2);

        int n = 11;
//        notes = Arrays.asList(n);     throws unsupported operation when adding int. WTF??
        notes = new ArrayList<>();
        notes.add(n);
        for (int i = 0; i < maxMandelbrotIters; i++)
        {
            n += major.get(i % major.size());
            notes.add(n);
        }
    }

    private static void drawMandelbrot(PApplet p5, int x0, int y0, int w, int h, float shiftX, float shiftY, float zoom, int maxIters) {

        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                int iter = getMandelbrotVal(x / (float) w, y / (float) h, shiftX, shiftY, zoom);

                p5.stroke(getMandelbrotHue(iter), 200, getMandelbrotBrightness(iter));
                p5.point(x0 + x, y0 + y);
            }
        }
        //        For each pixel (Px, Py) on the screen, do:
//        {
//            x0 = scaled x coordinate of pixel (scaled to lie in the Mandelbrot X scale (-2.5, 1))
//            y0 = scaled y coordinate of pixel (scaled to lie in the Mandelbrot Y scale (-1, 1))
//
//            x = 0.0
//            y = 0.0
//            iteration = 0
//            max_iteration = 1000
//            while ( x*x + y*y < 2*2  AND  iteration < max_iteration )
//            {
//                xtemp = x*x - y*y + x0
//                y = 2*x*y + y0
//                x = xtemp
//                iteration = iteration + 1
//            }
//            color = palette[iteration]
//            plot(Px, Py, color)
//        }
    }


    private static int getMandelbrotVal(float x0, float y0, float shiftX, float shiftY, float zoom) {
        float x = 0.5f - shiftX + (x0 - 0.5f) / zoom;
        float y = 0.5f - shiftY + (y0 - 0.5f) / zoom;

        Vec v = Mandelbrot.getDenormalizedVec(x, y);
        return Mandelbrot.getVal(v.x, v.y, maxMandelbrotIters);
    }

    class HilbertWithMandelbrot {
        Vec coordinate;
        int mandelbrotVal;

        HilbertWithMandelbrot(Vec coordinate, int mandelbrotVal)
        {
            this.coordinate = coordinate;
            this.mandelbrotVal = mandelbrotVal;
        }
    }

    private void generateAndDrawHilbertMandelbrot(int dMax)
    {
        List<HilbertWithMandelbrot> coordsWithVals = generateHilbertMandelbrot(dMax);
        hilbertCoordsAndMandelbrotVals = coordsWithVals;

        drawHilbertMandelbrot(this, 0, 0, width / 2, height, coordsWithVals);

        drawLinearizedValues(this, width / 2, 0, width / 2, height, coordsWithVals);

        generateNoteList();  // TODO: only generate if maxMandelbrotIters increased
    }

    private static void drawLinearizedValues(PApplet p5, int x0, int y0, int w, int h, List<HilbertWithMandelbrot> coordsWithVals)
    {
        int dMax = coordsWithVals.size();
        float maxA = w * h;
        float largestSide = sqrt(maxA/dMax);
        int s = getSideLength(dMax, w, h, largestSide);
        int x = 0, y = 0;
        for (int d = 1; d < dMax; d++)
        {
            p5.stroke(64);
            p5.strokeWeight(s > 2 ? 1 : 0);
            p5.fill(getMandelbrotHue(d, coordsWithVals), 255, getMandelbrotBrightness(d, coordsWithVals));

//            x = (d * s) % (w - s);          //this isn't quite right but on the right track. should be able to make this a function of d, x, y, w, h
//            y = ((d * s) / (w - s)) * s;    // must have to do with s not dividing equally into w
            p5.rect(x0 + x, y0 + y, s, s);

            if (w >= x + 2 * s)
            {
                x += s;
            }
            else
            {
                x = 0;
                y += s;
            }
        }
    }

    private static void drawHilbertMandelbrot(PApplet p5, int x0, int y0, int w, int h, List<HilbertWithMandelbrot> coordsWithVals)
    {
        for (int d = 1; d < coordsWithVals.size(); d++)
        {
            p5.strokeWeight(2);
            p5.stroke(getMandelbrotHue(d, coordsWithVals), 255, getMandelbrotBrightness(d, coordsWithVals));
            drawHilbertSegment(p5, x0, y0, w, h, d, coordsWithVals);
        }
    }

    private static void drawHilbertSegment(PApplet p5, int x, int y, int w, int h, int d, List<HilbertWithMandelbrot> coordsWithVals)
    {
        Vec a = coordsWithVals.get(d - 1).coordinate;
        Vec b = coordsWithVals.get(d).coordinate;

        float scaleX = w / (float) hilbertN, scaleY = h / (float) hilbertN;
        p5.line(x + a.x * scaleX, y + a.y * scaleY, x + b.x * scaleX, y + b.y * scaleY);
    }

    private static int getMandelbrotBrightness(int d, List<HilbertWithMandelbrot> coordsWithVals)
    {
        return getMandelbrotBrightness(coordsWithVals.get(d).mandelbrotVal);
    }

    private static int getMandelbrotBrightness(int val)
    {
        return val == maxMandelbrotIters ? 0 : 255;
    }

    private static int getMandelbrotHue(int d, List<HilbertWithMandelbrot> coordsWithVals)
    {
        return getMandelbrotHue(coordsWithVals.get(d).mandelbrotVal);
    }

    private static int getMandelbrotHue(int val)
    {
        return val % 256;
    }

    private static int getSideLength(int dMax, int w, int h, float largestSide)
    {
        int s = 0;
        for (int i = 1; i < w; i++)
        {
            s = (int) (w / (float) i);
            if (s > largestSide)
            {
                continue;
            }

            int sqPerRow = w / s;
            int rows = dMax / sqPerRow;
            int leftover = dMax % sqPerRow;

            if (rows * s + (leftover > 0 ? s : 0) <= h)
            {
                break;
            }
        }
        return s;
    }

    private List<HilbertWithMandelbrot> generateHilbertMandelbrot(int dMax)
    {
        List<HilbertWithMandelbrot> coordsWithVals = new ArrayList<>();
        for (int d = 0; d < dMax; d++)
        {
            Vec v = Hilbert.distanceToVector(hilbertN, d);
            coordsWithVals.add(new HilbertWithMandelbrot(v, getMandelbrotVal(v.x / hilbertN, v.y / hilbertN, shiftX, shiftY, zoom)));
        }
        return coordsWithVals;
    }

    int t = 0;
    int millisAtPlayed = millis();
    int u = 15;
    int millisAtPlayedBass = millis();
    int w = 25;
    int millisAtPlayedTenor = millis();


    List<Integer> notes;


    int delay = 300;

    boolean playing = false;

    @Override
    public void keyPressed(KeyEvent e)
    {
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            playing = !playing;
        }

        System.out.println(e.getKeyCode());
        if(e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            if(e.isControlDown()){
                hilbertN *= 2;
            } else {
                zoom *= 2;
            }
            redrawHilbertMandelbrot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            if(e.isControlDown()){
                hilbertN = hilbertN > 1 ? hilbertN /2 : hilbertN;
            } else {
                zoom = zoom > 1 ? zoom / 2 : zoom;
            }
            redrawHilbertMandelbrot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_UP) {
            shiftY += 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            shiftY -= 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            shiftX += 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            shiftX -= 0.1f / zoom;
            redrawHilbertMandelbrot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_HOME) {
            maxMandelbrotIters = maxMandelbrotIters > 2 ? maxMandelbrotIters / 2 : maxMandelbrotIters;
            redrawHilbertMandelbrot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_END) {
            maxMandelbrotIters = maxMandelbrotIters * 2;
            redrawHilbertMandelbrot = true;
        }

        System.out.println("zoom: " + zoom + " shiftX: " + shiftX + " shiftY: " + shiftY);
        System.out.println("maxIters: " + maxMandelbrotIters);


        super.keyPressed(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
	public void draw(){
        blendMode(REPLACE);

        if(redrawHilbertMandelbrot)
        {
            background(0);
            generateAndDrawHilbertMandelbrot(getHilbertDMax());
//            drawMandelbrot(this, 0, 0, width, height, shiftX, shiftY, zoom, maxMandelbrotIters);

            redrawHilbertMandelbrot = false;
        }

        blendMode(ADD);
        colorMode(HSB);

        if(millis() - millisAtPlayed > delay){
            if(!playing) {
                sendOscMsg("/volume", 0);
                return;
            }

            sendOscMsg("/volume", 1);

            HilbertWithMandelbrot coordAndVal = hilbertCoordsAndMandelbrotVals.get(t % hilbertCoordsAndMandelbrotVals.size());
            int val = coordAndVal.mandelbrotVal;
            System.out.println(val);

            sendOscMsg("/note", val == maxMandelbrotIters ? -1000 : notes.get(val % notes.size()));

            // draw the note coord we're playing
            Vec v = coordAndVal.coordinate;
            stroke(0, 0, 255, 150);
            float sx = ((width / 2) / ((float) hilbertN)), sy = (height / (float) hilbertN);
            strokeWeight(10);
            point(v.x * sx, v.y * sy);

            t++;
            millisAtPlayed = millis();
        }

        /*if(millis() - millisAtPlayedBass > delay){
            int bass = hilbertCoordsAndMandelbrotVals.get(u % hilbertCoordsAndMandelbrotVals.size()).mandelbrotVal;
            sendOscMsg("/noteBass", bass == maxMandelbrotIters ? -1000 : notes.get((bass - 6) % notes.size()));

            Vec v = hilbertCoordsAndMandelbrotVals.get(t).coordinate;
            stroke(95, 255, 255, 150);
            float sx = ((width/2)/((float) hilbertN)), sy = (height/(float) hilbertN);
            strokeWeight(11);
            point(v.x * sx, v.y * sy);

            u++;
            millisAtPlayedBass = millis();
        }

        if(millis() - millisAtPlayedTenor > delay){
            int tenor = hilbertCoordsAndMandelbrotVals.get(w % hilbertCoordsAndMandelbrotVals.size()).mandelbrotVal;
            sendOscMsg("/noteTenor", tenor == maxMandelbrotIters ? -1000 : notes.get((tenor - 3) % notes.size()));

            Vec v = hilbertCoordsAndMandelbrotVals.get(t).coordinate;
            stroke(180, 255, 255, 150);
            float sx = ((width/2)/((float) hilbertN)), sy = (height/(float) hilbertN);
            strokeWeight(12);
            point(v.x * sx, v.y * sy);

            w++;
            millisAtPlayedTenor = millis();
        }*/

//        int d = frameCount%(dMax-1) + 1;
//        Vec a = hilbertCoords.get(d-1);
//        Vec b = hilbertCoords.get(d);
//        int s = 8;
//        stroke(millis()%256, 200, 200);
//        line(a.x * s, a.y * s, b.x * s, b.y * s);

	}
}
