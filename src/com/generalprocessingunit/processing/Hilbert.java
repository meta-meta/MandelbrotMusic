package com.generalprocessingunit.processing;

public class Hilbert
{
    static Vec distanceToVector(int size, int distance)
    {
        Vec v = new Vec();
        for (int s = 1; s < size; s *= 2)
        {
            int rx = 1 & (distance / 2);
            int ry = 1 & (distance ^ rx);
            getRotatedVec(s, v, rx, ry);
            v.x += s * rx;
            v.y += s * ry;
            distance /= 4;
        }
        return v;
    }


    //rotate/flip a quadrant appropriately
    private static void getRotatedVec(int n, Vec v, int rx, int ry)
    {
        if (ry == 0)
        {
            if (rx == 1)
            {
                v.x = n - 1 - v.x;
                v.y = n - 1 - v.y;
            }

            //Swap x and y
            float temp = v.x;
            v.x = v.y;
            v.y = temp;
        }
    }
}
