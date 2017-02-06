package com.quaap.fishberserker;

import android.app.Application;
import android.content.Context;

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
public class App extends Application {

    public static App getInstance(Context context) {
        return (App)context.getApplicationContext();
    }

    private SoundEffects mSoundEffects;


    public SoundEffects getSoundEffects() {
        return mSoundEffects;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSoundEffects = new SoundEffects(this);


    }

    @Override
    public void onTerminate() {
        mSoundEffects.release();
        super.onTerminate();
    }
}
