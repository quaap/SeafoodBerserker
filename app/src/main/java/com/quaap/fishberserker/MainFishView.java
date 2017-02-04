package com.quaap.fishberserker;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

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
public class MainFishView extends SurfaceView implements  SurfaceHolder.Callback, SurfaceView.OnTouchListener  {

    private final long STEP = 33; // ~30 fps

    private final double GRAVITY = 1;
    private final double AIRRESIST = .06;

    private final double INITIAL_XVMIN = AIRRESIST * 30;
    private final double INITIAL_XVMAX = AIRRESIST * 180;

    private final double INITIAL_YVMIN = GRAVITY * -35;

    private final double INITIAL_YVMAX = GRAVITY * -60;

    private Paint mLinePaint;
    private Paint mBGPaint;

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
        this.setOnTouchListener(this);
        mLinePaint = new Paint();
        mLinePaint.setARGB(255, 255, 64, 64);
        mLinePaint.setStrokeWidth(5);
        mBGPaint =  new Paint();
        mBGPaint.setARGB(255, 255, 255, 255);
    }

    private static int getRand(int max) {
        return (int) (max*Math.random());
    }

    private static double getRand(double min, double max) {
        return (max-min)*Math.random() + min;
    }

    private void doDraw(final Canvas canvas, long ticks) {

        if (itemsInPlay.size()<3 && Math.random()>.9) {
            FlyingItem item = FlyingItem.getCopy(availableItems.get(getRand(availableItems.size())));

            double xv = getRand(INITIAL_XVMIN, INITIAL_XVMAX) * Math.signum(Math.random()-.5);
            item.setmXv(xv);
            if (xv<0) {
                item.setX(getRand(mWidth/2) + mWidth/2);
            } else {
                item.setX(getRand(mWidth/2));
            }
            item.setY(mHeight + 20);
            item.setmYv(getRand(INITIAL_YVMIN, INITIAL_YVMAX));
            synchronized (itemsInPlay) {
                itemsInPlay.add(item);
            }
        }

        canvas.drawPaint(mBGPaint);

        synchronized (itemsInPlay) {
            for (Iterator<FlyingItem> it = itemsInPlay.iterator(); it.hasNext(); ) {
                FlyingItem item = it.next();
                item.updatePosition(GRAVITY*1000/mHeight, AIRRESIST);
                if (item.getY() > mHeight && item.getmYv() > 0) {
                    it.remove();
                } else {
                    item.draw(canvas);
                }
            }
        }
        if (mAxes.size()>0) {
            float[] axe = mAxes.pop();
            if (axe != null) {
                for (int i = 0; i < axe.length - 4; i += 2) {
                    canvas.drawLine(axe[i], axe[i + 1], axe[i + 2], axe[i + 3], mLinePaint);
                }
            }
        }

    }

    private volatile Stack<float[]> mAxes = new Stack<>();

    private float x1;
    private float y1;

    @Override
    public boolean onTouch(View view, MotionEvent e) {
        float x0 = e.getX();
        float y0 = e.getY();
        //Log.d("f", e.getAction() + " " + x + " ," + y);
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // System.out.println(e.getAction());

                float dx = x0 - x1;
                float dy = y0 - y1;
                double dist = Math.sqrt(dx*dx + dy+dy);
                //Log.d("f", e.getAction() + " " + speed);
                if (dist > 100) {
                    float[] axe = new float[5*2];

                    for (int ti=0; ti<10; ti+=2) {
                        float t = ti/10f;
                        int xt = (int)((1-t)*x0 + t*x1);
                        int yt = (int)((1-t)*y0 + t*y1);
                        axe[ti] = xt;
                        axe[ti+1] = yt;
                        synchronized (itemsInPlay) {
                            for (Iterator<FlyingItem> it = itemsInPlay.iterator(); it.hasNext(); ) {
                                FlyingItem item = it.next();
                                if (item.isHit(xt, yt)) {
                                    it.remove();
                                }
                            }
                        }
                    }
                    mAxes.push(axe);
                }
                break;



            case MotionEvent.ACTION_UP:



            case MotionEvent.ACTION_DOWN:

        }

        x1 = x0;
        y1 = y0;
        return false;

    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mThread = new RunThread(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        mWidth = width;
        mHeight = height;

        Log.d("dimen", mWidth + "x" + mHeight);
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
                    }
//                    else {
//                        try {
//                            sleep(STEP/5);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
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
            Log.d("RunThread", "stopRunning");
            mRun = false;
        }

        public boolean isRunning() {
            return mRun;
        }
    }



}
