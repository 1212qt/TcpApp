package com.example.tcpapp2;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.github.mikephil.charting.charts.LineChart;
import java.io.IOException;

public class FullScreenChart extends AppCompatActivity {
    private final String DB_TAG = new String("FullScreenChart");
    private ConstraintLayout constrLayout;
    private ProgressBar bar;
    private TextView text;
    @Override
    public void onCreate(Bundle savedStateBundle){
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedStateBundle);
        setContentView(R.layout.activity_full_screen_chart);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        constrLayout = findViewById(R.id.constraint_layout);
        text = findViewById(R.id.textView);
        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
        constrLayout.setBackgroundColor(Color.BLACK);

        loadChart();
    }

    private void terminateLoader() {
        bar.setVisibility(View.INVISIBLE);
        constrLayout.setBackgroundColor(Color.WHITE);
    }

    private void loadChart(){
        new Thread(() -> {
            try {
                Looper.prepare();
                LayoutInflater l_inflater = LayoutInflater.from(this);
                ConstraintLayout l_chartView = (ConstraintLayout) l_inflater.inflate(R.layout.full_screen_chart, constrLayout, false);
                LineChart l_chart = l_chartView.findViewById(R.id.chart);
                ChartDrawer.loadChart(l_chart, Connector.getData(), ChartDrawer.chart);
                l_chart.setTouchEnabled(true);
                l_chart.setPinchZoom(true);
                terminateLoader();
                runOnUiThread(() -> text.setText(ChartDrawer.chart.title));
                runOnUiThread(() -> constrLayout.addView(l_chartView));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
