package com.quaap.fishberserker.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.quaap.fishberserker.R;


/**
 * Created by tom on 2/10/17.
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
public class SliderPreference extends Preference {

    private Context mContext;

    int mProgress = 90;
    SeekBar seeker;
    String title;

    public SliderPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        setLayoutResource(R.layout.slider_preference_layout);
        title = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "title");

        for (int i=0; i<attrs.getAttributeCount(); i++) {
            Log.d("attrs", i + " " + attrs.getAttributeName(i) + " " + attrs.getAttributeValue(i));
        }

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ((TextView) view.findViewById(R.id.txtLabel)).setText(title);

        seeker = (SeekBar) view.findViewById(R.id.seekbar);

        seeker.setMax(100);
        seeker.setProgress(mProgress);
        seeker.setMinimumWidth(300);

        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
                if (fromUser) persistInt(prog);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }



    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getInt(index, 90));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {


        setValue(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);


    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != mProgress) {
            mProgress = value;
            notifyChanged();
        }
    }

}
