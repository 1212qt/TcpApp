package com.example.tcpapp2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class ChartDrawer {
    private static final String DB_TAG = new String("ChartDrawer");
    private static int MAX_NO_OF_SAMPLES_PER_CHART = 1000;
    public static Configuration.Chart chart;

    public static class ChartGestureListener implements OnChartGestureListener {
        private final Configuration.Chart m_Chart;
        private final Context m_Context;

        public ChartGestureListener(Configuration.Chart p_chart, Context p_context) {
            m_Chart = p_chart;
            m_Context = p_context;
        }

        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {

        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {
            Log.i(DB_TAG, "Chart double clicked");
            chart = m_Chart;
            Intent l_intent = new Intent(m_Context, FullScreenChart.class);

            m_Context.startActivity(l_intent);
        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {

        }
    }

    public static List<ChartGestureListener> listeners;

    private static int getRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);

        return Color.rgb(r, g, b);
    }

    private static List<Entry> getEntrys(List<List<Double>> p_list, int p_noOfSamples) {
        Log.i(DB_TAG, "getting entrys");
        List<Entry> l_list = new Vector<>();
        List<Double> l_dblList = p_list.get(0);
        double l_offset = p_list.get(1).get(0);
        int l_offsetBetweenNextPoint = l_dblList.size() / p_noOfSamples;
        for (int j = 0; j < p_noOfSamples; j++) {
            l_list.add(new Entry((float) (p_list.get(1).get(j * l_offsetBetweenNextPoint) - l_offset) * 1000, l_dblList.get(j * l_offsetBetweenNextPoint).floatValue()));
        }
        return l_list;
    }

    private static List<List<Double>> extractWantedRow(List<List<Double>> p_list, int p_row) {
        Log.i(DB_TAG, "extracting wanted row");
        List<List<Double>> l_returnList = new Vector<>();
        l_returnList.add(new Vector<>());
        l_returnList.add(new Vector<>());
        for (int i = 0; i < p_list.get(0).size(); i++) {
            l_returnList.get(0).add(p_list.get(p_row).get(i));
            l_returnList.get(1).add(p_list.get(63).get(i));
        }
        return l_returnList;
    }

    private static LineData getLineDataSet(List<List<Double>> p_data, Configuration.Chart p_list) throws IOException, InterruptedException {
        Log.i(DB_TAG, "getting LineDataSet");
        LineData l_lineData = new LineData();
        Log.i(DB_TAG, "data fetched");
        int l_noOfDataSets = p_list.data.size();
        for (int i = 0; i < l_noOfDataSets; i++) {
            int l_color = getRandomColor();
            List<Entry> l_entrys = getEntrys(extractWantedRow(p_data, p_list.data.get(i).first), MAX_NO_OF_SAMPLES_PER_CHART/l_noOfDataSets);
            LineDataSet l_set = new LineDataSet(l_entrys, p_list.data.get(i).second);
            l_set.setCircleColor(l_color);
            l_set.setColor(l_color);
            l_set.setCircleRadius(1.5f);
            l_set.setDrawCircleHole(false);

            l_lineData.addDataSet(l_set);
        }
        return l_lineData;
    }

    public static void loadChart(LineChart p_chart, List<List<Double>> p_data, Configuration.Chart p_list) throws IOException, InterruptedException {
        LineData l_data = getLineDataSet(p_data, p_list);

        p_chart.getDescription().setEnabled(false);
        YAxis yAxis = p_chart.getAxisLeft();
        yAxis.setTextSize(15f);
        yAxis.setDrawAxisLine(true); // no axis line
        yAxis.setDrawGridLines(true); // no grid lines
        yAxis.setDrawZeroLine(true); // draw a zero line
        p_chart.getAxisRight().setEnabled(false); // no right axis

        XAxis xAxis = p_chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(15f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        p_chart.setData(l_data);
        p_chart.setScaleEnabled(false);
        p_chart.invalidate();
    }
}
