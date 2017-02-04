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
    private Bitmap mBitmap;
    private double mX;
    private double mY;
    private double mXv;
    private double mYv;

    private int bmWidth;
    private int bmHeight;

    public FlyingItem(Bitmap bitmap) {
        this(bitmap, 0, 0, 0, 0);
    }

    public FlyingItem(Bitmap bitmap, double x, double xv, double y, double yv) {
        this.mBitmap = bitmap;
        this.mX = x;
        this.mXv = xv;
        this.mY = y;
        this.mYv = yv;

        bmWidth = bitmap.getWidth();
        bmHeight = bitmap.getHeight();
    }

    public static FlyingItem getCopy(FlyingItem item) {
        return new FlyingItem(item.mBitmap, item.mX, item.mXv, item.mY, item.mYv);
    }

    public void updatePosition(double gravity, double airresit) {
        mYv -= Math.signum(mYv)*airresit;
        mXv -= Math.signum(mXv)*airresit;

        mYv += gravity;

        mX += mXv;
        mY += mYv;
    }

    public boolean isHit(float x, float y) {
        return (x > mX && x < mX+bmWidth && y > mY && y < mY+bmHeight);

    }

    public void draw(Canvas c) {
        c.drawBitmap(mBitmap, (int) mX - bmWidth/2, (int) mY, null);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public double getX() {
        return mX;
    }

    public void setX(double x) {
        this.mX = x;
    }

    public double getmXv() {
        return mXv;
    }

    public void setmXv(double mXv) {
        this.mXv = mXv;
    }

    public double getY() {
        return mY;
    }

    public void setY(double y) {
        this.mY = y;
    }

    public double getmYv() {
        return mYv;
    }

    public void setmYv(double mYv) {
        this.mYv = mYv;
    }
}
