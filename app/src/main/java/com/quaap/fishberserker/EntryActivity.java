package com.quaap.fishberserker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.quaap.fishberserker.component.SoundEffects;

public class EntryActivity extends Activity {

    private boolean horz = false;

    SoundEffects s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        findViewById(R.id.start_classic_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent game = new Intent(EntryActivity.this, MainActivity.class);
                game.putExtra(MainActivity.GAME_TYPE, MainActivity.GAME_TYPE_CLASSIC);
                game.putExtra(MainActivity.GAME_HORZ, horz);
                startActivity(game);
            }
        });

        findViewById(R.id.start_arcade_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent game = new Intent(EntryActivity.this, MainActivity.class);
                game.putExtra(MainActivity.GAME_TYPE, MainActivity.GAME_TYPE_ARCADE);
                game.putExtra(MainActivity.GAME_HORZ, horz);
                startActivity(game);
            }
        });
        s = App.getInstance(this).getSoundEffects();


        findViewById(R.id.open_prefs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent p = new Intent(EntryActivity.this, SettingsActivity.class);
                startActivity(p);
            }
        });

//        findViewById(R.id.orienty).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                horz = ((Switch)view).isChecked();
//                setOrientation();
//            }
//        });



    }


    private void setOrientation() {
        if (horz) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    protected void onPause() {
        s.pauseBGMusic();
        //s.releaseBGM();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        s.playBGMusic(0);

    }

}
