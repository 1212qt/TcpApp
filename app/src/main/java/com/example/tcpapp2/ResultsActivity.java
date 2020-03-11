package com.example.tcpapp2;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.github.mikephil.charting.charts.LineChart;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ResultsActivity extends AppCompatActivity{
    private static final String DB_TAG = "ResultsActivity";
    private ConstraintLayout m_constrLayout;
    private ProgressBar m_bar;


    @Override
    protected void onCreate(Bundle savedStateBundle){
        super.onCreate(savedStateBundle);
        setContentView(R.layout.activity_result);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getSupportActionBar().hide();

        ChartDrawer.listeners = new Vector<>();
        m_constrLayout = findViewById(R.id.constraint_layout);
        m_bar = findViewById(R.id.progressBar);
        m_bar.setVisibility(View.VISIBLE);
        m_constrLayout.setBackgroundColor(Color.BLACK);

        setViews();
    }

    private void failure(){
        Log.i(DB_TAG, "Socket error, returning to parameters activity");
        AlertDialog.Builder l_dialog = new AlertDialog.Builder(this);
        l_dialog.setTitle("Błąd!");
        l_dialog.setMessage("Błąd ładowania danych z serwera!");
        l_dialog.setCancelable(false);
        l_dialog.setPositiveButton("ok", (DialogInterface.OnClickListener)(a, b) -> runOnUiThread(() -> finish()));
        runOnUiThread(l_dialog::show);
    }

    private void terminateLoader() {
        m_bar.setVisibility(View.INVISIBLE);
        m_constrLayout.setBackgroundColor(Color.WHITE);
    }

    private void setViews() {
        m_bar.setVisibility(View.VISIBLE);
        m_constrLayout.setBackgroundColor(Color.BLACK);
        CountDownLatch l_done = new CountDownLatch(1);
        new Thread(() -> {
            boolean l_failure = false;
            try(Socket l_socket = new Socket()){
                Looper.prepare();
                l_socket.connect(new InetSocketAddress(Connector.ip, Connector.port), 1000);
                if (!Connector.TcpReadScopeBufferDirect(l_socket, 0, 0, 0, 0, 2048)){
                    Log.i(DB_TAG, "Can't read data from server");
                    l_failure = true;
                    failure();
                }
            if (l_failure){
                Log.i(DB_TAG + " Thread", "Thread killed.");
                terminateLoader();
                Thread.currentThread().interrupt();
            }
            String l_jsonString = Configuration.getJSONConfiguration(this);
            List<Configuration.ReadableParameter> p_list = Configuration.getReadableParameters(l_jsonString);
            List<Configuration.Chart> l_chartsConfig = Configuration.getCharts(l_jsonString);

            LinearLayout l_layout = findViewById(R.id.linear_layout);
            l_layout.setVisibility(View.INVISIBLE);
            LayoutInflater l_inflater = LayoutInflater.from(this);

            for (int i = 0; i < p_list.size(); i++) {
                EditText l_editText;
                l_editText = new EditText(this);
                l_editText.setEnabled(false);
                Configuration.ReadableParameter l_parameter = p_list.get(i);
                if (l_parameter.type.equals("int")) {
                    l_editText.setText(l_parameter.name + ":" + Connector.TcpReadUnsigned(l_socket, l_parameter.address));
                }
                else {
                    l_editText.setText(l_parameter.name + ":" + Connector.TcpReadDouble(l_socket, l_parameter.address));
                }
                runOnUiThread(() -> l_layout.addView(l_editText));
                Log.i(DB_TAG, "Parameters added!");
            }
            for (int i = 0; i < l_chartsConfig.size(); i++) {
                Configuration.Chart l_configurationChart = Configuration.getCharts(l_jsonString).get(i);
                ConstraintLayout l_chartView = (ConstraintLayout) l_inflater.inflate(R.layout.chart, l_layout, false);
                LineChart l_chart = l_chartView.findViewById(R.id.chart);
                TextView l_editText = l_chartView.findViewById(R.id.textView);
                try {
                    ChartDrawer.loadChart(l_chart, Connector.getData(), l_configurationChart);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                ChartDrawer.ChartGestureListener l_listener = new ChartDrawer.ChartGestureListener(Configuration.getCharts(l_jsonString).get(i), this);
                l_editText.setText(l_configurationChart.title);
                ChartDrawer.listeners.add(l_listener);
                l_chart.setOnChartGestureListener(l_listener);
                runOnUiThread(() -> l_layout.addView(l_chartView));
            }
            terminateLoader();
            Log.i(DB_TAG, "Charts added!");
            runOnUiThread(() -> l_layout.setVisibility(View.VISIBLE));

            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

    }
}
