package android.example.pongactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

class ball {
    //member vars
    private RectF mRect; //holds the coords of the ball
    private float xVel; //the speed the ball will travel through the x-axis
    private float yVel;
    private float mWidth;
    private float mHeight;
    private int mScreenX;

    //graphic
    Bitmap mBitmap;

    public ball(Context context, int screenX) {
        //init the ball to 1% of the screen width
        mScreenX = screenX;
        mWidth = screenX/100;
        mHeight = screenX/100;

        //init the rectF
        mRect = new RectF();

        //set the bitmap
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ball);
        //scale the bitmap
        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)mWidth, (int)mHeight, true);
    }

    public RectF getmRect() { //used to get the rectF values in the pongGame class
        return mRect;
    }

    void update(long fps) {
        //change the coords of the ball based on the FPS and velocity
        //first update the top left corner of the ball
        mRect.left = mRect.left + (xVel/fps);
        mRect.top = mRect.top + (yVel/fps);
        //update remaining corners based on size of the ball
        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top + mHeight;
        //fixes coords if ball goes past walls
        if (mRect.left < 0) {
            mRect.left = 0;
            mRect.right = mRect.left + mWidth;
        }
        if (mRect.top < 0) {
            mRect.top = 0;
            mRect.bottom = mRect.top + mHeight;
        }
        if (mRect.right > mScreenX) {
            mRect.right = mScreenX;
            mRect.left = mScreenX - mWidth;
        }
    }

    void reverseXVel() {
        xVel = -xVel;
    }

    void reverseYVel() {
        yVel = -yVel;
    }

    void increaseVelocity() { //speed increases by 20% after each succesful hit
        xVel = xVel * 1.20f;
        yVel = yVel * 1.20f;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    void batBounce(RectF batRect) {
        //center of bat
        float batCenter = (batRect.left + batRect.right)/2;
        //center of ball
        float ballCenter = (mRect.left + mRect.right)/2;

        //check where the ball hit the bat and change the velocity based on that
        if ((ballCenter - batCenter) < 0) {
            //should bounce off to the left
            xVel = -Math.abs(xVel);
        }
        else {
            //bounce to the right
            xVel = Math.abs(xVel);
        }
        reverseYVel();
        //yVel = Math.abs(yVel); // the ball will now be traveling up
    }

    //used to reposition the ball
    void reset(int x, int y) {
        mRect.left = x / 2;
        mRect.top = 0;
        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top + mHeight;

        //set the initial velocities
        xVel = (y/3);
        yVel = -(y/3);
    }
}
