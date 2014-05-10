package com.generalprocessingunit.processing;

class Vec {
    double x, y;

    Vec() {
    }

    float x() {
        return (float) x;
    }

    float y() {
        return (float) y;
    }

    Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
