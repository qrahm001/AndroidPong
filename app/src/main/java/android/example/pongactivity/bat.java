package android.example.pongactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

class bat {
    private RectF mRect;
    private float mLength; //length of the bat
    private float height;
    private float xCoord; //x coordinate of the bat
    private float yCoord;
    private float mBatSpeed;
    private int mSxreenX;

    //used by pngGame to determine the current movement of the bat
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;
    private int mBatMoving = STOPPED;

    //the bat's graphic
    Bitmap mBitmap;


    public bat(Context context, int x, int y) {
        mSxreenX = x;

        //configure size of the bat
        mLength = mSxreenX / 8;
        height = y / 40;
        //configure default location of bat
        xCoord = mSxreenX / 2;
        yCoord = y - height;

        //initialize the RectF vals
        mRect = new RectF(xCoord, yCoord,xCoord+mLength, yCoord+height);

        //set speed of the bat
        mBatSpeed = mSxreenX; //bat can move across the screen in a second

        //set the graphic for the bat
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bat);
        //scale the bitmap
        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)mLength, (int)height, true);
    }

    public void setMovement(int x) {
        mBatMoving = x;
    }

    public RectF getRect() {
        return mRect;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void increaseVelocity() {
        mBatSpeed *= 1.05;
    }

    public void reset() {
        mBatSpeed = mSxreenX;
    }

    void update(long fps) {
        if (mBatMoving != STOPPED) {
            //determine which direction bat should move
            if (mBatMoving == LEFT) {
                xCoord = xCoord - (mBatSpeed / fps);
            } else if (mBatMoving == RIGHT) {
                xCoord = xCoord + (mBatSpeed / fps);
            }

            //check if bat is going offscreen
            if (xCoord < 0) {
                xCoord = 0;
            } else if (xCoord + mLength > mSxreenX) {
                xCoord = mSxreenX - mLength;
            }

            //update mRect
            mRect.left = xCoord;
            mRect.right = xCoord + mLength;
        }
    }
}
