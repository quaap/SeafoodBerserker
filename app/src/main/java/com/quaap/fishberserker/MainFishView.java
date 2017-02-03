package com.quaap.fishberserker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class MainFishView extends SurfaceView implements  SurfaceHolder.Callback  {

    private final long STEP = 33; // ~30 fps

    private final double GRAVITY = .2;
    private final double AIRRESIST = .01;

    private RunThread mThread;

    private int mWidth;
    private int mHeight;

    private final List<FlyingItem> availableItems = new ArrayList<>();
    private final List<FlyingItem> itemsInPlay = new ArrayList<>();

    public MainFishView(Context context) {
        super(context);
        init(context);
    }

    public MainFishView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MainFishView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        for (int resid: new int[]{R.drawable.money_1c, R.drawable.money_1d}) {
            FlyingItem item = new FlyingItem(BitmapFactory.decodeResource(getResources(), resid));
            availableItems.add(item);

        }

    }


    private static int getRand(int max) {
        return (int) (max*Math.random());
    }

    private void doDraw(final Canvas canvas, long ticks) {

        if (itemsInPlay.size()<3 && Math.random()>.9) {
            FlyingItem item = FlyingItem.getCopy(availableItems.get(getRand(availableItems.size())));
            item.setX(getRand(mWidth));
            item.setXv((Math.random() - .5)*3);
            item.setY(mHeight + 20);
            item.setYv(-((Math.random() + .5) * 20));
            itemsInPlay.add(item);
        }

        canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

        for(Iterator<FlyingItem> it = itemsInPlay.iterator(); it.hasNext();) {
            FlyingItem item = it.next();
            item.updatePosition(GRAVITY, AIRRESIST);
            if (item.getY()>mHeight && item.getYv()>0) {
                it.remove();
            } else {
                item.draw(canvas);
            }
        }
    }




    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mThread = new RunThread(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        mWidth = width;
        mHeight = height;
        if (mThread!=null) {
            if (!mThread.isRunning()) {
                mThread.start();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mThread.stopRunning();
        mThread = null;
    }

    public void pause() {
        if (mThread!=null) mThread.pauseRunning();

    }

    public void unpause() {
        if (mThread != null) mThread.unpauseRunning();
    }

    class RunThread extends Thread {


        private final SurfaceHolder mSurfaceHolder;


        private volatile boolean mPaused = false;
        private volatile boolean mRun = false;


        public RunThread(final SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
        }


        @Override
        public void run() {
            Log.d("RunThread", "run");
            mRun = true;
            long lasttime = System.currentTimeMillis();
            while (mRun) {
                if (mPaused) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    final long now = System.currentTimeMillis();

                    if (now - lasttime > STEP) {
                        Canvas c = null;
                        try {
                            c = mSurfaceHolder.lockCanvas();
                            if (mRun && !mPaused) {
                                doDraw(c, System.currentTimeMillis() - lasttime);
                            }

                        } finally {
                            if (c != null) {
                                mSurfaceHolder.unlockCanvasAndPost(c);
                            }
                        }
                        lasttime = System.currentTimeMillis();
                    } else {
                        try {
                            sleep(STEP/5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


        public void pauseRunning() {
            mPaused = true;
        }

        public void unpauseRunning() {
            mPaused = false;
        }

        public void stopRunning() {
            Log.d("GraphicDmnThread", "stopRunning");
            mRun = false;
        }

        public boolean isRunning() {
            return mRun;
        }
    }



}
