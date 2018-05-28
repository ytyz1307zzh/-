package com.plane.plane.game;

import android.graphics.Bitmap;
//大飞机

public class BigPlane extends EnemyPlane {
    public BigPlane(Bitmap bitmap) {
        super(bitmap);
        setPower(10);
        setValue(3000);
    }
}
