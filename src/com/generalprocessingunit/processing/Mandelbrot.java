package com.generalprocessingunit.processing;

import processing.core.PApplet;

/** This class is derived from Wikipedia's pseudocode.
 *
 *  <pre>
 For each pixel (Px, Py) on the screen, do:
 {
     x0 = scaled x coordinate of pixel (scaled to lie in the Mandelbrot X scale (-2.5, 1))
     y0 = scaled y coordinate of pixel (scaled to lie in the Mandelbrot Y scale (-1, 1))

     x = 0.0
     y = 0.0
     iteration = 0
     max_iteration = 1000
     while ( x*x + y*y < 2*2  AND  iteration < max_iteration )
     {
         xtemp = x*x - y*y + x0
         y = 2*x*y + y0
         x = xtemp
         iteration = iteration + 1
     }
     color = palette[iteration]
     plot(Px, Py, color)
 }
 </pre>
 */
public class Mandelbrot {

    /**
     * Draws the Mandelbrot Set to the screen
     * @param p5 PApplet instance
     * @param width
     * @param height
     * @param panX
     * @param panY
     * @param zoom
     * @param maxIters
     */
    public static void draw(PApplet p5, int width, int height, double panX, double panY, double zoom, int maxIters) {
        drawRect(p5, width, height, 0, width, 0, height, panX, panY, zoom, maxIters);
    }

    public static void drawRect(PApplet p5, int totalWidth, int totalHeight, int x0, int x1, int y0, int y1, double panX, double panY, double zoom, int maxIters) {
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                drawPoint(p5, x / (double) totalWidth, y / (double) totalHeight, panX, panY, zoom, maxIters, x, y);
            }
        }
    }

    private static void drawPoint(PApplet p5, double normalizedX, double normalizedY, double panX, double panY, double zoom, int maxIters, int x, int y) {
        int iter = Mandelbrot.getValFromNormalizedCoordinates(normalizedX, normalizedY, panX, panY, zoom, maxIters);

        p5.stroke(getHue(iter), 200, getBrightness(iter, maxIters));
        p5.point(x, y);
    }

    public static int getBrightness(int val, int maxIters) {
        return val == maxIters ? 0 : 255;
    }

    public static int getHue(int val) {
        return val % 255;
    }

    /**
     * Returns the number of iterations required to escape the Mandelbrot set at the given coordinates
     * @param x normalized x coordinate 0 to 1.0
     * @param y normalized y coordinate 0 to 1.0
     * @param panX < 0 pans left. > 0 pans right
     * @param panY < 0 pans up. > 0 pans down
     * @param zoom 1.0 is unzoomed
     * @param maxIterations
     * @return
     */
    public static int getValFromNormalizedCoordinates(double x, double y, double panX, double panY, double zoom, int maxIterations) {
        double x1 = 0.5f - panX + (x - 0.5f) / zoom;
        double y1 = 0.5f - panY + (y - 0.5f) / zoom;

        Vec v = getDenormalizedCoordinates(x1, y1);
        return getVal(v.x, v.y, maxIterations);
    }

    /**
     * Returns a {@code Vec} representing the Euclidian coordinates of the given normalized "Mandelbrot coordinates"
     * <br>Mandelbrot X scale: -2.5 to 1
     * <br>Mandelbrot Y scale: -1 to 1
     * @param x normalized x coordinate 0 to 1.0
     * @param y normalized y coordinate 0 to 1.0
     * @return
     */
    public static Vec getDenormalizedCoordinates(double x, double y) {
        return new Vec(
            -2.5f + x * 3.5f,
            -1f + y * 2f
        );
    }

    /**
     * Returns the number of iterations to escape the Mandelbrot set at the given coordinates
     * @param scaledX
     * @param scaledY
     * @param maxIterations
     * @return
     */
    public static int getVal(double scaledX, double scaledY, int maxIterations) {

        double x = 0f, y = 0f;
        int iter = 0;

        while (x * x + y * y < 4 && iter < maxIterations) {
            double xTemp = x * x - y * y + scaledX;
            y = 2 * x * y + scaledY;
            x = xTemp;
            iter++;
        }
        return iter;
    }

}
