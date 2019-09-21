package android.example.pongactivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

class pongGame extends SurfaceView implements Runnable {

    private boolean debugging = true;
    //used for drawing
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private Paint mPaint;
    //Current FPS
    private long mFPS;
    //# of milliseconds in a second
    private final int MS_PER_S = 1000;
    //resolution of the screen
    private int mScreenX;
    private int mScreenY;
    //size of text
    private int mFontSize;
    private int mMarginSize;
    //game objects
    private bat mBat;
    private ball mBall;
    private debugBox mDebug;
    //score and lives
    private int mScore = 0;
    private int mLives = 3;
    private boolean game_over = false;

    //Threading objects
    private Thread mGameThread;
    private volatile boolean mPlaying;
    private boolean mPaused = true; //player controlled pause of the game

    //sound objects
    SoundPool mSp;
    private int bat_hit = -1;
    private int wall_hit = -1;
    private int miss = -1;
    private int select = -1;


    public pongGame(Context context, int x, int y) {
        super(context); //sends the context to the parent class constructor of SurfaceView

        mScreenX = x;
        mScreenY = y;
        //font size will be 5% of screen width, margin will be 2%
        mFontSize = mScreenX/20;
        mMarginSize = mScreenX/50;
        //initialize objects for drawing
        mSurfaceHolder = getHolder();   //PongGame is a surfaceView, getHolder() is a surfaceView method
        mPaint = new Paint();
        //initialize bat and ball
        mBall = new ball(getContext(), mScreenX);
        mBat = new bat(getContext(), mScreenX, mScreenY);


        //initialize sound pool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //initialize soundpool with audioAttribute
            AudioAttributes audioAttributes = new AudioAttributes.Builder().
                    setUsage(AudioAttributes.USAGE_MEDIA).
                    setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).
                    build();

            mSp = new SoundPool.Builder().
                    setMaxStreams(5).
                    setAudioAttributes(audioAttributes).
                    build();
        }
        else {
            mSp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        //load all the sounds into the pool
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor;

        try { //the sound will be loaded into RAM
            descriptor = assetManager.openFd("hit_bat.ogg");
            bat_hit = mSp.load(descriptor, 0);

            descriptor = assetManager.openFd("hit_wall.ogg");
            wall_hit = mSp.load(descriptor, 0);

            descriptor = assetManager.openFd("miss.ogg");
            miss = mSp.load(descriptor, 0);

            descriptor = assetManager.openFd("select.ogg");
            select = mSp.load(descriptor, 0);
        }
        catch (IOException e) {
            Log.e("ERROR", "Sound Loading Failed!");
        }

        //start new game
        startGame();
    }

    private void startGame() {
        //place ball and bat back in default locations
        mBall.reset(mScreenX, mScreenY);
        mBat.reset();
    }

    @Override
    public void run() {
        while(mPlaying) {
            //get the current time for the frame
            long frameStartTime = System.currentTimeMillis();

            if (!mPaused) {
                //update the locations of the bat and ball
                update();
                //check if any collisions have occured
                detectCollisions();
            }

            //locations have been updated, so time to draw
            draw();
            //get the new system time to find out time needed to finish drawing a frame
            long frameTime = System.currentTimeMillis() - frameStartTime;
            if (frameTime > 0) {
                //calculates the FPS
                mFPS = MS_PER_S / frameTime;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //check what movement the player has performed
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: //player placed finger down onto screen
                Log.d("FINGER", "down");
                mPaused = false; //unpauses game
                if (game_over) {
                    game_over = false;
                    mScore = 0;
                    mLives = 3;
                }
                //determine what side of the screen the finger was placed down
                if (event.getX() < mScreenX / 2) { //left
                    mBat.setMovement(mBat.LEFT);
                }
                else {
                    mBat.setMovement(mBat.RIGHT);
                }
                return true; //returning true instead of calling break since further
                //motionevents such as ACTION_UP will not be processed unless
                //an ACTION_DOWN event had occured prior

            case MotionEvent.ACTION_UP: //player lifts their finger
                //bat is stopped
                Log.d("FINGER", "Up");
                mBat.setMovement(mBat.STOPPED);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void update() {
        //update the bat and ball locations
        mBall.update(mFPS);
        mBat.update(mFPS);

    }

    private void detectCollisions() {
        //ball hits bat
        if (RectF.intersects(mBall.getmRect(), mBat.getRect())) {
            mBall.batBounce(mBat.getRect());
            mBall.increaseVelocity();
            mBat.increaseVelocity();
            mScore++;
            mSp.play(bat_hit, 1, 1, 0, 0, 1);
        }

        //ball hits top wall
        if (mBall.getmRect().top <= 0) {
            mBall.reverseYVel();
            mSp.play(wall_hit, 1, 1, 0, 0, 1);
        }

        //ball hits left wall
        if (mBall.getmRect().left <= 0) {
            mBall.reverseXVel();
            mSp.play(wall_hit, 1, 1, 0, 0, 1);
        }

        //ball hits right wall
        if (mBall.getmRect().right >= mScreenX) {
            mBall.reverseXVel();
            mSp.play(wall_hit, 1, 1, 0, 0, 1);
        }

        //ball falls
        if (mBall.getmRect().bottom > mScreenY) {
            mLives--;
            mSp.play(miss, 1, 1, 0, 0, 1);
            mBall.reset(mScreenX, mScreenY);
            if (mLives == 0) {
                mPaused = true;
                game_over = true;
                mSp.play(select, 1, 1, 0, 0, 1);
                startGame();
            }
        }
    }

    //called when player quits the game
    public void pause() {
        //set playing to false and stop the thread
        mPlaying = false;
        try {
            mGameThread.join();
        }
        catch (InterruptedException e) {
            Log.e("Error: ", "Joining the thread");
        }
    }

    //called when player starts the game
    public void resume() {
        //set playing to true and create and start a thread
        mPlaying = true;
        try {
            mGameThread = new Thread(this);
            mGameThread.start();
        }
        catch(Exception e) {
            Log.e("Error", "Starting the gameThread");
        }
    }

    private void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            //locks the canvas (graphics memory) so it is ready to be drawn on
            mCanvas = mSurfaceHolder.lockCanvas();
            //fill the screen with a solid color
            mCanvas.drawColor(Color.argb(255, 38, 59, 210));
            //the color to paint with
            mPaint.setColor(Color.argb(255,255,255,255));
            //draw bat and ball
            mCanvas.drawBitmap(mBall.getBitmap(), mBall.getmRect().left, mBall.getmRect().top, mPaint);
            mCanvas.drawBitmap(mBat.getmBitmap(), mBat.getRect().left, mBat.getRect().top, mPaint);

            //set font size
            mPaint.setTextSize(mFontSize);
            //Draw the HUD
            mCanvas.drawText("Score: " + mScore + " Lives: " + mLives, mFontSize, 2*mMarginSize, mPaint);

            //game over screen
            if (game_over) {
                Log.d("PRINT", "Game_Over");
                mPaint.setColor(Color.argb(255,255,0,0));
                mPaint.setTextSize(mScreenY / 3);
                mCanvas.drawText("GameOver", mScreenX/8, mScreenY/2, mPaint);
            }

            if (debugging) {
                printDebugInfo();
            }

            //display drawing on the screen
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);    //this method displays the canvas that was drawn on
        }
    }

    private void printDebugInfo() {
        int debugSize = mFontSize/2;
        int debugStart = 150; //arbitrary vertical position
        mPaint.setTextSize(debugSize);
        mCanvas.drawText("FPS: " + mFPS, 10, debugSize+debugStart, mPaint);
    }
}
