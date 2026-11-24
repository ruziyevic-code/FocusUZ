package uz.focusuz.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView timerText;
    Button startButton;
    CountDownTimer timer;
    long remaining = 1500000;
    boolean running = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40,40,40,40);
        root.setBackgroundColor(Color.rgb(16,20,17));

        TextView title = new TextView(this);
        title.setText("FocusUZ");
        title.setTextSize(36);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Diqqat. Intizom. Natija.");
        subtitle.setTextColor(Color.LTGRAY);
        root.addView(subtitle);

        timerText = new TextView(this);
        timerText.setTextSize(48);
        timerText.setTextColor(Color.parseColor("#81E8A4"));
        timerText.setGravity(Gravity.CENTER);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setStroke(4, Color.parseColor("#81E8A4"));
        timerText.setBackground(circle);

        int size = (int)(220 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(size,size);
        params.setMargins(0,60,0,60);
        root.addView(timerText,params);
        updateTimer();

        startButton = new Button(this);
        startButton.setText("Fokusni boshlash");
        startButton.setAllCaps(false);
        startButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
            Color.parseColor("#81E8A4")));
        root.addView(startButton);

        startButton.setOnClickListener(v -> {
            if(running) {
                timer.cancel();
                running = false;
                startButton.setText("Davom ettirish");
            } else {
                startTimer();
            }
        });

        Button reset = new Button(this);
        reset.setText("Qayta boshlash");
        reset.setAllCaps(false);
        root.addView(reset);

        reset.setOnClickListener(v -> {
            if(timer != null) timer.cancel();
            running = false;
            remaining = 1500000;
            updateTimer();
            startButton.setText("Fokusni boshlash");
        });

        setContentView(root);
    }

    void startTimer() {
        running = true;
        startButton.setText("Pauza");

        timer = new CountDownTimer(remaining,1000) {
            public void onTick(long ms) {
                remaining = ms;
                updateTimer();
            }

            public void onFinish() {
                running = false;
                remaining = 0;
                updateTimer();
                Toast.makeText(MainActivity.this,
                    "Fokus yakunlandi!",
                    Toast.LENGTH_LONG).show();
                remaining = 1500000;
                startButton.setText("Yangi fokus");
            }
        }.start();
    }

    void updateTimer() {
        long sec = remaining / 1000;
        timerText.setText(String.format(
            Locale.US,"%02d:%02d",sec/60,sec%60));
    }

    @Override
    protected void onDestroy() {
        if(timer != null) timer.cancel();
        super.onDestroy();
    }
}
