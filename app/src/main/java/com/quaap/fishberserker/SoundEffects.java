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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class SoundEffects {

    private SoundPool mSounds;

    private int[] mBGMSongIds;
    private MediaPlayer mBGMPlayer;
    private MediaPlayer mBGMPlayerNext;

    private float mBGMVol = 1;


    private Map<Integer,Integer> mSoundIds = new HashMap<>();



    private int [] soundFiles;

    private float [] soundVolumes;
    private String [] soundUses;


    private SharedPreferences appPreferences;

    private volatile boolean mReady = false;

    private volatile boolean mMute = false;

    private float mBGMVolume = .3f;

    private Context mContext;

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


        mBGMSongIds = getResIdArray(context, R.array.songs);

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
    }

    private int[] getResIdArray(Context context, int id) {
        final TypedArray idsarr = context.getResources().obtainTypedArray(id);
        int [] ids = new int[idsarr.length()];
        for (int i=0; i<idsarr.length(); i++) {
            ids[i] = idsarr.getResourceId(i, 0);
        }
        idsarr.recycle();
        return ids;
    }

    public int getBGMSongs() {
        return mBGMSongIds.length;
    }

    public void playRandomBGMusic() {
        playBGMusic(Utils.getRand(mBGMSongIds.length));

    }
    public void playBGMusic(final int which) {

        Utils.async(new Runnable() {
            @Override
            public void run() {

                if (mBGMPlayer!=null) {
                    //pauseBGMusic();
                    mBGMPlayer.release();
                }
                if (mBGMPlayerNext!=null) {
                    mBGMPlayerNext.release();
                }

                mBGMPlayer = MediaPlayer.create(mContext, mBGMSongIds[which]);
                mBGMPlayerNext = MediaPlayer.create(mContext, mBGMSongIds[which]);

                mBGMPlayer.setNextMediaPlayer(mBGMPlayerNext);
                mBGMPlayerNext.setNextMediaPlayer(mBGMPlayer);

                mBGMPlayer.setVolume(mBGMVolume,mBGMVolume);
                mBGMPlayerNext.setVolume(mBGMVolume,mBGMVolume);

                mBGMPlayer.setOnCompletionListener(oncomplete);
                mBGMPlayerNext.setOnCompletionListener(oncomplete);

                mBGMPlayer.setOnErrorListener(onerr);
                mBGMPlayerNext.setOnErrorListener(onerr);


                mBGMPlayer.start();

            }
        });
    }


    Handler h = new Handler();

    private MediaPlayer.OnCompletionListener oncomplete = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (!((App)mContext.getApplicationContext()).check(mContext)) {
                mediaPlayer.release();
                mBGMPlayer.release();
                mBGMPlayerNext.release();
                return;
            }
            h.post(new Runnable() {
                @Override
                public void run() {

                }
            });
            mediaPlayer.seekTo(0);
        }
    };

    private MediaPlayer.OnErrorListener onerr = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            mediaPlayer.release();
            mBGMPlayer.release();
            mBGMPlayerNext.release();
            mBGMPlayer = null;
            mBGMPlayerNext = null;
            return false;
        }
    };


    public void pauseBGMusic() {
        if (mBGMPlayer!=null && mBGMPlayer.isPlaying()) {
            mBGMPlayer.pause();
        }
    }

    public void restartBGMusic() {
        pauseBGMusic();
        if (mBGMPlayer!=null) {
            mBGMPlayer.seekTo(0);
            mBGMPlayer.start();

        }
    }

    public void releaseBGM() {
        if (mBGMPlayer!=null) {
            try {
                mBGMPlayer.setNextMediaPlayer(null);
                mBGMPlayer.stop();
            } catch( Exception e) { };
            mBGMPlayer.release();
            mBGMPlayer=null;
        }
        if (mBGMPlayerNext!=null) {
            try {
                mBGMPlayerNext.setNextMediaPlayer(null);
                mBGMPlayerNext.stop();
            } catch( Exception e) { };
            mBGMPlayerNext.release();
            mBGMPlayerNext=null;
        }
    }

    public void deltaBGMusicVolume(float volchange) {
        float newvol = mBGMVol+volchange;
        if (newvol>=0 || newvol <=1) {
            setBGMusicVolume(newvol);
        }
    }
    public void setBGMusicVolume(float vol) {
        mBGMVol = vol;
        if (mBGMPlayer!=null) {
            mBGMPlayer.setVolume(mBGMVol, mBGMVol);
        }
        if (mBGMPlayerNext!=null) {
            mBGMPlayerNext.setVolume(mBGMVol, mBGMVol);
        }
    }

    public void setBGMusicVolumeTemp(float vol, long timeoutmillis) {
        setBGMusicVolume(vol);
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                setBGMusicVolume(mBGMVol);
                t.cancel();
            }
        }, timeoutmillis);
    }


    private boolean isReady() {
        return mReady;
    }

    public void setMute(boolean mute) {
        mMute = mute;
        if (mMute) pauseBGMusic();
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
        try {
            if (isReady() && !mMute && appPreferences.getBoolean("use_sound_effects", true)) {

                float vol = soundVolumes[soundKey] + getRandHundreth();
                mSounds.play(mSoundIds.get(soundKey), vol, vol, 1, loop, speed);
                Log.d("sfx", soundKey + " key");
            }
        } catch (Exception e) {
            Log.e("SoundEffects", "Error playing " + soundKey, e);
        }
    }

    public void playGood() {
        List<Integer> goods = new ArrayList<>();
        for (int i=0; i<soundUses.length; i++) {
            if (soundUses[i].equals("good")) {
                goods.add(i);
            }
        }

        play(goods.get(Utils.getRand(goods.size())));
    }

    public void playBad() {
        List<Integer> bads = new ArrayList<>();
        for (int i=0; i<soundUses.length; i++) {
            if (soundUses[i].equals("bad")) {
                bads.add(i);
            }
        }

        play(bads.get(Utils.getRand(bads.size())));
    }


    public void playLoop() {
        List<Integer> loops = new ArrayList<>();
        for (int i=0; i<soundUses.length; i++) {
            if (soundUses[i].equals("loop")) {
                loops.add(i);
            }
        }

        loop(loops.get(Utils.getRand(loops.size())), Utils.getRandInt(1,4));
    }

//    public boolean isLooping() {
//       // mSounds.
//    }

    private float getRandHundreth() {
        return (float)((Math.random()-.5)/10);
    }



    public void release() {
        releaseBGM();
        mSounds.release();
    }


}