package com.quaap.fishberserker;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by tom on 2/3/17.
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
public class Utils {
    public static int getRand(int max) {
        return (int) (max*Math.random());
    }

    public static double getRand(double min, double max) {
        return (max-min)*Math.random() + min;
    }

    public static int getRandInt(int min, int max) {
        return (int)((max-min)*Math.random() + min);
    }


    public static Bitmap trimImage(Bitmap image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int left = 0;
        int top = 0;
        int right = width - 1;
        int bottom = height - 1;
        int minRight = width - 1;
        int minBottom = height - 1;

        TOP:
        for (;top < bottom; top++){
            for (int x = 0; x < width; x++){
                if (Color.alpha(image.getPixel(x, top)) != 0){
                    minRight = x;
                    minBottom = top;
                    break TOP;
                }
            }
        }

        LEFT:
        for (;left < minRight; left++){
            for (int y = height - 1; y > top; y--){
                if (Color.alpha(image.getPixel(left, y)) != 0){
                    minBottom = y;
                    break LEFT;
                }
            }
        }

        BOTTOM:
        for (;bottom > minBottom; bottom--){
            for (int x = width - 1; x >= left; x--){
                if (Color.alpha(image.getPixel(x, bottom)) != 0){
                    minRight = x;
                    break BOTTOM;
                }
            }
        }

        right:
        for (;right > minRight; right--){
            for (int y = bottom; y >= top; y--){
                if (Color.alpha(image.getPixel(right, y)) != 0){
                    break right;
                }
            }
        }

        return Bitmap.createBitmap(image, left, top, right - left + 1, bottom - top + 1);
    }
}
