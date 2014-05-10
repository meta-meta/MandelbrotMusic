package com.generalprocessingunit.processing;

import processing.core.PApplet;


public class HilbertCurve extends PApplet {


    public static final double scale = 2;

    public static void main(String[] args) {
        PApplet.main(new String[]{"--present", HilbertCurve.class.getCanonicalName()});
    }

    @Override
    public void setup() {
//		size((int)(720 * scale), (int)(480 * scale), PApplet.OPENGL);
		size(displayWidth, displayHeight, PApplet.OPENGL);

        background(0);

        blendMode(ADD);



    }

    private void drawHilbert(int size) {


        float xCoef = (sin(millis() / 5000f) + 2) * width - 5;
        float yCoef = (cos(millis() / 5000f) + 2) * height - 5;
        float xScale = xCoef / (float)size;
        float yScale = yCoef / (float)size;


//        fill(255);
//        rect(xScale * size * .5f, yScale * size * .5f, 10, 10);


        pushMatrix();

//        translate(-xScale * Hilbert.distanceToVector(size, size * size - 1).x() * .02f, 0);


        for (int i = 1; i < size * size; i++) {
            Vec v1 = Hilbert.distanceToVector(size, i);
            Vec v2 = Hilbert.distanceToVector(size, i - 1);
            line(
                    5 + v1.x() * xScale, 5 +  v1.y() * yScale,
                    5 +  v2.x() * xScale, 5 +  v2.y() * yScale);
        }
        popMatrix();
    }


    @Override
    public void draw() {
        background(0);

        strokeWeight(6);
        stroke(255, 255, 0, 64);
        drawHilbert(256);


        pushMatrix();
        translate(5,5);
        strokeWeight(2);
        stroke(0, 255, 0, 96);
        drawHilbert(128);
        popMatrix();

        strokeWeight(3);
        stroke(255, 0, 0, 96);
        drawHilbert(128);

        pushMatrix();
        translate(-5,-5);
        stroke(0, 0, 255, 96);
        drawHilbert(128);
        popMatrix();
    }

}
