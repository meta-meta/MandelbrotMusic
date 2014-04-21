package com.generalprocessingunit.processing;

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
     * Returns the number of iterations required to escape the Mandelbrot set at the given coordinates
     * @param x normalized x coordinate 0 to 1.0
     * @param y normalized y coordinate 0 to 1.0
     * @param panX < 0 pans left. > 0 pans right
     * @param panY < 0 pans up. > 0 pans down
     * @param zoom 1.0 is unzoomed
     * @param maxIterations
     * @return
     */
    public static int getValFromNormalizedCoordinates(float x, float y, float panX, float panY, float zoom, int maxIterations) {
        float x1 = 0.5f - panX + (x - 0.5f) / zoom;
        float y1 = 0.5f - panY + (y - 0.5f) / zoom;

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
    public static Vec getDenormalizedCoordinates(float x, float y) {
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
    public static int getVal(float scaledX, float scaledY, int maxIterations) {

        float x = 0f, y = 0f;
        int iter = 0;

        while (x * x + y * y < 4 && iter < maxIterations) {
            float xTemp = x * x - y * y + scaledX;
            y = 2 * x * y + scaledY;
            x = xTemp;
            iter++;
        }
        return iter;
    }

}
