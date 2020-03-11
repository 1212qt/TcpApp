package com.example.tcpapp2;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class Configuration {
    private static final String DB_TAG = new String("Configuration");
    public static String getJSONConfiguration(Context p_context){
        File m_sdcard = new File("/storage/self/primary");
        File m_setup = new File(m_sdcard, "setup.json");
        String l_jsonString = null;
//        try(InputStream inputStream = p_context.getAssets().open("setup.json")) {
        try(InputStream inputStream = new FileInputStream(m_setup)) {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            l_jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return l_jsonString;
    }

    public static String getIpFromJSON(String p_JSONString) {
        JSONObject l_jsonObject = null;
        String l_ip = new String();

        try {
            l_jsonObject = new JSONObject(p_JSONString);
            l_ip = l_jsonObject.getString("Ip");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return l_ip;
    }

    public static int getPortFromJSON(String p_JSONString) {
        JSONObject l_jsonObject = null;
        int l_port = 0;

        try {
            l_jsonObject = new JSONObject(p_JSONString);
            l_port = l_jsonObject.getInt("Port");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return l_port;
    }

    public static List<Configuration.WritableParameter> getWritableParameters (String p_JSONString) {
        JSONObject jsonObject = null;
        List<Configuration.WritableParameter> l_list = new Vector();
        try {
            jsonObject = new JSONObject(p_JSONString);

            JSONArray writables = jsonObject.getJSONArray("WritableParameters");

            for (int i = 0; i < writables.length(); i++) {
                JSONObject l_obj = writables.getJSONObject(i);
                l_list.add(new Configuration.WritableParameter());
                l_list.get(i).name = l_obj.getString("name");
                l_list.get(i).type = l_obj.getString("type");
                l_list.get(i).min = l_obj.getInt("min");
                l_list.get(i).max = l_obj.getInt("max");
                l_list.get(i).address = Integer.decode(l_obj.getString("address"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return l_list;
    }


    public static List<Configuration.ReadableParameter> getReadableParameters (String p_JSONString) {
        JSONObject jsonObject = null;
        List<Configuration.ReadableParameter> l_list = new Vector();
        try {
            jsonObject = new JSONObject(p_JSONString);

            JSONArray writables = jsonObject.getJSONArray("ReadableParameters");

            for (int i = 0; i < writables.length(); i++) {
                JSONObject l_obj = writables.getJSONObject(i);
                l_list.add(new Configuration.ReadableParameter());
                l_list.get(i).name = l_obj.getString("name");
                l_list.get(i).type = l_obj.getString("type");
                l_list.get(i).address = Integer.decode(l_obj.getString("address"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return l_list;
    }

    public static List<Configuration.Chart> getCharts(String p_JSONString){
        JSONObject jsonObject = null;
        List<Configuration.Chart> l_list = new Vector();
        try {
            jsonObject = new JSONObject(p_JSONString);

            JSONArray l_charts = jsonObject.getJSONArray("Charts");

            for (int i = 0; i < l_charts.length(); i++) {
                JSONObject l_obj = l_charts.getJSONObject(i);
                Configuration.Chart l_chart = new Configuration.Chart();
                l_list.add(l_chart);
                l_chart.title = l_obj.getString("title");

                l_chart.data = new Vector<>();
                JSONArray l_data = l_obj.getJSONArray("data");
                for (int j = 0; j < l_data.length(); j++) {
                    JSONObject l_object = l_data.getJSONObject(j);
                    l_chart.data.add(new Pair<>(l_object.getInt("row"), l_object.getString("label")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return l_list;
    }

    static class ReadableParameter{
        String name;
        String type;
        int address;
    }

    static class WritableParameter{
        String name;
        String type;
        int min;
        int max;
        int address;
    }

    static class Chart{
        String title;
        List<Pair<Integer, String>> data;
    }
}
