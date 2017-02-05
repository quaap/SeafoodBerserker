package com.quaap.fishberserker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;


public class MainActivity extends Activity  {

    private MainFishView mMainFishView;

    private TextView mPointsView;

    private volatile int mPoints;

    final Handler handler = new Handler();

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
        mMainFishView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainFishView.unpause();
    }
}
