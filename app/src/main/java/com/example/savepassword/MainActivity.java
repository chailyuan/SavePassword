package com.example.savepassword;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnClickListener {

    private EditText etNum, etPassword;
    private Button btnCheck, btnAdd;

    private SQLiteDatabase dbReader, dbWriter;
    private SimpleCursorAdapter adapter;
    private OnItemLongClickListener listIntemLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

            new AlertDialog.Builder(MainActivity.this).setTitle("提醒").setMessage("你确定要删除这一项吗？")
                    .setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Cursor cursor = adapter.getCursor();
                    cursor.moveToPosition(position);
                    int itemID = cursor.getInt(cursor.getColumnIndex("_id"));
                    dbWriter.delete(Db.TABLENAME, "_id=?", new String[]{itemID + ""});
                    // dbWriter.close();

                    refreshListView();
                }
            }).show();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showlayout);

        etNum = (EditText) findViewById(R.id.etNum);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnCheck.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        Db db = new Db(this);
        dbReader = db.getReadableDatabase();
        dbWriter = db.getWritableDatabase();

        adapter = new SimpleCursorAdapter(this,
                R.layout.pass_list_cell,
                null,
                new String[]{Db.BIKENUM, Db.BIKEPASSWORD},
                new int[]{R.id.tvNumCell, R.id.tvPasswordCell});

        setListAdapter(adapter);

        refreshListView();

        getListView().setOnItemLongClickListener(listIntemLongClickListener);

    }

    private void refreshListView() {
        //刷新listview
        Cursor cursor = dbReader.query(Db.TABLENAME, null, null, null, null, null, null);
        adapter.changeCursor(cursor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCheck:
                if (!etNum.getText().toString().equals("")) {
                    Cursor lCursor = dbReader.rawQuery("SELECT * FROM "+Db.TABLENAME+" WHERE "+Db.BIKENUM+"=?",
                            new String[]{etNum.getText().toString()});
                    if (lCursor.moveToNext()) {
                        Toast.makeText(this, "已查询到", Toast.LENGTH_SHORT).show();
                        String pass = lCursor.getString(2);
                        etPassword.setText(pass);
                    }else {
                        Toast.makeText(this, "未查询到", Toast.LENGTH_SHORT).show();
                        etPassword.setText("");
                    }
                    lCursor.close();
                }
                // 查询密码
                break;
            case R.id.btnAdd:
                if (!etNum.getText().toString().equals("") &&
                        !etPassword.getText().toString().equals("")) {
                    // 添加密码
                    //添加之前先查询有没有
                    final Cursor lCursor = dbReader.rawQuery("SELECT * FROM "+Db.TABLENAME+" WHERE "+Db.BIKENUM+"=?",
                            new String[]{etNum.getText().toString()});
                    if (lCursor.moveToNext()) {
                        // 已存在提示是否更新
                        new AlertDialog.Builder(MainActivity.this).setTitle("提醒").setMessage("该车号的密码已存在,是否更新？")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //方法一，删除后再添加
//                                        int itemID = lCursor.getInt(lCursor.getColumnIndex("_id"));//获取到了_id
//                                        dbWriter.delete(Db.TABLENAME, "_id=?", new String[]{itemID + ""});
//                                        ContentValues values = new ContentValues();
//                                        values.put(Db.BIKENUM, etNum.getText().toString());
//                                        values.put(Db.BIKEPASSWORD, etPassword.getText().toString());
//                                        dbWriter.insert(Db.TABLENAME, null, values);

                                        //直接更新数据库数据
                                        ContentValues cv = new ContentValues();
                                        cv.put(Db.BIKEPASSWORD,etPassword.getText().toString());
                                        String whereClause = Db.BIKENUM+"=?";
                                        dbWriter.update(Db.TABLENAME,cv,whereClause,new String[]{etNum.getText().toString()});

                                        etPassword.setText("");
                                        etNum.setText("");

                                        refreshListView();
                                    }
                                }).show();

                    } else {
                        // 不存在的时候
                        Toast.makeText(this, "已添加", Toast.LENGTH_SHORT).show();
                        ContentValues values = new ContentValues();
                        values.put(Db.BIKENUM, etNum.getText().toString());
                        values.put(Db.BIKEPASSWORD, etPassword.getText().toString());
                        dbWriter.insert(Db.TABLENAME, null, values);
                        etPassword.setText("");
                        etNum.setText("");
                        refreshListView();
                    }
                    etNum.requestFocus();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbWriter.close();
    }

}
