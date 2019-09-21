package android.example.pongactivity;

import android.app.Activity;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

public class pongActivity extends Activity {
    //class member vars
    private pongGame mPongGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //use to get device screen resolution
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        //initialize the game engine and set the game as the view
        mPongGame = new pongGame(this, size.x, size.y);
        setContentView(mPongGame);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //call the game engine's resume method
        mPongGame.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //call the game engine's pause method
        mPongGame.pause();
    }
}
