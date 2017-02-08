package com.quaap.fishberserker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity  {

    private MainFishView mMainFishView;

    private TextView mPointsView;
    private TextView mdView;
    private TextView mLivesView;

    private int mPoints;
    private int mLives;

    final Handler handler = new Handler();

    Timer timer;
    TimerTask task;

    private int mWavenum;
    private App app;
    private SoundEffects mSounds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar b = getActionBar();
        if (b!=null) b.hide();

        mMainFishView = (MainFishView) findViewById(R.id.fishscreen);
        mPointsView = (TextView) findViewById(R.id.scores);
        mLivesView = (TextView) findViewById(R.id.lives);

        mLives = 5;
        mWavenum = 0;

        mMainFishView.setOnPointsListener(new MainFishView.OnPointsListener() {
            @Override
            public void onPoints(int points, int hits) {
                mSounds.playGood();
                if (hits>2) {
                    mMainFishView.setText("Combo Bonus!");
                }
                mPoints += points;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showScores();
                    }
                });


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
                            mMainFishView.end();

                        }
                    },500);
                    mLives = 0;
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showScores();
                    }
                });
            }

            @Override
            public void onBoom() {
                mSounds.playBad();
                mMainFishView.setText("Boom");
                onMiss(100);
            }
        });


    }

    private void showScores() {
        mPointsView.setText(mPoints + "");
        mLivesView.setText(mLives + " Lives");
    }


    @Override
    protected void onPause() {
        task.cancel();
        timer.cancel();

        mSounds.releaseBGM();
        mMainFishView.pause();

        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        mSounds.releaseBGM();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        mMainFishView.unpause();
        task = new TimerTask() {
            @Override
            public void run() {
                mSounds.playRandomBGMusic();
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
        app = App.getInstance(this);
        mSounds = app.getSoundEffects();
        //app.getSoundEffects().playBGMusic(0);

    }

    @Override
    protected void onDestroy() {
        mSounds.release();
        super.onDestroy();
    }
}
