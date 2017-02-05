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

    private volatile int mPoints;

    final Handler handler = new Handler();

    final Timer timer = new Timer();
    TimerTask task;

    int mWavenum = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar b = getActionBar();
        if (b!=null) b.hide();

        mMainFishView = (MainFishView) findViewById(R.id.fishscreen);
        mPointsView = (TextView) findViewById(R.id.scores);


        mMainFishView.setOnPointsListener(new MainFishView.OnPointsListener() {
            @Override
            public void onPoints(final int points, int hits) {
                if (hits>2) {
                    mMainFishView.setText("Combo Bonus!");
                }
                mPoints += points;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPointsView.setText(mPoints + " " + points);
                    }
                });


            }

            @Override
            public void onMiss(int points) {

            }
        });


    }

    @Override
    protected void onPause() {
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
                }, 3000);
            }
        };

        timer.schedule(task, 2000, 60000);
    }
}
