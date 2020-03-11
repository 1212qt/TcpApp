package com.example.tcpapp2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ParametersActivity extends AppCompatActivity {
    private final String DB_TAG = "ParametersActivity";
    private final int TIMEOUT = 1000;
    private List<TextInputLayout> m_textEditList = new Vector<>();
    private List<Configuration.WritableParameter> m_writableParameterList = new Vector<>();
    private boolean m_connected = false;

    private void setViews(List<Configuration.WritableParameter> p_list) {
        LinearLayout l_layout = findViewById(R.id.linearLayout);
        LayoutInflater l_inflater = LayoutInflater.from(this);

        for (int i = 0; i < p_list.size(); i++) {
            Configuration.WritableParameter l_item = p_list.get(i);
            ConstraintLayout l_rowView = (ConstraintLayout) l_inflater.inflate(R.layout.row, l_layout, false);
            TextInputLayout l_input = l_rowView.findViewById(R.id.input);
            l_input.setHint(l_item.name + ": " + l_item.type  + "   zakres:[" + l_item.min + "," + l_item.max + "]");
            l_layout.addView(l_rowView);
            m_textEditList.add(l_input);
        }

    }

    public void onLoadClick() {
        CountDownLatch l_done = new CountDownLatch(1);
        for (int i = 0; i < m_writableParameterList.size(); i++) {
            Configuration.WritableParameter l_parameter = m_writableParameterList.get(i);
            int l_address = l_parameter.address;
            String l_readString = m_textEditList.get(i).getEditText().getText().toString();
            if (l_parameter.type.equals("int")) {
                int l_value;
                if (l_readString.isEmpty()) {
                    l_value = 0;
                }
                else {
                    l_value = (int)Double.parseDouble(l_readString);
                    if(!(l_value >= l_parameter.min && l_value <= l_parameter.max)){
                        Log.e(DB_TAG, "Wrong values provided!");
                        AlertDialog.Builder l_dialog = new AlertDialog.Builder(this);
                        l_dialog.setTitle("Błąd!");
                        l_dialog.setMessage("Podano złe wartości!");
                        l_dialog.setCancelable(false);
                        l_dialog.setPositiveButton("ok", (DialogInterface.OnClickListener)(a,b) -> {});
                        l_dialog.show();
                        return;
                    }
                }
                new Thread(() -> {
                    Looper.prepare();
                    try(Socket l_socket = new Socket()){
                        l_socket.connect(new InetSocketAddress(Connector.ip, Connector.port), TIMEOUT);
                        if (l_socket.isConnected()){
                            l_done.countDown();
                        }
                        Connector.TcpWriteUnsigned(l_socket, l_address, l_value);
                    } catch (SocketTimeoutException e) {
                        Log.i(DB_TAG, "Socket timeout");
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            else {
                double l_value;
                if (l_readString.isEmpty()) {
                    l_value = 0d;
                }
                else {
                    l_value = Double.parseDouble(l_readString);
                    if(!(l_value >= l_parameter.min && l_value <= l_parameter.max)){
                        Log.e(DB_TAG, "Wrong values provided!");
                        AlertDialog.Builder l_dialog = new AlertDialog.Builder(this);
                        l_dialog.setTitle("Błąd!");
                        l_dialog.setMessage("Podano złe wartości!");
                        l_dialog.setCancelable(false);
                        l_dialog.setPositiveButton("ok", (DialogInterface.OnClickListener)(a,b) -> {});
                        l_dialog.show();
                        return;
                    }
                }
                new Thread(() -> {
                    try(Socket l_socket = new Socket()){
                        l_socket.connect(new InetSocketAddress(Connector.ip, Connector.port), TIMEOUT);
                        if (l_socket.isConnected()){
                            l_done.countDown();
                        }
                        Connector.TcpWriteDouble(l_socket, l_address, l_value);
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                        Log.i(DB_TAG, "Socket timeout");
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        try {
            m_connected = l_done.await(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!m_connected){
            Log.e(DB_TAG, "Socket error, returning to main activity");
            AlertDialog.Builder l_dialog = new AlertDialog.Builder(this);
            l_dialog.setTitle("Błąd!");
            l_dialog.setMessage("Błąd połączenia z serwerem!");
            l_dialog.setCancelable(false);
            l_dialog.setPositiveButton("ok", (DialogInterface.OnClickListener)(a,b) -> runOnUiThread(() -> finish()));
            l_dialog.show();
        }
        else{
            Log.i(DB_TAG, "Data sent succesfully");
            AlertDialog.Builder l_dialog = new AlertDialog.Builder(this);
            l_dialog.setTitle("Sukces!");
            l_dialog.setMessage("Dane pomyślnie wysłane!");
            l_dialog.setCancelable(false);
            l_dialog.setPositiveButton("ok", (DialogInterface.OnClickListener)(a,b) -> {});
            l_dialog.show();
        }
    }

    public void runOtherActivity(){
        Intent l_intent = new Intent(this, ResultsActivity.class);
        startActivity(l_intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);

        String l_JSONString = Configuration.getJSONConfiguration(this);
        m_writableParameterList = Configuration.getWritableParameters(l_JSONString);
        setViews(m_writableParameterList);
        findViewById(R.id.button).setOnClickListener(v -> onLoadClick());
        findViewById(R.id.button3).setOnClickListener(v -> runOtherActivity());
    }
}
