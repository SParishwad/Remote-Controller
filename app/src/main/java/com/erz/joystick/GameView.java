package com.erz.joystick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Vector;

/**
 * Created by edgarramirez on 6/15/15.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static Context mContext;
    private int i;
    private int size = 20;
    private int minSpeed;
    private int maxSpeed;
    private int minRadius;
    private int maxRadius;
    private float width;
    private float height;
    private float posX;
    private float posY;
    private float radius;
    private double angle;
    private double power;
    private double angle2;
    private String joyStickID;
    private float xPercent;
    private float yPercent;

    private int oldLeftPercentX = 0, newLeftPercentX = 0, oldLeftPercentY = 0, newLeftPercentY = 90;
    private int oldRightPercentX = 0, newRightPercentX = 0, oldRightPercentY = 0, newRightPercentY = 0;

    private Bitmap droid;
    private Bitmap plane1;
    private Bitmap plane2;
    private GameLoop gameLoop;
    private Paint paint;
    private Vector<Star> stars = new Vector<>();
    private RectF rectF = new RectF();
    private Random random = new Random();

    Handler handler = new Handler();
    WebSocket ws = null;

    public static void SetItems(Context context) {
        mContext = context;
    }

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        droid = BitmapFactory.decodeResource(getResources(), R.drawable.droid);
        plane1 = BitmapFactory.decodeResource(getResources(), R.drawable.plane1);
        plane2 = BitmapFactory.decodeResource(getResources(), R.drawable.plane2);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;
        canvas.drawColor(Color.BLACK);

        if (stars != null && stars.size() > 0) {
            for (i = 0; i < size; i++) {
                stars.get(i).draw(canvas, paint, rectF, random, minSpeed, maxSpeed, minRadius, maxRadius, width, height, maxRadius);
            }
        }

        posX -= Math.cos(angle) * power;
        posY += Math.sin(-angle) * power;
        if (posX > width - radius) posX = width - radius;
        if (posX < radius) posX = radius;
        if (posY > height - radius) posY = height - radius;
        if (posY < radius) posY = radius;

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(30);
        canvas.drawText(String.format("%.2f, %.2f", (newLeftPercentX * 0.3), (newLeftPercentY * 0.9)), 10, 25, paint);
        canvas.drawText(String.format("%.2f, %.2f", (newRightPercentX * 0.3), (newRightPercentY * 0.9)), 1980, 25, paint);
        //canvas.drawText(String.format("%.2f", radius), posX-radius, posY-radius, paint);

        float rotate;
        if (angle2 == 0) rotate = 0;
        else rotate = (int) angle2;
        //else rotate = (float) Math.toDegrees(angle2) - 90;
        canvas.rotate(rotate, posX, posY);

        //rectF.set(posX - radius * 4, posY - radius, posX + radius * 4, posY + radius);
        rectF.set(posX-(newLeftPercentY+210)*2, posY-(newLeftPercentY+210)/2,               // For zooming the bitmap image
                posX+(newLeftPercentY+210)*2, posY+(newLeftPercentY+210)/2);
        canvas.drawBitmap(plane2, null, rectF, paint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameLoop = new GameLoop(this);
        gameLoop.setRunning(true);
        gameLoop.start();
        // Create a WebSocket Factory set 5000 milliseconds as a timeout
        // Value for socket connection.
        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
        // Create a WebSocket. The timeout value set above is used.
        try {
            ws = factory.createSocket("ws://192.168.4.1:81/ws/");

            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    Log.d("TAG", "onTextMessage: " + message);
                }
            });

            ws.connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startRepeatingTask();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
        float min = Math.min(width, height);

        float centerX = width / 2;
        float centerY = height / 2;
        posX = centerX;
        posY = centerY;
        radius = min / 12;
        rectF = new RectF(posX - radius, posY - radius, posX + radius, posY + radius);

        minSpeed = (int) (min / 37);
        maxSpeed = (int) (min / 12);
        minRadius = (int) (min / 250);
        maxRadius = (int) (min / 220);

        if (maxRadius == minRadius) maxRadius += minRadius;

        stars.clear();
        for (i = 0; i < size; i++) {
            stars.add(new Star(random, minSpeed, maxSpeed, minRadius, maxRadius, width, height));
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        gameLoop.setRunning(false);
        gameLoop = null;
        stopRepeatingTask();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void move(String joyStick, float percentX, float percentY) {
        // There is a better way to handle a multi-touch events. When I figure it out, I will delete
        // the switch condition and uncomment the code below.
        // The way to display and vibrate from this loop.

        switch (joyStick) {
            case "Left Joystick":
                int leftXPercent = (int) (percentX * 100);
                int leftYPercent = (int) (percentY * 100);
                if (Math.abs(leftXPercent - oldLeftPercentX) >= 1) {                                                     // 0.01
                    newLeftPercentX = leftXPercent;
                }
                if (Math.abs(leftYPercent - oldLeftPercentY) >= 1) {                                                    // 0.01
                    newLeftPercentY = leftYPercent;
                }
                if (newLeftPercentX != oldLeftPercentX || newLeftPercentY != oldLeftPercentY) {
                    DecimalFormat df = new DecimalFormat("##.#");
                    Log.d("Left Joystick", "X percent: "
                            + df.format(newLeftPercentX * 0.3)                              // Rudder Sweep = 85 +- 30          // *100
                            + " Y percent: " + df.format(newLeftPercentY * 0.19));         // BLDC                             // *100
                    /*if (ws.isOpen()) {
                        ws.sendText("L" + ";" + df.format(newLeftPercentX * 0.3) + ";"          // Rudder Sweep = 85 +- 30          // *100
                                + df.format(newLeftPercentY * 0.9));                            // BLDC                             // *100
                    }*/
                    oldLeftPercentX = newLeftPercentX;
                    oldLeftPercentY = newLeftPercentY;
                    /** This if loop is used for performing vibrations */
                    if ((Math.abs(newLeftPercentX) >= 98) || (Math.abs(newLeftPercentY) >= 98)) {                       // *100 twice
                        long[] mVibratePattern = new long[]{500, 500, 200, 200, 200, 200, 200};
                        int[] mAmplitudes = new int[]{255, 255, 255, 0, 255, 0, 255};
                        ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE))
                                .vibrate(VibrationEffect.createWaveform(mVibratePattern, mAmplitudes, -1));
                    } else {
                        ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE))
                                .vibrate(VibrationEffect.createOneShot(8, 50));
                    }
                }
                break;
            case "Right Joystick":
                int rightXPercent = (int) (percentX * 100);
                int rightYPercent = (int) (percentY * 100);
                if (Math.abs(rightXPercent - oldRightPercentX) >= 1) {                                                                  // 0.01
                    newRightPercentX = rightXPercent;
                }
                if (Math.abs(rightYPercent - oldRightPercentY) >= 1) {                                                                  // 0.01
                    newRightPercentY = rightYPercent;
                }
                if (newRightPercentX != oldRightPercentX || newRightPercentY != oldRightPercentY) {
                    DecimalFormat df = new DecimalFormat("##.#");
                    Log.d("Right Joystick", "X percent: "
                            + df.format(newRightPercentX * 0.35)                            // Aileron Sweep = 80/100 +- 35     // *100
                            + " Y percent: " + df.format(newRightPercentY * 0.19));         // Elevator Sweep = 84 +- 19        // *100
                    /*if (ws.isOpen()) {
                        ws.sendText("R" + ";" + df.format(newRightPercentX * 0.35) + ";"        // Aileron Sweep = 80/100 +- 35     // *100
                                + df.format(newRightPercentY * 0.19));                          // Elevator Sweep = 84 +- 19        // *100
                    }*/
                    oldRightPercentX = newRightPercentX;
                    oldRightPercentY = newRightPercentY;
                    /** This if loop is used for performing vibrations */
                    if ((Math.abs(newRightPercentX) >= 98) || (Math.abs(newRightPercentY) >= 98)) {                                     // *100 twice
                        long[] mVibratePattern = new long[]{500, 500, 200, 200, 200, 200, 200};
                        int[] mAmplitudes = new int[]{255, 255, 255, 0, 255, 0, 255};
                        ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE))
                                .vibrate(VibrationEffect.createWaveform(mVibratePattern, mAmplitudes, -1));
                    } else {
                        ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE))
                                .vibrate(VibrationEffect.createOneShot(8, 50));
                    }
                }
                break;
        }

        /**
         * Uncomment this block of code after the multi-touch event issue is resolved
         */
