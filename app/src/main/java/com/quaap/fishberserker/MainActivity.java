package com.quaap.fishberserker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity  {

    private MainFishView mMainFishView;


    private int mPoints;
    private int mLives;

    final Handler handler = new Handler();
    private final int NEW_LIFE_EVERY = 5000;

    Timer timer;
    TimerTask task;

    private int mWavenum;

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
        mWavenum = 0;

        mMainFishView.setOnPointsListener(new MainFishView.OnPointsListener() {
            @Override
            public void onPoints(int points) {
                if (mPoints%NEW_LIFE_EVERY > (mPoints+points)%NEW_LIFE_EVERY) {
                    mLives++;
                    mSounds.playBest();
                } else {
                    mSounds.playGood();
                }

                mPoints += points;
                updateScores();

            }

            @Override
            public void onCombo(int hits) {
                mSounds.playGood();
                if (hits>2) {
                    onPoints(hits*10);
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
                mSounds.playBad();
                mMainFishView.setText("Boom");
                onMiss(100);
            }
        });


    }

    private void updateScores() {
        mMainFishView.setTopStatus(mPoints + "", mLives);
    }


    @Override
    protected void onPause() {
        task.cancel();
        timer.cancel();

        mBGMusic.releaseBGM();
        mMainFishView.pause();

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

        mMainFishView.unpause();
        task = new TimerTask() {
            @Override
            public void run() {
                mBGMusic.playRandomBGMusic();
                mWavenum++;
                mMainFishView.setText("Wave " + mWavenum);
                mMainFishView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMainFishView.startWave(mWavenum, 5000, 11);
                    }
                }, 3500);
            }
        };
        timer = new Timer();
        timer.schedule(task, 2000, 60000);

        updateScores();

    }

    @Override
    protected void onDestroy() {
        mBGMusic.releaseBGM();
        super.onDestroy();
    }
}
