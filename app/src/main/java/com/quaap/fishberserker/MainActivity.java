package com.quaap.fishberserker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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

    final Timer timer = new Timer();
    TimerTask task;

    private int mWavenum;
    private App app;
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
        mWavenum = 3;

        mMainFishView.setOnPointsListener(new MainFishView.OnPointsListener() {
            @Override
            public void onPoints(int points, int hits) {
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
        });


    }

    private void showScores() {
        mPointsView.setText(mPoints + "");
        mLivesView.setText(mLives + " Lives");
    }


    @Override
    protected void onPause() {
        app.getSoundEffects().releaseBGM();
        task.cancel();
        mMainFishView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainFishView.unpause();
        task = new TimerTask() {
            @Override
            public void run() {
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

        timer.schedule(task, 2000, 60000);
        app = App.getInstance(this);
        app.getSoundEffects().playBGMusic(0);
    }
}
