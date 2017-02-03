package com.quaap.fishberserker;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by tom on 2/3/17.
 * <p>
 * Copyright (C) 2017  tom
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
public class FlyingItem {
    private Bitmap bitmap;
    private double x;
    private double y;
    private double xv;
    private double yv;

    public FlyingItem(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public FlyingItem(Bitmap bitmap, double x, double xv, double y, double yv) {
        this.bitmap = bitmap;
        this.x = x;
        this.xv = xv;
        this.y = y;
        this.yv = yv;
    }

    public static FlyingItem getCopy(FlyingItem item) {
        return new FlyingItem(item.bitmap, item.x, item.xv, item.y, item.yv);
    }

    public void updatePosition(double gravity, double airresit) {
        yv -= yv*airresit;
        xv -= xv*airresit;

        yv += gravity;

        x += xv;
        y += yv;
    }

    public void draw(Canvas c) {
        c.drawBitmap(bitmap, (int)x, (int)y, null);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getXv() {
        return xv;
    }

    public void setXv(double xv) {
        this.xv = xv;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getYv() {
        return yv;
    }

    public void setYv(double yv) {
        this.yv = yv;
    }
}
