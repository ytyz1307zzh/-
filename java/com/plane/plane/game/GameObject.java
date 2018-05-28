package com.plane.plane.game;

/**
 * Created by 俊 on 2018/5/20.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * GameObject类，游戏中的所有需要绘制物体的父类
 */
public class GameObject {
    private boolean visible = true;
    private float x = 0;
    private float y = 0;
    private float collideOffset = 0;
    private boolean destroyed = false;
    private Bitmap bitmap = null;
    private int frame = 0;

    public GameObject(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public boolean getVisible() {
        return visible;
    }

    public int getFrame() {
        return frame;
    }

    public void setDestroyed() {
        bitmap = null;
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isVisible() {
        return visible;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getHeight() {
        if (bitmap != null)
            return bitmap.getHeight();
        return 0;
    }

    public float getWidth() {
        if (bitmap != null)
            return bitmap.getWidth();
        return 0;
    }

    public RectF getRectF() {
        float left = x;
        float up = y;
        float right = left + getWidth();
        float bottom = up + getHeight();
        RectF rectf = new RectF(left, up, right, bottom);
        return rectf;
    }

    public void move(float X, float Y) {
        x += X;
        y += Y;
    }

    public void moveTo(float X, float Y) {
        this.x = X;
        this.y = Y;
    }

    public void centreToXY(float X, float Y) {
        float width = getWidth();
        float height = getHeight();
        x = X - width / 2;
        y = Y - height / 2;
    }

    public Rect getBitmapSrcRec() {
        Rect rect = new Rect(0, 0, (int) getWidth(), (int) getHeight());
        return rect;
    }

    public RectF getCollideRectF() {
        RectF rectF = getRectF();
        rectF.left -= collideOffset;
        rectF.right += collideOffset;
        rectF.top -= collideOffset;
        rectF.bottom += collideOffset;
        return rectF;
    }

    public Point getCollidePoint(GameObject other) {
        Point point = null;
        RectF rectF_1 = getCollideRectF();
        RectF rectF_2 = other.getCollideRectF();
        RectF rectF = new RectF();
        boolean a = rectF.setIntersect(rectF_1, rectF_2);
        if (a) {
            point = new Point(Math.round(rectF.centerX()), Math.round(rectF.centerY()));
        }
        return point;
    }

    public final void draw(Canvas canvas, Paint paint, GameView gameview) {
        frame++;
        beforeDraw(canvas, paint, gameview);
        onDraw(canvas, paint, gameview);
        afterDraw(canvas, paint, gameview);
    }

    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameview) {
    }

    protected void afterDraw(Canvas canvas, Paint paint, GameView gameview) {

    }

    public void onDraw(Canvas canvas, Paint paint, GameView gameview) {
        if (!destroyed && this.bitmap != null && getVisible()) {
            RectF tarRectF = getRectF();
            Rect srcRect = getBitmapSrcRec();
            canvas.drawBitmap(bitmap, srcRect, tarRectF, paint);
        }
    }

}
