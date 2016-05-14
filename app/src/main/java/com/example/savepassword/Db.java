package com.example.savepassword;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Db extends SQLiteOpenHelper {

    /**
     * 改为公共静态变量，方便外部调用
     */
    public static final String TABLENAME = "password";
    public static final String BIKENUM = "number";
    public static final String BIKEPASSWORD = "password";

    public Db(Context context) {
        super(context, "db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE " + TABLENAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                BIKENUM + " INT DEFAULT \"\"," +
                BIKEPASSWORD + " TEXT DEFAULT \"\")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

}
