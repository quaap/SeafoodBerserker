package com.quaap.fishberserker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
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

    public static final int MAX_FLY = 10;
    public static final double SPAWN_CHANCE = .95;
    public static final int CONFIG_HEIGHT = 1000;
    public static final int MIN_SWIPE = 40;
    public static final int SWIPE_OVERSHOOT = 20;
    public static final int MAX_AXES_REPS = 15;
    public static final int AXE_TIMEOUT = 1000;

    private final long STEP = 33; // 1000 ms / ~30 fps  =  33

    private final double GRAVITY = 1.5;
    private final double AIRRESIST = .06;

    private final double INITIAL_XVMIN = AIRRESIST * 30;
    private final double INITIAL_XVMAX = AIRRESIST * 180;

    private final double INITIAL_YVMIN = GRAVITY * -24;

    private final double INITIAL_YVMAX = GRAVITY * -33;

    private final List<FlyingItem> availableItems = new ArrayList<>();
    private final List<FlyingItem> itemsInPlay = new ArrayList<>();
    private final Bitmap[] splats = new Bitmap[2];
    private final Bitmap[] anchor = new Bitmap[1];
    private final Bitmap[] axes = new Bitmap[1];
    private final Bitmap[] bgs = new Bitmap[1];
    private final Bitmap[] fgtops = new Bitmap[1];
    private final Bitmap[] fgbottoms = new Bitmap[1];
    private final Bitmap[] bgsScaled = new Bitmap[1];
    private final Bitmap[] fgtopsScaled = new Bitmap[1];
    private final Bitmap[] fgbottomsScaled = new Bitmap[1];

    private Paint mLinePaint;
    private Paint mBGPaint;
    private Paint mTextPaint;
    private String mScore;
    private int mLives;
    private volatile String mText;
    private final Object mTextLock = new Object();

    private RunThread mThread;
    private int mWidth;
    private int mHeight;
    private OnPointsListener onPointsListener;
    private long mTextStarted;
    private long mWaveStarted;
    private int mWaveNum;
    private int mIntervalmillis;
    private int mIntervals;
    private long mIntervalStarted;
    private int mMaxNumFly;
    private boolean mWaveGoing;
    private double mShipBob = 0;
    private Stack<float[]> mAxes = new Stack<>();
    private float x1;
    private float y1;
    private long starttime;


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

    int totalValue;
    private void init(Context context) {
        if (!this.isInEditMode()) {

            splats[0] = BitmapFactory.decodeResource(getResources(), R.drawable.splat1);
            splats[1] = BitmapFactory.decodeResource(getResources(), R.drawable.splat2);
            anchor[0] = BitmapFactory.decodeResource(getResources(), R.drawable.anchor_sm);
            axes[0] = BitmapFactory.decodeResource(getResources(), R.drawable.axe2);
            bgs[0] = BitmapFactory.decodeResource(getResources(), R.drawable.sea);
            fgtops[0] = BitmapFactory.decodeResource(getResources(), R.drawable.sail);
            fgbottoms[0] = BitmapFactory.decodeResource(getResources(), R.drawable.shipside2);

            final SurfaceHolder holder = getHolder();
            holder.addCallback(this);
            TypedArray fish = getResources().obtainTypedArray(R.array.fish);
            int[] values = getResources().getIntArray(R.array.points);
            totalValue = 0;
            for (int i = 0; i < fish.length(); i++) {
                FlyingItem item = new FlyingItem(BitmapFactory.decodeResource(getResources(), fish.getResourceId(i, 0)));
                item.setValue(values[i]);
                availableItems.add(item);
                totalValue += (100 - values[i]);
            }
            fish.recycle();

            this.setOnTouchListener(this);
            mLinePaint = new Paint();
            mLinePaint.setARGB(255, 255, 64, 64);
            mLinePaint.setStrokeWidth(5);
            mBGPaint = new Paint();
            mBGPaint.setARGB(255, 127, 127, 200);

            mTextPaint = new Paint();
            mTextPaint.setColor(Color.BLACK);
            mTextPaint.setShadowLayer(8,8,8,Color.WHITE);
            mTextPaint.setTextSize(80);

        }
    }

    public void setOnPointsListener(OnPointsListener onPointsListener) {
        this.onPointsListener = onPointsListener;
    }

    public void setText(String text) {
        synchronized (mTextLock) {
            mText = text;
            mTextStarted = System.currentTimeMillis();
        }
    }

    public void setTopStatus(String score, int lives) {
        mScore = score;
        mLives = lives;
    }


    public void setBonusMode(boolean on) {

    }

    public void startWave(int num, int intervalmillis, int intervals) {
        mWaveNum = num;
        mIntervalmillis = intervalmillis;
        mIntervals = intervals;
        mWaveStarted = System.currentTimeMillis();
        mIntervalStarted = mWaveStarted;
        mMaxNumFly = num + 2;
        mWaveGoing = true;
    }

    private long lastAnchor = System.currentTimeMillis();
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

                if (itemsInPlay.size() < mMaxNumFly * (intervalspan / (double) mIntervalmillis) && Utils.getRand(100)>82) {
                    FlyingItem item = spawnFish();
                    if (Utils.getRand(0,100)>97 || now - lastAnchor>5000 && wavespan>mIntervalmillis/3) {
                        item.setBitmap(anchor[0]);
                        item.setBoom(true);
                        item.setYv(Utils.getRand(0,100)<90 ? INITIAL_YVMIN + 2 : INITIAL_YVMAX-2);
                        lastAnchor = now;
                    }
                }
            } else {
                mWaveGoing = false;
            }
        }
    }

    private FlyingItem spawnFish() {

        int randomIndex = -1;
        double random = Math.random() * totalValue;
        for (int i = 0; i < availableItems.size(); ++i)
        {
            random -= (100 - availableItems.get(i).getValue());
            if (random <= 0.0d)
            {
                randomIndex = i;
                break;
            }
        }

        FlyingItem item = FlyingItem.getCopy(availableItems.get(randomIndex));
        //FlyingItem item = FlyingItem.getCopy(availableItems.get(Utils.getRand(availableItems.size())));

        double xv = Utils.getRand(INITIAL_XVMIN, INITIAL_XVMAX) * Math.signum(Math.random()-.5);
        item.setXv(xv);

        int xmid = (int)(mWidth/2*.8);

        if (xv<0) {
            item.setX(mWidth - Utils.getRand(xmid) - xmid/4);
        } else {
            item.setX(Utils.getRand(xmid) + 20);
        }
        item.setY(mHeight + 20);
        item.setYv(Utils.getRand(INITIAL_YVMIN, INITIAL_YVMAX));
        item.setSpinv((Math.random()-.5)*45);

        synchronized (itemsInPlay) {
            itemsInPlay.add(item);
        }
        return item;
    }



    private void doDraw(final Canvas canvas) {

        spawnAsNeeded();

        mShipBob +=.06;
        double shipBobSin = Math.sin(mShipBob);

        drawBackground(canvas, shipBobSin);


        markHits(canvas);


        moveAndDrawItems(canvas);

        pareDeadItems();

        drawBottomFG(canvas, shipBobSin);

        drawFallingItems(canvas);

        drawTopFG(canvas, shipBobSin);

        drawScoreboard(canvas);

        drawLives(canvas);

        drawPopupText(canvas);

    }

    private void drawTopFG(Canvas canvas, double shipBobSin) {
        int bobfactor = 7;

       // int top = Math.min(fgtopsScaled[0].getHeight()-bobfactor, mHeight/6);

        int top = -bobfactor;
        int diff = fgtopsScaled[0].getHeight() - mHeight/6;
        if (diff>0) {
            top -= diff;
        }

        canvas.drawBitmap(fgtopsScaled[0],(int)(shipBobSin*3), top + (int)(shipBobSin*bobfactor),null);
    }

    private void drawBottomFG(Canvas canvas, double shipBobSin) {
        int bobfactor = 6;

        int top = Math.min(fgbottomsScaled[0].getHeight()-bobfactor, mHeight/6);

        canvas.drawBitmap(fgbottomsScaled[0],0, mHeight - top + (int)(shipBobSin*-bobfactor),null);
    }

    private void drawBackground(Canvas canvas, double shipBobSin) {
        Rect dst = new Rect(0, (int) (shipBobSin * 10), mWidth, mHeight);

        canvas.drawBitmap(bgsScaled[0], null, dst, null);

        // canvas.drawPaint(mBGPaint);
    }

    private void drawPopupText(Canvas canvas) {
        synchronized (mTextLock) {
            if (mText!=null) {
                float [] widths = new float[mText.length()];
                mTextPaint.getTextWidths(mText, widths);
                float sum = 0;
                for (float w : widths) {
                    sum += w;
                }

                canvas.drawText(mText, mWidth/2 - sum/2, mHeight/3 , mTextPaint);
                if (System.currentTimeMillis() - mTextStarted > 1500) {
                    mText = null;
                }
            }

        }
    }

    private void drawLives(Canvas canvas) {

        for (int i=1; i<=mLives; i++) {
            canvas.drawBitmap(axes[0], mWidth - (i*axes[0].getWidth()+10), 10, null);
        }
    }

    private void drawScoreboard(Canvas canvas) {
        if (mScore!=null) {
            canvas.drawText(mScore, 10, mTextPaint.getTextSize(), mTextPaint);
        }
    }

    private void drawFallingItems(Canvas canvas) {
        synchronized (itemsInPlay) {
            //draw items coming down here so they'll fall over bottem foreground.
            for (Iterator<FlyingItem> it = itemsInPlay.iterator(); it.hasNext(); ) {
                FlyingItem item = it.next();

                if (item.getYv() > 0 && !item.wasHit() && !item.isBoom()) {
                    item.draw(canvas);
                }
            }
        }
    }

    private void pareDeadItems() {
        //remove some hit items early if too many;
        if (itemsInPlay.size()>12) {
            int num = 0;
            synchronized (itemsInPlay) {
                for (Iterator<FlyingItem> it = itemsInPlay.listIterator(2); it.hasNext(); ) {
                    FlyingItem item = it.next();
                    if (item.wasHit()) {
                        it.remove();
                        if (num++ > 4) {
                            break;
                        }

                    }
                }
            }
            Log.d("f", "removed " + num + " items early");
        }
    }

    private void moveAndDrawItems(Canvas canvas) {
        synchronized (itemsInPlay) {
            for (Iterator<FlyingItem> it = itemsInPlay.iterator(); it.hasNext(); ) {
                FlyingItem item = it.next();
                item.updatePosition(GRAVITY * CONFIG_HEIGHT / mHeight, AIRRESIST);
                if (item.getY() > mHeight && item.getYv() > 0 || item.getX() < 0 || item.getX() > mWidth) {
                    if (!item.isBoom() && !item.wasHit() && onPointsListener != null) {
                        onPointsListener.onMiss(item.getValue());
                    }
                    it.remove();
                } else if (item.getYv() <= 0 || item.wasHit() || item.isBoom()) {

                    item.draw(canvas);
                }
            }
        }
    }

    private void markHits(Canvas canvas) {
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
                                    if (item.isBoom()) {
                                        if (onPointsListener!=null) onPointsListener.onBoom();
                                        it.remove();
                                        break;
                                    }
                                    item.setHit();
                                    newItems.add(item.cut(axe[i], axe[i + 1]));
                                    //it.remove();
                                    int s = Utils.getRand(splats.length);
                                    item.setBitmap(splats[s]);
                                    item.setYv(3);
                                    item.setXv(item.getXv()/2);
                                    item.setSpinv(1);
                                    //canvas.drawBitmap(splats[s],(float)item.getX()-splats[0].getWidth()/2, (float)item.getY()-splats[0].getHeight()/2, null);
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
    }

    @Override
    public boolean onTouch(View view, MotionEvent e) {
        float x0 = e.getX();
        float y0 = e.getY();
        //Log.d("f", e.getAction() + " " + x + " ," + y);
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                if (System.currentTimeMillis() - starttime > AXE_TIMEOUT) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    return true;
                }

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
                starttime = System.currentTimeMillis();

        }

        x1 = x0;
        y1 = y0;
        return false;

    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mThread = new RunThread(surfaceHolder);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        mWidth = width;
        mHeight = height;


        for (int b=0;b<bgs.length; b++) {
            int bgw = bgs[b].getWidth();
            int bgh = bgs[b].getHeight();
            int bgw2 = (int) (bgw / (double) bgh * mHeight);

            Rect src = new Rect(bgw>mWidth ? Utils.getRand(bgw - mWidth-1):0, 0, bgw, bgh);
            Rect dest = new Rect(0, 0, bgw2, mHeight);

            bgsScaled[b] = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bgsScaled[b]);
            c.drawBitmap(bgs[b], src, dest, null);
        }


        for (int b=0;b<fgtops.length; b++) {
            int bgw = fgtops[b].getWidth();
            int bgh = fgtops[b].getHeight();
            int bgh2 = (int) (bgh / (double) bgw * mWidth);

            Rect src = new Rect(0, 0, bgw, bgh);
            Rect dest = new Rect(0, 0, mWidth, bgh2);

            fgtopsScaled[b] = Bitmap.createBitmap(mWidth, bgh2, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(fgtopsScaled[b]);
            c.drawBitmap(fgtops[b], src, dest, null);
        }

        for (int b=0;b<fgbottoms.length; b++) {
            int bgw = fgbottoms[b].getWidth();
            int bgh = fgbottoms[b].getHeight();
            int bgh2 = (int) (bgh / (double) bgw * mWidth);

            Rect src = new Rect(0, 0, bgw, bgh);
            Rect dest = new Rect(0, 0, mWidth, bgh2);

            fgbottomsScaled[b] = Bitmap.createBitmap(mWidth, bgh2, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(fgbottomsScaled[b]);
            c.drawBitmap(fgbottoms[b], src, dest, null);
        }


        Log.d("dimen", mWidth + "x" + mHeight);
//        if (mThread!=null) {
//            if (!mThread.isRunning()) {
//                mThread.start();
//            }
//        }
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

    public void end() {
        if (mThread!=null) {
            mThread.pauseRunning();
            mThread.stopRunning();
        }


    }


    public interface OnPointsListener {
        void onPoints(int points, int hits);
        void onMiss(int points);
        void onBoom();
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
            try {
                while (mRun) {
                    if (mPaused) {
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        final long start = System.currentTimeMillis();

                        Canvas c = null;
                        try {
                            c = mSurfaceHolder.lockCanvas();
                            if (mRun && !mPaused) {
                                doDraw(c);
                            }

                        } finally {
                            if (c != null) {
                                mSurfaceHolder.unlockCanvasAndPost(c);
                            }
                        }
                        long runtime = System.currentTimeMillis() - start;

                        if (runtime < STEP) {
                            Thread.sleep(STEP - runtime);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Log.e("FishLoop", "Mainloop interrupted");
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
