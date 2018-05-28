package com.plane.plane.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.plane.plane.R;

import java.io.*;
import java.util.*;

/**
 * View
 */

public class GameView extends View {
    //游戏的各个状态
    public static final int GAME_START = 1;
    public static final int GAME_PAUSE = 2;
    public static final int GAME_OVER = 3;
    public static final int GAME_DESTROYED = 4;
    private int status = GAME_DESTROYED;
    //touch事件
    private static final int MOVE = 1;
    private static final int SIGLE = 2;
    private static final int DOUBLE = 3;
    private static final int SINGLE_CLICK_DELTA = 200;
    private static final int DOUBLE_CLICK_DELTA = 300;
    //构图要素
    private Paint paint;
    private Paint textPaint;
    private PlayerPlane playerPlane = null;
    private List<GameObject> gameobjects = new ArrayList<GameObject>();
    //bitmaps
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    //需要绘制的东西
    private List<GameObject> gameobjectsNeedToDraw = new ArrayList<GameObject>();
    //游戏得分与绘制帧数,图形尺寸
    private long frame = 0;
    private long scores = 0;
    private long highestScore=-1;
    private long startTime=0;
    private int frequency=100;
    private float fontSizeText = 12;
    private float fontSizeDialogText = 20;
    private float borderSize = 2;
    private Rect continueRect = new Rect();
    private float density = getResources().getDisplayMetrics().density;

    private long lastSingleClickTime = -1;
    private long downTime = -1;
    private long upTime = -1;
    private float X = -1;
    private float Y = -1;

