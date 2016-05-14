package com.example.savepassword;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("收到了一个Broadcast消息");
        //通过BroadcastReceiver启动activity
        Intent i = new Intent(context,LaunchActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        //启动服务
        context.startService(new Intent(context,ConnectService.class));
    }
}
