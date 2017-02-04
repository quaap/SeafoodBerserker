package com.quaap.fishberserker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

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


    private double mSpin;
    private double mSpinv;
    Matrix mSpinMatrix = new Matrix();

    private int bmWidth;
    private int bmHeight;

    public FlyingItem(Bitmap bitmap) {
        this(bitmap, 0, 0, 0, 0, 0);
    }

    public FlyingItem(Bitmap bitmap, double x, double xv, double y, double yv, double spinv) {
        mBitmap = bitmap;
        mX = x;
        mXv = xv;
        mY = y;
        mYv = yv;
        mSpinv = spinv;

        bmWidth = bitmap.getWidth();
        bmHeight = bitmap.getHeight();
    }

    public static FlyingItem getCopy(FlyingItem item) {
        return new FlyingItem(item.mBitmap, item.mX, item.mXv, item.mY, item.mYv, item.mSpinv);
    }

    public void updatePosition(double gravity, double airresit) {
        mYv -= Math.signum(mYv)*airresit;
        mXv -= Math.signum(mXv)*airresit;

        mSpinv -= Math.signum(mSpinv)*airresit*2;


        mYv += gravity;

        mX += mXv;
        mY += mYv;
        mSpin += mSpinv;
    }

    public boolean isHit(float x, float y) {
        return (x > mX && x < mX+bmWidth && y > mY && y < mY+bmHeight);

    }

    public void draw(Canvas c) {
        int max = Math.max(bmHeight,bmWidth);

        mSpinMatrix.reset();
        //mSpinMatrix.setTranslate(0, bmHeight/2);
        mSpinMatrix.setRotate((float)mSpin, max/2, max/2);
        // mSpinMatrix.setTranslate(max/2, max/2);

        Bitmap bm = Bitmap.createBitmap(max, max, Bitmap.Config.ARGB_8888);
        Canvas rot = new Canvas(bm);
        rot.drawBitmap(mBitmap, mSpinMatrix, null);

        c.drawBitmap(bm, (int) mX - max/2, (int) mY - max/2, null);
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

    public void setSpinv(double spinv) {
        mSpinv = spinv;
    }
}
