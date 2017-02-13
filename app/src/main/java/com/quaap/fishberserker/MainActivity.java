package com.quaap.fishberserker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.quaap.fishberserker.component.SoundEffects;
import com.quaap.fishberserker.game.MainFishView;


public class MainActivity extends Activity  {

    final Handler handler = new Handler();
    private final int NEW_LIFE_EVERY = 5000;


    private MainFishView mMainFishView;
    private int mPoints;
    private int mLives;


    private boolean mPaused;

    private SoundEffects mSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar b = getActionBar();
        if (b!=null) b.hide();

        mMainFishView = (MainFishView) findViewById(R.id.fishscreen);

        mLives = 5;

        if (savedInstanceState!=null) {
            unfreeze(savedInstanceState);

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

        mMainFishView.setOnGameListener(new MainFishView.OnGameListener() {
            @Override
            public void onWaveStart(int wavenum) {
                mSounds.playBGMusic();
            }

            @Override
            public void onWaveDone(int wavenum) {
                mSounds.fadeBGMusic();
            }

            @Override
            public void onIntervalStart(int intervalnum) {

            }

            @Override
            public void onIntervalDone(int intervalnum) {

            }

            @Override
            public void onItemLaunch() {
                mSounds.playPuh();
            }

            @Override
            public void onItemHit(int points) {
                mSounds.playChop();
                if (mPoints%NEW_LIFE_EVERY > (mPoints+points)%NEW_LIFE_EVERY) {
                    mLives++;
                    mSounds.playBest();
                }

                mPoints += points;
                updateScores();

            }

            @Override
            public void onCombo(int hits) {
                mSounds.playGood();
                if (hits>2) {
                    mPoints += hits*10;
                    updateScores();
                    mMainFishView.setText("Combo Bonus!");

                }
            }

            @Override
            public void onMiss(int points) {
                mSounds.playBad();
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


    private void pause() {
        mPaused = true;
        mMainFishView.pause();
    }
    private void unpause() {

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

        Bundle fishview = new Bundle();
        mMainFishView.freeze(fishview);
        bundle.putBundle("fishview", fishview);
    }

    private void unfreeze(Bundle bundle) {
        mPoints = bundle.getInt("mPoints");
        mLives = bundle.getInt("mLives");

        Bundle fishview = bundle.getBundle("fishview");

        mMainFishView.unfreeze(fishview);
    }

    @Override
    protected void onPause() {

        mSounds.releaseBGM();
        pause();

        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        mSounds.releaseBGM();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY  ;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        mSounds = App.getInstance(this).getSoundEffects();


        unpause();

        updateScores();

        mSounds.playBGMusic();

    }


    @Override
    protected void onDestroy() {
        mSounds.releaseBGM();
        super.onDestroy();
    }
}
