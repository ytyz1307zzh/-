package com.plane.plane.game;

import android.graphics.Bitmap;

/**
 * 小飞机
 */

public class LittlePlane extends EnemyPlane {
    public LittlePlane(Bitmap bitmap) {
        super(bitmap);
        setPower(1);
        setValue(100);
    }
}
