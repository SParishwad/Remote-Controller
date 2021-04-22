package com.erz.joystick;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.erz.joysticklibrary.JoyStick;

public class MainActivity extends AppCompatActivity implements JoyStick.JoyStickListener {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameView = (GameView) findViewById(R.id.game);
        JoyStick joy1 = (JoyStick) findViewById(R.id.leftJoystick);
        joy1.setListener(this);
        joy1.enableStayPut(true);
        joy1.setPadBackground(R.drawable.pad);
        joy1.setButtonDrawable(R.drawable.button);

        JoyStick joy2 = (JoyStick) findViewById(R.id.rightJoystick);
        joy2.setListener(this);
        joy2.setPadColor(Color.parseColor("#55ffffff"));
        joy2.setButtonColor(Color.parseColor("#55ff0000"));

        GameView.SetItems(getApplicationContext());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMove(JoyStick joyStick, float xPercent, float yPercent, double angle) {
        switch (joyStick.getId()) {
            case R.id.leftJoystick:
                gameView.move("Left Joystick", xPercent, yPercent);
                break;
            case R.id.rightJoystick:
                gameView.move("Right Joystick", xPercent, yPercent);
                gameView.rotate(angle);
                break;
        }
    }

    @Override
    public void onTap() {}

    @Override
    public void onDoubleTap() {}

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
    }
}