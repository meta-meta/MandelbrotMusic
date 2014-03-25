package com.generalprocessingunit.processing;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;


public class Main extends PApplet{

    static final int MAX_IT = 1000;

    public static void main(String[] args){
		PApplet.main(new String[] { "--present", Main.class.getCanonicalName() });
	}

    static final int N = 2048; // N must be power of 2  \
    static final int dMax = N * N -1;
    List<Vec> coords = new ArrayList();

    int[][] mandelbrot = new int[N][N];

    List<Integer> mandelbrotOneD = new ArrayList<>();


    @Override
	public void setup() {		
		size(1920, 1080, PApplet.OPENGL);

        background(0);
        colorMode(HSB);


//        drawMandelbrot();
        drawHilbert();
    }

    private void drawMandelbrot() {
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                int it = getMandelVal(i, j);

                stroke(200 + it % 255, 200, it == MAX_IT ? 0 : 200);
                point(i * (width / (float) N), j * (height / (float) N));
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


    private int getMandelVal(int i, int j) {
        float x0 = -2.5f + i * (3.5f / N);
        float y0 = -1f + j * (2f / N);

        float x = 0f, y = 0f;
        int it = 0;

        while (x * x + y * y < 2 * 2 && it < MAX_IT)
        {
            float xtemp = x * x - y * y + x0;
            y = 2 * x * y + y0;
            x = xtemp;
            it++;
        }
        return it;
    }

    private void drawHilbert()
    {
        for(int d = 0; d < dMax; d++){
            Vec v = d2xy(N, d);
            coords.add(v);

            mandelbrotOneD.add(getMandelVal(v.x/8+1000,v.y/6+600));
        }


        for(int d = 1; d < dMax; d++){
            Vec a = coords.get(d-1);
            Vec b = coords.get(d);
            float s = 2.5f;
            float sx = (width/((float) N)), sy = (height/(float) N);
            strokeWeight(2);
            stroke( (50 +  mandelbrotOneD.get(d) * 20 )%256, 255, mandelbrotOneD.get(d) == MAX_IT ? 0 : 255);
//            line(a.x * sx, a.y * sy, b.x * sx, b.y * sy);
            line(a.x * s, a.y * s, b.x * s, b.y * s);
        }
    }

    @Override
	public void draw(){
//        int d = frameCount%(dMax-1) + 1;
//        Vec a = coords.get(d-1);
//        Vec b = coords.get(d);
//        int s = 8;
//        stroke(millis()%256, 200, 200);
//        line(a.x * s, a.y * s, b.x * s, b.y * s);

	}

    Vec d2xy(int n, int d)
    {
        int rx, ry, s, t = d;
        Vec v = new Vec();
        for (s = 1; s < n; s *= 2)
        {
            rx = 1 & (t / 2);
            ry = 1 & (t ^ rx);
            rot(s, v, rx, ry);
            v.x += s * rx;
            v.y += s * ry;
            t /= 4;
        }
        return v;
    }

    //rotate/flip a quadrant appropriately
    void rot(int n, Vec v, int rx, int ry)
    {
        if (ry == 0)
        {
            if (rx == 1)
            {
                v.x = n - 1 - v.x;
                v.y = n - 1 - v.y;
            }

            //Swap x and y
            int t = v.x;
            v.x = v.y;
            v.y = t;
        }
    }

    class Vec {
        int x, y;
    }

}
