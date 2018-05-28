package com.plane.plane.game;

import android.graphics.*;
/**
 * 非玩家物品
 */

public class NpcObject extends GameObject {
    private float speed = 2;
    public NpcObject(Bitmap bitmap) {
        super(bitmap);
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    public float getSpeed() {
        return speed;
    }
    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameview) {
        if(!isDestroyed()) {
            float a = gameview.getDensity();
            move(0, speed *a);
        }
    }
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){

            RectF canvasRecF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            RectF spriteRecF = getRectF();
            if(!RectF.intersects(canvasRecF, spriteRecF)){
                setDestroyed();
            }
        }
    }
}
