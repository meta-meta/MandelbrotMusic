package com.generalprocessingunit.processing;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;


public class Main extends PApplet{
	public static void main(String[] args){
		PApplet.main(new String[] { /*"--present",*/ Main.class.getCanonicalName() });
	}

    int n = 128; // n must be power of 2  \
    int dMax = n*n-1;
    List<Vec> coords = new ArrayList();
		
	@Override
	public void setup() {		
		size(1024, 1024, PApplet.OPENGL);


        for(int d = 0; d < dMax; d++){
            coords.add(d2xy(n,d));
        }

        background(0);
        colorMode(HSB);

        for(int d = 1; d < dMax; d++){
            Vec a = coords.get(d-1);
            Vec b = coords.get(d);
            int s = 8;
            stroke(d%100+100, 200, 40);
            line(a.x * s, a.y * s, b.x * s, b.y * s);
        }

	}
	
	@Override
	public void draw(){
        int d = frameCount%(dMax-1) + 1;
        Vec a = coords.get(d-1);
        Vec b = coords.get(d);
        int s = 8;
        stroke(millis()%256, 200, 200);
        line(a.x * s, a.y * s, b.x * s, b.y * s);

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
