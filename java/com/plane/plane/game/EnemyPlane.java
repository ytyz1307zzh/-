package com.plane.plane.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.List;

/**
 * 敌人
 */

public class EnemyPlane extends NpcObject {
    private int power = 1;
    private int value = 0;
    private int type = 1;

    public EnemyPlane(Bitmap bitmap) {
        super(bitmap);
    }

    public void setPower(int power) {
        this.power = power;
        this.type = power;
    }

    public int getPower() {
        return power;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        super.afterDraw(canvas, paint, gameView);
        if (!isDestroyed()) {

            List<Bullet> bullets = gameView.getAliveBullets();
            for (Bullet bullet : bullets) {

                Point p = getCollidePoint(bullet);
                if (p != null) {
                    bullet.setDestroyed();
                    power--;
                    if (power <= 0) {

                        explode(gameView);
                        return;
                    }
                }
            }
        }
    }


    public void explode(GameView gameView) {

        float centerX = getX() + getWidth() / 2;
        float centerY = getY() + getHeight() / 2;
        Bitmap bitmap = gameView.getExplosionBitmap(type);
        Explosion explosion = new Explosion(bitmap, type);
        explosion.centreToXY(centerX, centerY);
        gameView.addGameObject(explosion);

        gameView.addScore(value);
        setDestroyed();
    }
}
