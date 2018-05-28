package com.plane.plane.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
/**
 * 奖励
 */

public class AwardObject extends NpcObject {
    public static int STATUS_DOWN1 = 1;
    public static int STATUS_UP2 = 2;
    public static int STATUS_DOWN3 = 3;

    private int status = STATUS_DOWN1;

    public AwardObject(Bitmap bitmap){
        super(bitmap);
        setSpeed(7);
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {

        if(!isDestroyed()){

            int canvasHeight = canvas.getHeight();
            if(status != STATUS_DOWN3){
                float maxY = getY() + getHeight();
                if(status == STATUS_DOWN1){

                    if(maxY >= canvasHeight * 0.25){

                        setSpeed(-5);
                        status = STATUS_UP2;
                    }
                }
                else if(status == STATUS_UP2){

                    if(maxY+this.getSpeed() <= 0){

                        setSpeed(13);
                        status = STATUS_DOWN3;
                    }
                }
            }
            if(status == STATUS_DOWN3){
                if(getY() >= canvasHeight){
                    setDestroyed();
                }
            }
        }
    }
}