/*        if (Math.abs(xPercent - oldPercentX) >= 0.01) {
            newPercentX = xPercent;
        }
        if (Math.abs(yPercent - oldPercentY) >= 0.01) {
            newPercentY = yPercent;
        }
        if (newPercentX != oldPercentX || newPercentY != oldPercentY) {
            //DecimalFormat df = new DecimalFormat("##.#");
            Log.d(String.valueOf(joyStick), "X percent: " + newPercentX
                    + " Y percent: " + newPercentY);
            oldPercentX = newPercentX;
            oldPercentY = newPercentY;
            if(ws.isOpen()){
                ws.sendText(df.format(newPercentX * 100));
                ws.sendText(df.format(newPercentY * 100));
            }
            // This if loop is used for performing vibrations
            if((Math.abs(newPercentX * 100) >= 98) || (Math.abs(newPercentY * 100) >= 98)) {
                long[] mVibratePattern = new long[]{500, 500, 200, 200, 200, 200, 200};
                int[] mAmplitudes = new int[]{255, 255, 255, 0, 255, 0, 255};
                ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE))
                        .vibrate(VibrationEffect.createWaveform(mVibratePattern, mAmplitudes, -1));
            }
            else {
                ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE))
                        .vibrate(VibrationEffect.createOneShot(8, 50));
            }
        }*/
    }

    Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (ws.isOpen()) {
                    DecimalFormat df = new DecimalFormat("##.#");
                    ws.sendText("L" + ";" + df.format(newLeftPercentX * 0.3) + ";"          // Rudder Sweep = 85 +- 30          // *100
                            + df.format(newLeftPercentY * 0.9));                            // BLDC                             // *100
                    ws.sendText("R" + ";" + df.format(newRightPercentX * 0.35) + ";"        // Aileron Sweep = 80/100 +- 35     // *100
                            + df.format(newRightPercentY * 0.19));                          // Elevator Sweep = 84 +- 19        // *100
                    /*if (newLeftPercentX != oldLeftPercentX || newLeftPercentY != oldLeftPercentY){
                        Log.d("Left Joystick", "X percent: "
                                + df.format(newLeftPercentX * 0.3)                              // Rudder Sweep = 85 +- 30          // *100
                                + " Y percent: " + df.format(newLeftPercentY * 0.19));         // BLDC                             // *100
                    }
                    if (newRightPercentX != oldRightPercentX || newRightPercentY != oldRightPercentY){
                        Log.d("Right Joystick", "X percent: "
                                + df.format(newRightPercentX * 0.35)                            // Aileron Sweep = 80/100 +- 35     // *100
                                + " Y percent: " + df.format(newRightPercentY * 0.19));         // Elevator Sweep = 84 +- 19        // *100
                    }*/
                }
            } finally {
                handler.postDelayed(statusChecker, 150);
            }
        }
    };

    void startRepeatingTask() {
        statusChecker.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(statusChecker);
    }

    /*public void move(double angle, double power) {
        this.angle = angle;
        this.power = power;
    }*/

    public void rotate(double angle2) {
        this.angle2 = angle2;
    }

    public static byte[] floatToByteArray(float value) {
        int intBits = Float.floatToIntBits(value);
        return new byte[]{
                (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits)};
    }
}