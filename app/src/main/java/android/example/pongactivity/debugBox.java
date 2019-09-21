package android.example.pongactivity;

import android.graphics.RectF;

public class debugBox {
    //member vars
    private RectF mRect = new RectF();


    public debugBox(float x, float y) {
        //a small box that can be hit to turn debugging off/on
        mRect.left = 10;
        mRect.top = 10;
        mRect.right = mRect.top + x/3;
        mRect.bottom = mRect.top + y/3;
    }

    public RectF getRect() {
        return mRect;
    }
}
