package com.quaap.fishberserker;

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
}
