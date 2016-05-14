package com.example.savepassword;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectService extends Service {
    Db db;
    private SQLiteDatabase dbReader,dbWriter;
    public ConnectService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DoListen();
            }
        }
        ).start();
    }

    //监听端口，等待pc的连接
    private void DoListen(){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(10086);
            Socket socket = serverSocket.accept();
            new Thread(new SyncClient(this,socket)).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  new Binder();
    }
}
