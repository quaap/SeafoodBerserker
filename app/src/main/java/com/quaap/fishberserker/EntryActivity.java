package com.quaap.fishberserker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
                Intent game = new Intent(EntryActivity.this, PlayActivity.class);
                game.putExtra(PlayActivity.GAME_TYPE, PlayActivity.GAME_TYPE_CLASSIC);
                game.putExtra(PlayActivity.GAME_HORZ, horz);
                startActivity(game);
            }
        });

        findViewById(R.id.start_arcade_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent game = new Intent(EntryActivity.this, PlayActivity.class);
                game.putExtra(PlayActivity.GAME_TYPE, PlayActivity.GAME_TYPE_ARCADE);
                game.putExtra(PlayActivity.GAME_HORZ, horz);
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


        findViewById(R.id.about_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about = new Intent(EntryActivity.this, AboutActivity.class);
                startActivity(about);
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

        TextView scorelabel = findViewById(R.id.high_score);
        SharedPreferences prefs = getSharedPreferences("scores", MODE_PRIVATE);
        int score = prefs.getInt("score",0);
        if (score>0) {
            long scoredate = prefs.getLong("date",0);
            scorelabel.setText(getString(R.string.high_score_lab, score, scoredate));
        } else {
            scorelabel.setText("");
        }
    }

}
