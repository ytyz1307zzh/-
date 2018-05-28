package com.plane.plane.game;

import android.graphics.Bitmap;

/**
 * 中飞机
 */

public class MiddlePlane extends EnemyPlane {
    public MiddlePlane(Bitmap bitmap) {
        super(bitmap);
        setPower(4);
        setValue(600);
    }
}
