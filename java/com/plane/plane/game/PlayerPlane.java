package com.plane.plane.game;

import android.graphics.*;

import java.util.List;
//玩家飞机

public class PlayerPlane extends GameObject {
    private boolean collide = false;
    private int bombAwardCount = 0;

    private boolean single = true;
    private int doubleTime = 0;
    private int maxDoubleTime = 140;

    private long beginFlushFrame = 0;
    private int flushTime = 0;
    private int flushFrequency = 16;
    private int maxFlushTime = 10;

    public PlayerPlane(Bitmap bitmap) {
        super(bitmap);
    }

    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if (!isDestroyed()) {

            validatePosition(canvas);


            if (getFrame() % 7 == 0) {
                fight(gameView);
            }
        }
    }

    private void validatePosition(Canvas canvas) {
        if (getX() < 0) {
            setX(0);
        }
        if (getY() < 0) {
            setY(0);
        }
        RectF rectF = getRectF();
        int canvasWidth = canvas.getWidth();
        if (rectF.right > canvasWidth) {
            setX(canvasWidth - getWidth());
        }
        int canvasHeight = canvas.getHeight();
        if (rectF.bottom > canvasHeight) {
            setY(canvasHeight - getHeight());
        }
    }


    public void fight(GameView gameView) {

        if (collide || isDestroyed()) {
            return;
        }

        float x = getX() + getWidth() / 2;
        float y = getY() - 5;
        if (single) {
            //单发模式
            Bitmap yellowBulletBitmap = gameView.getYellowBulletBitmap();
            Bullet yellowBullet = new Bullet(yellowBulletBitmap);
            yellowBullet.moveTo(x, y);
            gameView.addGameObject(yellowBullet);
        } else {
            //双发模式
            float offset = getWidth() / 4;
            float leftX = x - offset;
            float rightX = x + offset;
            Bitmap blueBulletBitmap = gameView.getBlueBulletBitmap();

            Bullet leftBlueBullet = new Bullet(blueBulletBitmap);
            leftBlueBullet.moveTo(leftX, y);
            gameView.addGameObject(leftBlueBullet);

            Bullet rightBlueBullet = new Bullet(blueBulletBitmap);
            rightBlueBullet.moveTo(rightX, y);
            gameView.addGameObject(rightBlueBullet);

            doubleTime++;
            if (doubleTime >= maxDoubleTime) {
                single = true;
                doubleTime = 0;
            }
        }
    }


    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if (isDestroyed()) {
            return;
        }

        if (!collide) {
            List<EnemyPlane> enemies = gameView.getAliveEnemyPlanes();
            for (EnemyPlane enemyPlane : enemies) {
                Point p = getCollidePoint(enemyPlane);
                if (p != null) {

                    explode(gameView);
                    break;
                }
            }
        }

        if (beginFlushFrame > 0) {
            long frame = getFrame();

            if (frame >= beginFlushFrame) {
                if ((frame - beginFlushFrame) % flushFrequency == 0) {
                    boolean visible = getVisible();
                    setVisible(!visible);
                    flushTime++;
                    if (flushTime >= maxFlushTime) {

                        setDestroyed();

                    }
                }
            }
        }


        if (!collide) {

            List<bombAward> bombAwards = gameView.getAlivebombAwards();
            for (bombAward bombAward : bombAwards) {
                Point p = getCollidePoint(bombAward);
                if (p != null) {
                    bombAwardCount++;
                    bombAward.setDestroyed();

                }
            }


            List<BulletAward> bulletAwards = gameView.getAliveBulletAwards();
            for (BulletAward bulletAward : bulletAwards) {
                Point p = getCollidePoint(bulletAward);
                if (p != null) {
                    bulletAward.setDestroyed();
                    single = false;
                    doubleTime = 0;
                }
            }
        }
    }

    //爆炸
    private void explode(GameView gameView) {
        if (!collide) {
            collide = true;
            setVisible(false);
            float centerX = getX() + getWidth() / 2;
            float centerY = getY() + getHeight() / 2;
            Explosion explosion = new Explosion(gameView.getExplosionBitmap(0), 0);
            explosion.centreToXY(centerX, centerY);
            gameView.addGameObject(explosion);
            beginFlushFrame = getFrame() + explosion.getExplodeDurationFrame();
        }
    }

    public int getBombCount() {
        return bombAwardCount;
    }

    public void bomb(GameView gameView) {
        if (collide || isDestroyed()) {
            return;
        }

        if (bombAwardCount > 0) {
            List<EnemyPlane> enemyPlanes = gameView.getAliveEnemyPlanes();
            for (EnemyPlane enemyPlane : enemyPlanes) {
                enemyPlane.explode(gameView);
            }
            bombAwardCount--;
        }
    }

}
