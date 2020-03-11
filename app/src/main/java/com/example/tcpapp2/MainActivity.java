package com.example.tcpapp2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private final String DB_TAG = "MainActivity";

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            setUp();
        }
        else{
            finish();
        }
    }

    private void checkHost(String p_ip){
        Log.i(DB_TAG, "Checking host connection...");
        Runtime l_runtime = Runtime.getRuntime();
        new Thread(() -> {
            int l_result = 0;
            try {
                Process l_process = l_runtime.exec("/system/bin/ping -c 1 " + p_ip);
                Log.i(DB_TAG, "Pinging:" + p_ip);
                l_result = l_process.waitFor();
                if(l_result==0){
                    Log.i(DB_TAG, "Host reachable!");
                    runOnUiThread(this::runParametersActivity);
                }else {
                    Log.e(DB_TAG, "Host unreachable!");
                    runOnUiThread(this::popErrorDialog);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

    }

    private void setUp(){
        EditText l_ipView = findViewById(R.id.ipEditText);
        EditText l_portView = findViewById(R.id.portEditText);
        String l_jsonConfiguration = Configuration.getJSONConfiguration(this);

        l_ipView.setText(Configuration.getIpFromJSON(l_jsonConfiguration));
        l_portView.setText(String.valueOf(Configuration.getPortFromJSON(l_jsonConfiguration)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    private void runParametersActivity(){
        Intent intent = new Intent(this, ParametersActivity.class);
        startActivity(intent);
    }

    private void popErrorDialog(){
        AlertDialog.Builder l_dialog = new AlertDialog.Builder(this);
        l_dialog.setTitle("Błąd!");
        l_dialog.setMessage("Błąd połączenia z serwerem!");
        l_dialog.setCancelable(false);
        l_dialog.setPositiveButton("ok", (DialogInterface.OnClickListener)(a, b) -> {});
        l_dialog.show();
    }

    public void onClick(View view){
        EditText l_ipView = findViewById(R.id.ipEditText);
        EditText l_portView = findViewById(R.id.portEditText);
        if(!l_ipView.getText().toString().isEmpty() &&
            !l_portView.getText().toString().isEmpty()){
                Connector.ip = l_ipView.getText().toString();
                Connector.port = Integer.parseInt(l_portView.getText().toString());
                checkHost(Connector.ip);
        }
    }
}
