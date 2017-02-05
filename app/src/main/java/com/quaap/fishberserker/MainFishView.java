package com.quaap.fishberserker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
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

    public static final int MAX_FLY = 10;
    public static final double SPAWN_CHANCE = .95;
    public static final int CONFIG_HEIGHT = 1000;
    public static final int MIN_SWIPE = 40;
    public static final int SWIPE_OVERSHOOT = 20;
    public static final int MAX_AXES_REPS = 10;

    private final long STEP = 33; // 1000 ms / ~30 fps  =  33

    private final double GRAVITY = 2;
    private final double AIRRESIST = .06;

    private final double INITIAL_XVMIN = AIRRESIST * 30;
    private final double INITIAL_XVMAX = AIRRESIST * 180;

    private final double INITIAL_YVMIN = GRAVITY * -20;

    private final double INITIAL_YVMAX = GRAVITY * -30;

    private Paint mLinePaint;
    private Paint mBGPaint;
    private Paint mTextPaint;

    private RunThread mThread;

    private int mWidth;
    private int mHeight;

    private final List<FlyingItem> availableItems = new ArrayList<>();
    private final List<FlyingItem> itemsInPlay = new ArrayList<>();

    private final Bitmap[] splats = new Bitmap[2];


    private String mText;


    private OnPointsListener onPointsListener;


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
        if (!this.isInEditMode()) {

            splats[0] = BitmapFactory.decodeResource(getResources(), R.drawable.splat1);
            splats[1] = BitmapFactory.decodeResource(getResources(), R.drawable.splat2);

            final SurfaceHolder holder = getHolder();
            holder.addCallback(this);
            TypedArray fish = getResources().obtainTypedArray(R.array.fish);
            int[] values = getResources().getIntArray(R.array.points);
            for (int i = 0; i < fish.length(); i++) {
                FlyingItem item = new FlyingItem(BitmapFactory.decodeResource(getResources(), fish.getResourceId(i, 0)));
                item.setValue(values[i]);
                availableItems.add(item);
            }
            fish.recycle();

            this.setOnTouchListener(this);
            mLinePaint = new Paint();
            mLinePaint.setARGB(255, 255, 64, 64);
            mLinePaint.setStrokeWidth(5);
            mBGPaint = new Paint();
            mBGPaint.setARGB(255, 255, 255, 255);

            mTextPaint = new Paint();
            mTextPaint.setARGB(255, 255, 64, 64);
            mTextPaint.setStrokeWidth(5);
            mTextPaint.setTextSize(90);
        }
    }

    public void setOnPointsListener(OnPointsListener onPointsListener) {
        this.onPointsListener = onPointsListener;
    }

    private Bitmap mTextBitmap;
    public void setText(String text) {
        mText = text;
        if (text ==null || text.trim().equals("")) {
            mTextBitmap = null;
        } else {

            mTextBitmap = Bitmap.createBitmap(400,200, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mTextBitmap);
            c.drawText(text,0,0, mTextPaint);
        }

    }

    public void setBonusMode(boolean on) {

    }

    private long mWaveStarted;
    private int mWaveNum;
    private int mIntervalmillis;
    private int mIntervals;
    private long mIntervalStarted;
    private int mMaxNumFly;

    private boolean mWaveGoing;

    public void startWave(int num, int intervalmillis, int intervals) {
        mWaveNum = num;
        mIntervalmillis = intervalmillis;
        mIntervals = intervals;
        mWaveStarted = System.currentTimeMillis();
        mIntervalStarted = mWaveStarted;
        mMaxNumFly = num + 2;
        mWaveGoing = true;
    }

    private void spawnAsNeeded() {
        if (mWaveGoing) {
            long now = System.currentTimeMillis();
            long wavespan = now - mWaveStarted;
            if (wavespan < mIntervalmillis * mIntervals) {
                long intervalspan = now - mIntervalStarted;

                if (intervalspan > mIntervalmillis) {
                    mIntervalStarted = now;
                    return;
                }


                if (itemsInPlay.size() < mMaxNumFly * (intervalspan / (double) mIntervalmillis)) {
                    spawnFish();
                }
            } else {
                mWaveGoing = false;
            }
        }
    }

    private void doDraw(final Canvas canvas, long ticks) {

        spawnAsNeeded();
//        if (itemsInPlay.size()< MAX_FLY && Math.random()>SPAWN_CHANCE) {
//            spawnFish();
//        }

        canvas.drawPaint(mBGPaint);

        int times = 0;
        int points = 0;
        int hits = 0;
        while (mAxes.size()>0 && times++< MAX_AXES_REPS) {
            float[] axe = mAxes.pop();

            if (axe != null) {
                for (int i = 0; i < axe.length - 4; i += 2) {
                    if (axe[i + 3]>0) {
                        canvas.drawLine(axe[i], axe[i + 1], axe[i + 2], axe[i + 3], mLinePaint);
                        synchronized (itemsInPlay) {
                            List<FlyingItem[]> newItems = new ArrayList<>();
                            for (Iterator<FlyingItem> it = itemsInPlay.iterator(); it.hasNext(); ) {
                                FlyingItem item = it.next();
                                if (item.isHit(axe[i], axe[i + 1])) {
                                    item.setHit();
                                    newItems.add(item.cut(axe[i], axe[i + 1]));
                                    //it.remove();
                                    int s = Utils.getRand(splats.length);
                                    item.setBitmap(splats[s]);
                                    item.setYv(3);
                                    item.setXv(item.getXv()/2);
                                    item.setSpinv(1);
                                    canvas.drawBitmap(splats[s],(float)item.getX()-splats[0].getWidth()/2, (float)item.getY()-splats[0].getHeight()/2, null);
                                    points += item.getValue();
                                    hits++;
                                }
                            }
                            for (FlyingItem[] fa: newItems) {
                                for (FlyingItem f: fa) {
                                    itemsInPlay.add(0,f);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mAxes.size()>3) mAxes.clear();

        if ((points>0 || hits>0) && onPointsListener!=null) {
            onPointsListener.onPoints(points,hits);
        }

        synchronized (itemsInPlay) {
            for (Iterator<FlyingItem> it = itemsInPlay.iterator(); it.hasNext(); ) {
                FlyingItem item = it.next();
                item.updatePosition(GRAVITY * CONFIG_HEIGHT / mHeight, AIRRESIST);
                if (item.getY() > mHeight && item.getYv() > 0) {
                    if (!item.wasHit() && onPointsListener!=null) {
                        onPointsListener.onMiss(item.getValue());
                    }
                    it.remove();
                } else {
                    item.draw(canvas);
                }
            }
        }

    }

    private void spawnFish() {
        FlyingItem item = FlyingItem.getCopy(availableItems.get(Utils.getRand(availableItems.size())));

        double xv = Utils.getRand(INITIAL_XVMIN, INITIAL_XVMAX) * Math.signum(Math.random()-.5);
        item.setXv(xv);
        if (xv<0) {
            item.setX(Utils.getRand(mWidth/2) + mWidth/2);
        } else {
            item.setX(Utils.getRand(mWidth/2));
        }
        item.setY(mHeight + 20);
        item.setYv(Utils.getRand(INITIAL_YVMIN, INITIAL_YVMAX));
        item.setSpinv((Math.random()-.5)*45);

        synchronized (itemsInPlay) {
            itemsInPlay.add(item);
        }
    }

    private volatile Stack<float[]> mAxes = new Stack<>();

    private float x1;
    private float y1;
    private long starttime;

    @Override
    public boolean onTouch(View view, MotionEvent e) {
        float x0 = e.getX();
        float y0 = e.getY();
        //Log.d("f", e.getAction() + " " + x + " ," + y);
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                //if (System.currentTimeMillis() - starttime < 100) {
                    double dx = x0 - x1;
                    double dy = y0 - y1;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    //Log.d("f", e.getAction() + " " + speed);
                    if (dist > MIN_SWIPE) {
                        int num = SWIPE_OVERSHOOT;
                        float[] axe = new float[num * 2];

                        int pos = 0;
                        for (int ti = -num / 2; ti < 10 + num / 2; ti += 2) {
                            float t = ti / 10f;
                            int xt = (int) ((1 - t) * x0 + t * x1);
                            int yt = (int) ((1 - t) * y0 + t * y1);
                            axe[pos] = xt;
                            axe[pos + 1] = yt;
                            pos += 2;
                        }
                        mAxes.push(axe);
                    }
                //}
                break;



            case MotionEvent.ACTION_UP:



            case MotionEvent.ACTION_DOWN:
                //starttime = System.currentTimeMillis();

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
                    else {
//                        try {
//                            sleep(STEP/5);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
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
            Log.d("RunThread", "stopRunning");
            mRun = false;
        }

        public boolean isRunning() {
            return mRun;
        }
    }

    public interface OnPointsListener {
        void onPoints(int points, int hits);
        void onMiss(int points);
    }


}
