package com.example.savepassword;

import android.content.Context;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by 临园 on 2016/4/5.
 */
public class SyncClient implements Runnable {
    private Context context;
    private Socket socket;

    public SyncClient(Context context,Socket socket){
        this.context = context;
        this.socket=socket;
    }

    @Override
    public void run() {
        String a= "hello test ";
        while(true){
            try {
                socket.getOutputStream().write(a.getBytes("UTF-8"));
                Thread.sleep(1000);
            } catch (IOException e) {
                e.printStackTrace();
                OnClose();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                OnClose();
                break;
            }
        }
    }

    public void OnClose(){
        try {
            if (socket!=null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
