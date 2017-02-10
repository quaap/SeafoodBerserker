package com.quaap.fishberserker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity  {

    final Handler handler = new Handler();
    private final int NEW_LIFE_EVERY = 5000;

//    Timer timer;
//    TimerTask task;
    private MainFishView mMainFishView;
    private int mPoints;
    private int mLives;
    //private int mWavenum;
    //private long lastSchedExec;
    //private boolean wasResumed;

    private boolean mPaused;

    private SoundEffects mSounds;

    private SoundEffects.BGMusic mBGMusic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar b = getActionBar();
        if (b!=null) b.hide();

        mMainFishView = (MainFishView) findViewById(R.id.fishscreen);

        mLives = 5;
//        mWavenum = 0;
//        lastSchedExec = 0;

        if (savedInstanceState!=null) {
            unfreeze(savedInstanceState);
            //wasResumed= true;
        }

        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPaused) {
                    unpause();
                } else {
                    pause();
                }
            }
        });

        mMainFishView.setOnPointsListener(new MainFishView.OnPointsListener() {
            @Override
            public void onPoints(int points) {
                if (mPoints%NEW_LIFE_EVERY > (mPoints+points)%NEW_LIFE_EVERY) {
                    mLives++;
                    mSounds.playBest();
                } else {
                   // mSounds.playGood();
                }

                mPoints += points;
                updateScores();

            }

            @Override
            public void onCombo(int hits) {
                //mSounds.playGood();
                if (hits>2) {
                    onPoints(hits*10);
                    mMainFishView.setText("Combo Bonus!");

                }
            }

            @Override
            public void onMiss(int points) {
               // mSounds.playBad();
                mLives--;
                if (mLives<=0) {
                    mMainFishView.setText("Game Over");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMainFishView.setText("Game Over");
                            mMainFishView.end();

                        }
                    },500);
                    mLives = 0;
                }

                updateScores();
            }

            @Override
            public void onBoom() {
                //mSounds.playBad();
                mMainFishView.setText("Boom");
                onMiss(100);
            }
        });


    }

    //private Bundle state;

    private void pause() {
        mPaused = true;
        mMainFishView.pause();
        //state = new Bundle();
        //freeze(state);


    }
    private void unpause() {
//        if (state!=null) {
//            unfreeze(state);
//        }
        mMainFishView.unpause();
        mPaused = false;
    }

    private void updateScores() {
        mMainFishView.setTopStatus(mPoints + "", mLives);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        freeze(outState);
    }

    private void freeze(Bundle bundle) {
        bundle.putLong("freeztime", System.currentTimeMillis());

        bundle.putInt("mPoints", mPoints);
        bundle.putInt("mLives", mLives);
//        bundle.putInt("mWavenum", mWavenum);
//        bundle.putLong("lastSchedExec", lastSchedExec);

        Bundle fishview = new Bundle();
        mMainFishView.freeze(fishview);
        bundle.putBundle("fishview", fishview);
    }

    private void unfreeze(Bundle bundle) {
        mPoints = bundle.getInt("mPoints");
        mLives = bundle.getInt("mLives");
//        mWavenum = bundle.getInt("mWavenum");
//        lastSchedExec = bundle.getLong("lastSchedExec");


//        long freeztime = bundle.getLong("freeztime");
//        long diff =  System.currentTimeMillis() - freeztime;
//        lastSchedExec += diff;

        Bundle fishview = bundle.getBundle("fishview");

        mMainFishView.unfreeze(fishview);
    }

    @Override
    protected void onPause() {
//        task.cancel();
//        timer.cancel();

        mBGMusic.releaseBGM();
        pause();

        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        mBGMusic.releaseBGM();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        mSounds = App.getInstance(this).getSoundEffects();
        mBGMusic = mSounds.getBGMusic();

        unpause();
//        task = new TimerTask() {
//            @Override
//            public void run() {
//                if (mPaused) return;
//                lastSchedExec = System.currentTimeMillis();
//                mBGMusic.playRandomBGMusic();
//                if (!wasResumed) {
//                    mWavenum++;
//                    mMainFishView.setText("Wave " + mWavenum);
//                    mMainFishView.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mMainFishView.startWave(mWavenum, INTERVAL_MILLIS, INTERVALS);
//                        }
//                    }, 3500);
//                }
//                wasResumed = false;
//            }
//        };
//
//        timer = new Timer();
//
//        long time = INTERVAL_MILLIS*(INTERVALS+1);
//        long starttime = 2000;
//        if (lastSchedExec>1) {
//            starttime = time-(System.currentTimeMillis()-lastSchedExec);
//            mBGMusic.playRandomBGMusic();
//        }
//
//        timer.schedule(task, starttime, time);

        updateScores();

    }


    @Override
    protected void onDestroy() {
        mBGMusic.releaseBGM();
        super.onDestroy();
    }
}
