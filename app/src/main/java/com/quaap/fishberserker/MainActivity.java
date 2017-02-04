package com.quaap.fishberserker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity  {

    MainFishView mMainFishView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar b = getActionBar();
        if (b!=null) b.hide();

        mMainFishView = (MainFishView) findViewById(R.id.fishscreen);

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
