package vn.edu.luhuynhkhanhhung.savethebunny;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;
import android.os.Handler;
import java.util.logging.LogRecord;

public class GameView extends View {
    Bitmap background, ground, rabbit;
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    boolean reachedPoints = false;
    final long UPDATE_MILLIS = 30;
    float TEXT_SIZE = 120;
    int points = 0;
    int life = 3;
    static int dWith, dHeight;
    Random random;
    float rabbitX, rabbitY;
    float oldX;
    float oldRabbitX;
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;

    public GameView(Context context) {
        super(context);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        rabbit = BitmapFactory.decodeResource(getResources(), R.drawable.rabbit);
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWith = size.x;
        dHeight = size.y;
        rectBackground = new Rect(0, 0, dWith, dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWith, dHeight);
        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();

            }
        };
        textPaint.setColor(Color.rgb(255,165,0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));
        healthPaint.setColor(Color.GREEN);
        random = new Random();
        rabbitX = dWith / 2 - rabbit.getWidth() / 2;
        rabbitY = dHeight - ground.getHeight() - rabbit.getHeight();
        spikes = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i =0; i<4; i++){
            Spike spike = new Spike(context);
            spikes.add(spike);
        }

    }
    // Tăng tốc độ rơi của spikes
    public void spikeSpeed() {
        if (points >= 100 && !reachedPoints) {

            for (Spike spike : spikes) {
                spike.resetPositon(); // Tăng tốc độ rơi của spikes
            }
            reachedPoints = true;
        }
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(rabbit, rabbitX, rabbitY ,null);
        invalidate();
        for (int i=0; i<spikes.size(); i++){
            canvas.drawBitmap(spikes.get(i).getSpike(spikes.get(i).spikeFrame), spikes.get(i).spikeX, spikes.get(i).spikeY, null);
            spikes.get(i).spikeFrame++;
            if (spikes.get(i).spikeFrame > 2){
                spikes.get(i).spikeFrame = 0;
            }
            spikes.get(i).spikeY += spikes.get(i).spikeVelocity;
            if (spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= dHeight - ground.getHeight()){
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = spikes.get(i).spikeX;
                explosion.explosionY = spikes.get(i).spikeY;
                explosions.add(explosion);
                spikeSpeed();   //Xử lý tốc độ theo điểm
            }
        }
//Xử lý va chạm
        for (int i =0; i < spikes.size(); i++){
            spikes.get(i).spikeY += spikes.get(i).spikeVelocity;
            // Kiểm tra va chạm với con thỏ
            if (spikes.get(i).spikeX + spikes.get(i).getSpikeWith() >= rabbitX
                    && spikes.get(i).spikeX <= rabbitX + rabbit.getWidth()
                    && spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= rabbitY
                    && spikes.get(i).spikeY <= rabbitY + rabbit.getHeight()){
                    points += 10; // Cộng điểm khi spike chạm thỏ
                    spikes.get(i).resetPositon();
            }
            // Kiểm tra và xử lý trường hợp spike chạm đất
            else if (spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= dHeight - ground.getHeight()){
                // Trừ mạng sống khi spikes chạm đất
                life--;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = spikes.get(i).spikeX;
                explosion.explosionY = spikes.get(i).spikeY;
                explosions.add(explosion);
                spikes.get(i).resetPositon();

                if (life == 0){
                    // Xử lý khi hết mạng
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
            // Vẽ spike
            canvas.drawBitmap(spikes.get(i).getSpike(spikes.get(i).spikeFrame), spikes.get(i).spikeX, spikes.get(i).spikeY, null);
            spikes.get(i).spikeFrame++;
            if (spikes.get(i).spikeFrame > 2){
                spikes.get(i).spikeFrame = 0;
            }
        }

        // Vẽ explode
        for (int i = 0; i <explosions.size(); i++){
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).explosionX, explosions.get(i).explosionY, null);
            explosions.get(i).explosionFrame++;
            if (explosions.get(i).explosionFrame > 3){
                explosions.remove(i);
            }
        }
        // vẽ thanh máu
        if  (life == 2){
            healthPaint.setColor(Color.YELLOW);
        } else if (life == 1) {
            healthPaint.setColor(Color.RED);
        }
        canvas.drawRect(dWith-200, 30, dWith-200+60*life, 80, healthPaint);
        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        if (touchY >= rabbitY){
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN){
                oldX = event.getX();
                oldRabbitX = rabbitX;
            }
            if (action == MotionEvent.ACTION_MOVE){
                float shift = oldX - touchX;
                float newRabbitX = oldRabbitX - shift;
                if (newRabbitX <= 0)
                    rabbitX = 0;
                else if (newRabbitX >= dWith - rabbit.getWidth())
                    rabbitX = dWith -rabbit.getWidth();
                else
                    rabbitX = newRabbitX;
            }
        }

        return true;
    }

}
