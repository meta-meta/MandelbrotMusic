package com.generalprocessingunit.processing;

public class Mandelbrot
{
    //        x0 = scaled x coordinate of pixel (scaled to lie in the Mandelbrot X scale (-2.5, 1))
//        y0 = scaled y coordinate of pixel (scaled to lie in the Mandelbrot Y scale (-1, 1))
    public static Vec getDenormalizedVec(float x, float y){
        return new Vec(
                -2.5f + x * 3.5f,
                -1f + y * 2f
        );
    }

    public static int getVal(float scaledX, float scaledY, int maxIterations) {

        float x = 0f, y = 0f;
        int iter = 0;

        while (x * x + y * y < 4 && iter < maxIterations)
        {
            float xTemp = x * x - y * y + scaledX;
            y = 2 * x * y + scaledY;
            x = xTemp;
            iter++;
        }
        return iter;
    }

}