    private void init(AttributeSet attributeSet, int style) {
        final TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.GameView, style, 0);
        a.recycle();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        textPaint.setColor(0xff000000);
        fontSizeText = textPaint.getTextSize();
        fontSizeDialogText *= density;
        fontSizeText *= density;
        borderSize *= density;
        textPaint.setTextSize(fontSizeText);
    }

    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attributeSet, int style) {
        super(context, attributeSet, style);
        init(attributeSet, style);
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet, 0);
    }

    //游戏状态控制
    public void start(int[] bitmapIds) {
        destroy();
        for (int bitmapId : bitmapIds) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapId);
            bitmaps.add(bitmap);
        }
        //start game
        setGameStart();
    }

    public void setGameStart() {
        playerPlane = new PlayerPlane(bitmaps.get(0));
        status = GAME_START;
        startTime=System.currentTimeMillis();
        postInvalidate();
    }

    public void setGamePause() {
        status = GAME_PAUSE;
    }

    public void setGameResume() {
        status = GAME_START;
        //
        postInvalidate();
    }

    public void reStart() {
        destroyNotRecyleBitmaps();
        frequency=100;
        setGameStart();
    }

    public long getScores() {
        return scores;
    }

    //绘图
    @Override
    protected void onDraw(Canvas canvas) {
        if (isSingleClick()) {
            onSingleClick(X, Y);
        }
        super.onDraw(canvas);
        if (status == GAME_START)
            drawGameStarted(canvas);
        else if (status == GAME_PAUSE)
            drawGamePaused(canvas);
        else if (status == GAME_OVER)
            drawGameOver(canvas);
    }

    private void drawGameStarted(Canvas canvas) {
        drawScoreAndBombs(canvas);
        if (frame == 0) {
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() - playerPlane.getHeight() / 2;
            playerPlane.centreToXY(centerX, centerY);
        }
        if (gameobjectsNeedToDraw.size() > 0) {
            gameobjects.addAll(gameobjectsNeedToDraw);
            gameobjectsNeedToDraw.clear();
        }
        //随着时间增长，飞机数目越来越多
        long currentTime=System.currentTimeMillis();
        long timeGap=Math.round((currentTime-startTime)/1000);
        if(timeGap%8==0 && timeGap>0 && frequency>20 && frequency>=100-10*(timeGap/8-1))
            frequency-=10;

        destroyBulletsFrontOfCombatAircraft();
        removeDestroyedgameobjects();

        if (frame % frequency == 0) {
            createRandomgameobjects(canvas.getWidth());
        }
        frame++;
        Iterator<GameObject> iterator = gameobjects.iterator();
        while (iterator.hasNext()) {
            GameObject tempObj = iterator.next();
            if (!tempObj.isDestroyed()) {
                tempObj.draw(canvas, paint, this);
            }
            if (tempObj.isDestroyed()) {
                iterator.remove();
            }
        }
        if (playerPlane != null) {
            playerPlane.draw(canvas, paint, this);
            if (playerPlane.isDestroyed()) {
                status = GAME_OVER;
            }
        }
        postInvalidate();
    }

    //暂停
    private void drawGamePaused(Canvas canvas) {
        drawScoreAndBombs(canvas);
        for (GameObject s : gameobjects) {
            s.onDraw(canvas, paint, this);
        }
        if (playerPlane != null) {
            playerPlane.onDraw(canvas, paint, this);
        }
        drawScoreDialog(canvas, "继续游戏", false);

        if (lastSingleClickTime > 0) {
            postInvalidate();
        }
    }

    //gameover
    private void drawGameOver(Canvas canvas) {
        drawScoreDialog(canvas, "重新开始", true);
        if (lastSingleClickTime > 0) {
            postInvalidate();
        }
    }

    private void drawScoreDialog(Canvas canvas, String operation, boolean gameOver) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        float originalFontSize = textPaint.getTextSize();
        Paint.Align originalFontAlign = textPaint.getTextAlign();
        int originalColor = paint.getColor();
        Paint.Style originalStyle = paint.getStyle();
        /*
        W = 360
        w1 = 20
        w2 = 320
        buttonWidth = 140
        buttonHeight = 42
        H = 558
        h1 = 150
        h2 = 60
        h3 = 94
        h4= 30
        h5 = 76
        */
        int w1 = (int) (20.0 / 360.0 * canvasWidth);
        int w2 = canvasWidth - 2 * w1;
        int buttonWidth = (int) (140.0 / 360.0 * canvasWidth);

        int h1 = (int) (150.0 / 558.0 * canvasHeight);
        int h2 = (int) (60.0 / 558.0 * canvasHeight);
        int h3 = (int) (94.0 / 558.0 * canvasHeight);
        int h4 = (int) (30.0 / 558.0 * canvasHeight);
        int h5 = (int) (76.0 / 558.0 * canvasHeight);
        int buttonHeight = (int) (42.0 / 558.0 * canvasHeight);

        canvas.translate(w1, h1);
        //绘制背景色
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFD7DDDE);
        Rect rect1 = new Rect(0, 0, w2, canvasHeight - 2 * h1);
        canvas.drawRect(rect1, paint);
        //绘制边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF515151);
        paint.setStrokeWidth(borderSize);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawRect(rect1, paint);
        ReadHighestScore();
        if(getScores()<getHighestScore()||!gameOver) {//没有超过最高分
            //绘制文本"飞机大战分数"
            textPaint.setTextSize(fontSizeDialogText);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("飞机大战分数", w2 / 2, (h2 - fontSizeDialogText) / 2 + fontSizeDialogText, textPaint);
            //绘制"飞机大战分数"下面的横线
            canvas.translate(0, h2);
            canvas.drawLine(0, 0, w2, 0, paint);
            //绘制实际的分数
            String allScore = String.valueOf(getScores());
            canvas.drawText(allScore, w2 / 2, (h3 - fontSizeDialogText) / 2 + fontSizeDialogText, textPaint);
            //绘制最高分
            String historyScore = "历史最高分: ";
            if(getHighestScore()>=0)
                historyScore += String.valueOf(getHighestScore());
            else//highestScore==-1, 就不输出-1了
                historyScore += String.valueOf(0);
            canvas.drawText(historyScore, w2 / 2 - historyScore.length() / 2, h3 + (h4 - fontSizeDialogText) / 2 + fontSizeDialogText, textPaint);
        }
        else{
            //绘制文本"历史最高分"
            textPaint.setTextSize(fontSizeDialogText);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("历史最高分！", w2 / 2, (h2 - fontSizeDialogText) / 2 + fontSizeDialogText, textPaint);
            //绘制"飞机大战分数"下面的横线
            canvas.translate(0, h2);
            canvas.drawLine(0, 0, w2, 0, paint);
            //绘制实际的分数
            String allScore = String.valueOf(getScores());
            canvas.drawText(allScore, w2 / 2, (h3+h4 - fontSizeDialogText) / 2 + fontSizeDialogText, textPaint);
            WriteHighestScore((int)getScores());
        }
        //绘制分数下面的横线
        canvas.translate(0, h3+h4);
        canvas.drawLine(0, 0, w2, 0, paint);
        //绘制按钮边框
        Rect rect2 = new Rect();
        rect2.left = (w2 - buttonWidth) / 2;
        rect2.right = w2 - rect2.left;
        rect2.top = (h5 - buttonHeight) / 2;
        rect2.bottom = h5 - rect2.top;
        canvas.drawRect(rect2, paint);
        //绘制文本"继续"或"重新开始"
        canvas.translate(0, rect2.top);
        canvas.drawText(operation, w2 / 2, (buttonHeight - fontSizeDialogText) / 2 + fontSizeDialogText, textPaint);
        continueRect = new Rect(rect2);
        continueRect.left = w1 + rect2.left;
        continueRect.right = continueRect.left + buttonWidth;
        continueRect.top = h1 + h2 + h3 + h4 + rect2.top;
        continueRect.bottom = continueRect.top + buttonHeight;
        //重置
        textPaint.setTextSize(originalFontSize);
        textPaint.setTextAlign(originalFontAlign);
        paint.setColor(originalColor);
        paint.setStyle(originalStyle);
    }

    private void drawScoreAndBombs(Canvas canvas) {
        Bitmap pauseBitmap = status == GAME_START ? bitmaps.get(9) : bitmaps.get(10);
        RectF pauseBitmapDstRecF = getPauseBitmapDstRecF();
        float pauseLeft = pauseBitmapDstRecF.left;
        float pauseTop = pauseBitmapDstRecF.top;
        canvas.drawBitmap(pauseBitmap, pauseLeft, pauseTop, paint);
        float scoreLeft = pauseLeft + pauseBitmap.getWidth() + 20 * density;
        float scoreTop = fontSizeText + pauseTop + pauseBitmap.getHeight() / 2 - fontSizeText / 2;
        canvas.drawText(scores + "", scoreLeft, scoreTop, textPaint);
        /*
        float freLeft = scoreLeft + pauseBitmap.getWidth() + 20 * density;
        float freTop = fontSizeText + pauseTop + pauseBitmap.getHeight() / 2 - fontSizeText / 2;
        canvas.drawText(frequency + "", freLeft, freTop, textPaint);
        */
        if (playerPlane != null && !playerPlane.isDestroyed()) {
            int bombCount = playerPlane.getBombCount();
            if (bombCount > 0) {
                Bitmap bombBitmap = bitmaps.get(11);
                float bombTop = canvas.getHeight() - bombBitmap.getHeight();
                canvas.drawBitmap(bombBitmap, 0, bombTop, paint);
                float bombCountLeft = bombBitmap.getWidth() + 10 * density;
                float bombCountTop = fontSizeDialogText + bombTop + bombBitmap.getHeight() / 2 - fontSizeText / 2;
                canvas.drawText("X " + bombCount, bombCountLeft, bombCountTop, textPaint);
            }
        }
    }

    private void destroyBulletsFrontOfCombatAircraft() {
        if (playerPlane != null) {
            float aircraftY = playerPlane.getY();
            List<Bullet> aliveBullets = getAliveBullets();
            for (Bullet bullet : aliveBullets) {
                if (aircraftY <= bullet.getY()) {
                    bullet.setDestroyed();
                }
            }
        }
    }

    private void removeDestroyedgameobjects() {
        Iterator<GameObject> iterator = gameobjects.iterator();
        while (iterator.hasNext()) {
            GameObject s = iterator.next();
            if (s.isDestroyed()) {
                iterator.remove();
            }
        }
    }


    private void createRandomgameobjects(int canvasWidth) {
        GameObject gameobject = null;
        int speed = 2;

        int callTime = Math.round(frame / 30);
        if ((callTime + 1) % 25 == 0) {

            if ((callTime + 1) % 50 == 0) {

                gameobject = new bombAward(bitmaps.get(7));
            } else {

                gameobject = new BulletAward(bitmaps.get(8));
            }
        } else {

            int[] nums = {0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2};
            int index = (int) Math.floor(nums.length * Math.random());
            int type = nums[index];
            if (type == 0) {

                gameobject = new LittlePlane(bitmaps.get(4));
            } else if (type == 1) {

                gameobject = new MiddlePlane(bitmaps.get(5));
            } else if (type == 2) {

                gameobject = new BigPlane(bitmaps.get(6));
            }
            if (type != 2) {
                if (Math.random() < 0.33) {
                    speed = 4;
                }
            }
        }

        if (gameobject != null) {
            float GameObjectWidth = gameobject.getWidth();
            float GameObjectHeight = gameobject.getHeight();
            float x = (float) ((canvasWidth - GameObjectWidth) * Math.random());
            float y = -GameObjectHeight;
            gameobject.setX(x);
            gameobject.setY(y);
            if (gameobject instanceof NpcObject) {
                NpcObject npcObject = (NpcObject) gameobject;
                npcObject.setSpeed(speed);
            }
            addGameObject(gameobject);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int touchType = resolveTouchType(event);
        if (status == GAME_START) {
            if (touchType == MOVE) {
                if (playerPlane != null) {
                    playerPlane.centreToXY(X, Y);
                }
            } else if (touchType == DOUBLE) {
                if (status == GAME_START) {
                    if (playerPlane != null) {

                        playerPlane.bomb(this);
                    }
                }
            }
        } else if (status == GAME_PAUSE) {
            if (lastSingleClickTime > 0) {
                postInvalidate();
            }
        } else if (status == GAME_OVER) {
            if (lastSingleClickTime > 0) {
                postInvalidate();
            }
        }
        return true;
    }


    private int resolveTouchType(MotionEvent event) {
        int touchType = -1;
        int action = event.getAction();
        X = event.getX();
        Y = event.getY();
        if (action == MotionEvent.ACTION_MOVE) {
            long deltaTime = System.currentTimeMillis() - downTime;
            if (deltaTime > SINGLE_CLICK_DELTA) {

                touchType = MOVE;
            }
        } else if (action == MotionEvent.ACTION_DOWN) {

            downTime = System.currentTimeMillis();
        } else if (action == MotionEvent.ACTION_UP) {

            upTime = System.currentTimeMillis();

            long downUpDurationTime = upTime - downTime;

            if (downUpDurationTime <= SINGLE_CLICK_DELTA) {

                long twoClickDurationTime = upTime - lastSingleClickTime;

                if (twoClickDurationTime <= DOUBLE_CLICK_DELTA) {

                    touchType = DOUBLE;

                    lastSingleClickTime = -1;
                    downTime = -1;
                    upTime = -1;
                } else {

                    lastSingleClickTime = upTime;
                }
            }
        }
        return touchType;
    }


    private boolean isSingleClick() {
        boolean singleClick = false;

        if (lastSingleClickTime > 0) {

            long deltaTime = System.currentTimeMillis() - lastSingleClickTime;

            if (deltaTime >= DOUBLE_CLICK_DELTA) {

                singleClick = true;

                lastSingleClickTime = -1;
                downTime = -1;
                upTime = -1;
            }
        }
        return singleClick;
    }

    private void onSingleClick(float x, float y) {
        if (status == GAME_START) {
            if (isClickPause(x, y)) {

                setGamePause();
            }
        } else if (status == GAME_PAUSE) {
            if (isClickContinueButton(x, y)) {

                setGameResume();
            }
        } else if (status == GAME_OVER) {
            if (isClickRestartButton(x, y)) {

                reStart();
            }
        }
    }


    private boolean isClickPause(float x, float y) {
        RectF pauseRecF = getPauseBitmapDstRecF();
        return pauseRecF.contains(x, y);
    }


    private boolean isClickContinueButton(float x, float y) {
        return continueRect.contains((int) x, (int) y);
    }


    private boolean isClickRestartButton(float x, float y) {
        return continueRect.contains((int) x, (int) y);
    }

    private RectF getPauseBitmapDstRecF() {
        Bitmap pauseBitmap = status == GAME_START ? bitmaps.get(9) : bitmaps.get(10);
        RectF recF = new RectF();
        recF.left = 15 * density;
        recF.top = 15 * density;
        recF.right = recF.left + pauseBitmap.getWidth();
        recF.bottom = recF.top + pauseBitmap.getHeight();
        return recF;
    }


    private void destroyNotRecyleBitmaps() {

        status = GAME_OVER;


        frame = 0;


        scores = 0;

        if (playerPlane != null) {
            playerPlane.setDestroyed();
        }
        playerPlane = null;


        for (GameObject s : gameobjects) {
            s.setDestroyed();
        }
        gameobjects.clear();
    }

    private void WriteHighestScore(int highestScore) {
        File file = new File(this.getContext().getFilesDir(),"HighestScore.txt");
        try {
            DataOutputStream output =new DataOutputStream(new FileOutputStream(file));
            output.writeInt(highestScore);
            output.close();
        }
        catch (FileNotFoundException e){
            System.err.println("Fail to find file: "+this.getContext().getFilesDir()+"HighestScore.txt");
        }
        catch(IOException e){
            System.err.println("IOException");
        }
    }

    private void ReadHighestScore() {
        File file = new File(this.getContext().getFilesDir(),"HighestScore.txt");
        try {
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            try {
                highestScore = input.readInt();
            } catch (IOException e) {
            }
            input.close();
        }
        catch (FileNotFoundException e){
            System.err.println("Fail to find file: "+this.getContext().getFilesDir()+"HighestScore.txt");
        }
        catch(IOException e){
            System.err.println("IOException");
        }
    }

    public void destroy() {
        destroyNotRecyleBitmaps();


        for (Bitmap bitmap : bitmaps) {
            bitmap.recycle();
        }
        bitmaps.clear();
    }


    public void addGameObject(GameObject gameObject) {
        gameobjectsNeedToDraw.add(gameObject);
    }

    public void addScore(int value) {
        scores += value;
    }

    public long getHighestScore(){
        return highestScore;
    }

    public float getDensity() {
        return density;
    }

    public Bitmap getYellowBulletBitmap() {
        return bitmaps.get(2);
    }

    public Bitmap getBlueBulletBitmap() {
        return bitmaps.get(3);
    }

    public Bitmap getExplosionBitmap(int type) {
        switch (type) {
            case 1:
                return bitmaps.get(12);
            case 4:
                return bitmaps.get(13);
            case 10:
                return bitmaps.get(14);
            case 0:
                return bitmaps.get(15);
        }
        return bitmaps.get(1);
    }


    public List<EnemyPlane> getAliveEnemyPlanes() {
        List<EnemyPlane> enemyPlanes = new ArrayList<EnemyPlane>();
        for (GameObject s : gameobjects) {
            if (!s.isDestroyed() && s instanceof EnemyPlane) {
                EnemyPlane gameobject = (EnemyPlane) s;
                enemyPlanes.add(gameobject);
            }
        }
        return enemyPlanes;
    }


    public List<bombAward> getAlivebombAwards() {
        List<bombAward> bombAwards = new ArrayList<bombAward>();
        for (GameObject s : gameobjects) {
            if (!s.isDestroyed() && s instanceof bombAward) {
                bombAward bombaward = (bombAward) s;
                bombAwards.add(bombaward);
            }
        }
        return bombAwards;
    }

    public List<BulletAward> getAliveBulletAwards() {
        List<BulletAward> bulletAwards = new ArrayList<BulletAward>();
        for (GameObject s : gameobjects) {
            if (!s.isDestroyed() && s instanceof BulletAward) {
                BulletAward bulletAward = (BulletAward) s;
                bulletAwards.add(bulletAward);
            }
        }
        return bulletAwards;
    }

    public List<Bullet> getAliveBullets() {
        List<Bullet> bullets = new ArrayList<Bullet>();
        for (GameObject s : gameobjects) {
            if (!s.isDestroyed() && s instanceof Bullet) {
                Bullet bullet = (Bullet) s;
                bullets.add(bullet);
            }
        }
        return bullets;
    }

}
