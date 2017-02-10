package com.quaap.fishberserker;

/**
 * Created by tom on 2/5/17.
 * <p>
 * Copyright (C) 2017  tom
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class SoundEffects implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SoundPool mSounds;

    private Map<Integer,Integer> mSoundIds = new HashMap<>();

    private int [] soundFiles;

    private float [] soundVolumes;
    private String [] soundUses;

    private volatile float sfvolume = .9f;
    private volatile float musicvolume = .2f;

    private SharedPreferences appPreferences;

    private volatile boolean mReady = false;

    private volatile boolean mMute = false;

    private Context mContext;
    private int[] mBGMSongIds;
    private MediaPlayer mBGMPlayer;
    private float mBGMVolume;


    public SoundEffects(final Context context) {
        mContext = context;
        if (Build.VERSION.SDK_INT>=21) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(5)
                    .build();
        } else {
            mSounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        appPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());


        appPreferences.registerOnSharedPreferenceChangeListener(this);

        soundFiles = getResIdArray(context, R.array.sounds);

        int [] vols =  context.getResources().getIntArray(R.array.sounds_volumes);
        soundVolumes = new float[soundFiles.length];
        for (int i=0; i<vols.length; i++) {
            soundVolumes[i] = vols[i]/100.0f;
        }

        soundUses = new String[soundFiles.length];
        String [] uses =  context.getResources().getStringArray(R.array.sounds_usage);

        for (int i=0; i<uses.length; i++) {
            soundUses[i] = uses[i];
        }


        Utils.async(new Runnable() {
            @Override
            public void run() {

                for (int i=0; i<soundFiles.length; i++) {
                    mSoundIds.put(i, mSounds.load(context, soundFiles[i],1));
                }
                mReady = true;
            }
        });

        mBGMSongIds = getResIdArray(context, R.array.songs);
    }

    private static int[] getResIdArray(Context context, int id) {
        final TypedArray idsarr = context.getResources().obtainTypedArray(id);
        int [] ids = new int[idsarr.length()];
        for (int i=0; i<idsarr.length(); i++) {
            ids[i] = idsarr.getResourceId(i, 0);
        }
        idsarr.recycle();
        return ids;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        //Log.d("Pref", s);

        if (s.equals("sound_effects_volume")) {
            sfvolume = sharedPreferences.getInt("sound_effects_volume", 90)/100.0f;
        }
        if (s.equals("music_volume")) {
            musicvolume = sharedPreferences.getInt("music_volume", 90)/100.0f;


            setBGMusicVolume(musicvolume);

        }
    }

    private boolean isReady() {
        return mReady;
    }

    public void setMute(boolean mute) {
        mMute = mute;

    }

    public boolean isMuted() {
        return mMute;
    }

    private void loop(int soundKey, int loop) {
        play(soundKey, 1, loop);
    }

    private void play(int soundKey) {
        play(soundKey, 1, 0);
    }

    private void play(int soundKey, float speed, int loop) {
        sfvolume = appPreferences.getInt("sound_effects_volume", 90)/100.0f;

        try {
            if (isReady() && !mMute && appPreferences.getBoolean("use_sound_effects", true)) {

                float vol = soundVolumes[soundKey] * sfvolume + getRandHundreth() ;
                mSounds.play(mSoundIds.get(soundKey), vol, vol, 1, loop, speed);
                Log.d("sfx", soundKey + " key");
            }
        } catch (Exception e) {
            Log.e("SoundEffects", "Error playing " + soundKey, e);
        }
    }

    public void playGood() {
        playUsage("good");
    }

    public void playBad() {
        playUsage("bad");
    }

    public void playBest() {
        playUsage("best");
    }

    public void playUsage(String usage) {
        List<Integer> usages = new ArrayList<>();
        for (int i=0; i<soundUses.length; i++) {
            if (soundUses[i].equals(usage)) {
                usages.add(i);
            }
        }

        play(usages.get(Utils.getRand(usages.size())));
    }

    private float getRandHundreth() {
        return (float)((Math.random()-.5)/10);
    }

    public void release() {
        appPreferences.unregisterOnSharedPreferenceChangeListener(this);

        releaseBGM();

    }


    private boolean mBGPaused;

    public int getBGMSongs() {
        return mBGMSongIds.length;
    }


    public void playBGMusic() {
        if (mBGPaused) {
            mBGMPlayer.start();
        } else {
            playRandomBGMusic();
        }

    }

    public void playRandomBGMusic() {
        playBGMusic(Utils.getRand(mBGMSongIds.length));

    }

    public void pauseBGMusic() {
        if (mBGMPlayer!=null && mBGMPlayer.isPlaying()) {
            mBGMPlayer.pause();
            mBGPaused = true;
        }
    }

    public void playBGMusic(final int which) {

        mBGPaused = false;

        mBGMVolume = appPreferences.getInt("music_volume", 90)/100.0f;

        Utils.async(new Runnable() {
            @Override
            public void run() {

                if (mBGMPlayer!=null) {
                    //pauseBGMusic();
                    mBGMPlayer.release();
                }


                mBGMPlayer = MediaPlayer.create(mContext, mBGMSongIds[which]);


                mBGMPlayer.setVolume(mBGMVolume,mBGMVolume);


                mBGMPlayer.setOnCompletionListener(oncomplete);


                mBGMPlayer.setOnErrorListener(onerr);

                mBGMPlayer.setLooping(true);

                mBGMPlayer.start();

            }
        });
    }



    public void releaseBGM() {
        if (mBGMPlayer!=null) {
            try {
                mBGMPlayer.stop();
            } catch( Exception e) { };
            mBGMPlayer.release();
            mBGMPlayer=null;
            mBGPaused = false;
        }
    }


    public void setBGMusicVolume(float vol) {
        mBGMVolume = vol;
        if (mBGMPlayer!=null) {
            mBGMPlayer.setVolume(mBGMVolume, mBGMVolume);
        }
    }

    private MediaPlayer.OnCompletionListener oncomplete = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
           // mediaPlayer.start();
        }
    };
    private MediaPlayer.OnErrorListener onerr = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            mBGMPlayer.release();
            mBGMPlayer = null;
            return false;
        }
    };

}