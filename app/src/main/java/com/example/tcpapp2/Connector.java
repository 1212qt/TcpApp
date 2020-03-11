package com.example.tcpapp2;

import android.util.Log;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class Connector {
    private final static String DB_TAG = "Connector";
    private static List<List<Double>> data;
    public static String ip;
    public static int port;

    public static List<List<Double>> getData(){
        return data;
    }

    public static void TcpWriteUnsigned(Socket p_socket, int p_offset, int p_value) throws IOException {
        DataInputStream l_in = new DataInputStream(p_socket.getInputStream());
        PrintWriter l_out = new PrintWriter(p_socket.getOutputStream());

        if (l_in.available() > 0){
            l_in.skipBytes(l_in.available());
        }

        l_out.write(String.format("!uWrite %X %d", p_offset, p_value));
        l_out.flush();
    }

    public static void TcpWriteDouble(Socket p_socket, int p_offset, double p_value) throws IOException {
        DataInputStream l_in = new DataInputStream(p_socket.getInputStream());
        PrintWriter l_out = new PrintWriter(p_socket.getOutputStream());

        if (l_in.available() > 0) {
            l_in.skipBytes(l_in.available());
        }

        l_out.write(String.format("!dWrite %X %f", p_offset, p_value));
        l_out.flush();
    }

    public static int TcpReadUnsigned(Socket p_socket, int p_offset) throws IOException, InterruptedException {
        DataInputStream l_in = new DataInputStream(p_socket.getInputStream());
        PrintWriter l_out = new PrintWriter(p_socket.getOutputStream());

        if (l_in.available() > 0){
            l_in.skipBytes(l_in.available());
        }

        l_out.write(String.format("!uRead %X", p_offset));
        l_out.flush();

        while (true){
            if (l_in.available() > 0) {
                break;
            }
            else {
                TimeUnit.MILLISECONDS.sleep(10);
            }
        }
        int l_noBits = l_in.available();
        StringBuilder l_string = new StringBuilder();
        while(l_noBits-- > 0){
            l_string.append((char)l_in.readByte());
        }
        int l_received;
        if(l_string.toString().isEmpty()){
            l_received = 0;
        }
        else{
            l_received = Integer.parseInt(l_string.toString());
        }
        Log.i(DB_TAG, "Received" + Integer.toString(l_received));
        return l_received;
    }

    public static double TcpReadDouble(Socket p_socket, int p_offset) throws IOException, InterruptedException {
            DataInputStream l_in = new DataInputStream(p_socket.getInputStream());
            PrintWriter l_out = new PrintWriter(p_socket.getOutputStream());

            if (l_in.available() > 0){
                l_in.skipBytes(l_in.available());
            }

            l_out.write(String.format("!uRead %X", p_offset));
            l_out.flush();

            while (true){
                if (l_in.available() > 0) {
                    break;
                }
                else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            }
            int l_noBits = l_in.available();
            StringBuilder l_string = new StringBuilder();
            while(l_noBits-- > 0){
                l_string.append((char)l_in.readByte());
            }
            double l_received;
            if(l_string.toString().isEmpty()){
                l_received = 0;
            }
            else{
                l_received = Double.parseDouble(l_string.toString());
            }

            return l_received;
        }

    public static boolean TcpReadScopeBufferDirect(Socket p_socket, int p_mode, int p_decimation, int p_trgChanNo, double p_trgLevel, int p_noOfPosTrigger) throws IOException, InterruptedException {
        // Mode 0-immadiatelly; 1-rising edge; 2-falling edge
        // decimation plus 1; 0 means 1; 1 means 2, etc.
        // Zero-based (17-DVR)
        int l_failureCounter = 10;
        List<Double> l_receivingArray = new Vector<>();
        data = new Vector<>();

        TcpWriteUnsigned(p_socket, 0x100, p_mode + 256 * p_decimation);
        TcpWriteUnsigned(p_socket, 0x104, p_noOfPosTrigger + 65536 * p_trgChanNo);
        TcpWriteDouble(p_socket, 0x110, p_trgLevel);

        TcpWriteUnsigned(p_socket, 0x108, 1);
        TimeUnit.MILLISECONDS.sleep(100);
        TcpWriteUnsigned(p_socket, 0x108, 0);

        TimeUnit.MILLISECONDS.sleep(130 * (p_decimation + 1));
        int l_noOfSamples = TcpReadUnsigned(p_socket, 0x10C);
        for(int i = 0; i < 100; i++){
            if (l_failureCounter == 0){
                Log.e(DB_TAG, "Failure reading data from server!");
                return false;
            }
            l_noOfSamples = TcpReadUnsigned(p_socket, 0x10C);
            Log.i(DB_TAG, "NO samples:" + l_noOfSamples);

            if(l_noOfSamples < p_noOfPosTrigger){
                Log.i(DB_TAG, "Scope buffer not full... Failure counter:" + l_failureCounter--);
                TimeUnit.SECONDS.sleep(1);
            }
            else{
                break;
            }
        }

        if( l_noOfSamples < p_noOfPosTrigger){
            Log.e(DB_TAG,"Scope buffer error!");
        }

        DataInputStream l_in = new DataInputStream(p_socket.getInputStream());
        PrintWriter l_out = new PrintWriter(p_socket.getOutputStream());

        l_out.write("!IPCScope ");
        l_out.flush();

        while (true){
            if (l_in.available() > 0) {
                break;
            }
            else {
                TimeUnit.MILLISECONDS.sleep(10);
            }
        }
        Log.i(DB_TAG, "Reading data from server.");
        while(l_receivingArray.size() < 64 * p_noOfPosTrigger){
            if(l_in.available() > 0){
                long d = l_in.readLong();
                l_receivingArray.add(Double.longBitsToDouble(Long.reverseBytes(d)));
            }
        }

        Log.i(DB_TAG, "Inserting data to vector.");
        for(int i = 0; i < 64; i++){
            List<Double> l_tmpArray = new Vector<>();
            for(int j = p_noOfPosTrigger * i; j < p_noOfPosTrigger * (i + 1); j++){
                l_tmpArray.add(l_receivingArray.get(j));
            }
            data.add(l_tmpArray);
        }
        return true;
    }

}
