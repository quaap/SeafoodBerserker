package com.quaap.fishberserker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class EntryActivity extends Activity {

    SoundEffects.BGMusic s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        findViewById(R.id.start_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent game = new Intent(EntryActivity.this, MainActivity.class);
                startActivity(game);
            }
        });
        s = App.getInstance(this).getSoundEffects().getBGMusic();
    }

    @Override
    protected void onPause() {
        s.pauseBGMusic();
        s.releaseBGM();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        s.playBGMusic(0);
    }

    @Override
    protected void onDestroy() {
        s.releaseBGM();
        super.onDestroy();
    }
}
