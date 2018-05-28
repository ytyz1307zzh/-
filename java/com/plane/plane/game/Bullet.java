package com.plane.plane.game;

import android.graphics.Bitmap;

/**
 * 单发子弹
 */

public class Bullet extends NpcObject {
    public Bullet(Bitmap bitmap){
        super(bitmap);
        setSpeed(-10);//负数表示子弹向上飞
    }
}
