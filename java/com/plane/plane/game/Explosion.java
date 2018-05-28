package com.plane.plane.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

//爆炸效果
public class Explosion extends GameObject {
    private int segment = 0;
    private int level = 0;
    private int explodeFrequency = 2;

    public Explosion(Bitmap bitmap, int type) {
        super(bitmap);
        switch (type) {
            case 1:
                segment = 3;
                break;
            case 4:
                segment = 4;
                break;
            case 10:
                segment = 6;
                break;
            case 0:
                segment = 4;
                break;
        }
    }

    @Override
    public float getWidth() {
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            return bitmap.getWidth() / segment;
        }
        return 0;
    }

    @Override
    public Rect getBitmapSrcRec() {
        Rect rect = super.getBitmapSrcRec();
        int left = (int) (level * getWidth());
        rect.offsetTo(left, 0);
        return rect;
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if (!isDestroyed()) {
            if (getFrame() % explodeFrequency == 0) {

                level++;
                if (level >= segment) {

                    setDestroyed();
                }
            }
        }
    }

    public int getExplodeDurationFrame() {
        return segment * explodeFrequency;
    }
}
