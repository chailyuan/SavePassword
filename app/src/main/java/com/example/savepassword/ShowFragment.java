package com.example.savepassword;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by re on 2016/3/28.
 * This is the first loaded fragment to show the data of the bicycle
 */
public class ShowFragment extends ListFragment implements View.OnClickListener{

    private EditText etNum,etPassword;
    private Button btnCheck,btnAdd;

    private SQLiteDatabase dbReader,dbWriter;
    private SimpleCursorAdapter adapter;

    private AdapterView.OnItemLongClickListener onitemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            new AlertDialog.Builder(getActivity()).setTitle("提醒").setMessage("你确定要删除这一项吗？")
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
    private AdapterView.OnItemClickListener onitemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = adapter.getCursor();
            cursor.moveToPosition(position);

            etNum.setText(cursor.getString(cursor.getColumnIndex(Db.BIKENUM)));
            etPassword.setText(cursor.getString(cursor.getColumnIndex(Db.BIKEPASSWORD)));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.showlayout, container, false);
        etNum = (EditText) view.findViewById(R.id.etNum);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        btnCheck = (Button) view.findViewById(R.id.btnCheck);
        btnAdd = (Button) view.findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(this);
        btnCheck.setOnClickListener(this);

        Db db = new Db(this.getActivity());
        dbReader = db.getReadableDatabase();
        dbWriter = db.getWritableDatabase();
        adapter = new SimpleCursorAdapter(this.getActivity(),
                R.layout.pass_list_cell,
                null,
                new String[]{Db.BIKENUM,Db.BIKEPASSWORD},
                new int[]{R.id.tvNumCell,R.id.tvPasswordCell});

        setListAdapter(adapter);

        refreshListView();

        return view;
    }

    //Generally, everything that uses UI widgets should be in onActivityCreated.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemLongClickListener(onitemLongClickListener);
        getListView().setOnItemClickListener(onitemClickListener);
    }

    //刷新列表
    private void refreshListView(){
//        Cursor c = dbReader.query(Db.TABLENAME,null,null,null,null,null,null);
        Cursor c = dbReader.rawQuery("SELECT * FROM " + Db.TABLENAME + " ORDER BY " + Db.BIKENUM, null);
        adapter.changeCursor(c);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCheck:
                if (!etNum.getText().toString().equals("")) {
                    Cursor lCursor = dbReader.rawQuery("SELECT * FROM "+Db.TABLENAME+" WHERE "+Db.BIKENUM+"=?",
                            new String[]{etNum.getText().toString()});
                    if (lCursor.moveToNext()) {
                        String pass = lCursor.getString(2);
                        etPassword.setText(pass);

                        new AlertDialog.Builder(this.getActivity())
                                .setTitle(etNum.getText().toString())
                                .setMessage("密码："+pass)
                                .setNegativeButton("关闭", null)
                                .show();

                    }else {

                        new AlertDialog.Builder(this.getActivity())
                                .setTitle("未查询到")
                                .setMessage("是否跳转至微信？")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("跳转", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                })
                                .show();

                        Toast.makeText(this.getActivity(), "未查询到", Toast.LENGTH_SHORT).show();
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
                        new AlertDialog.Builder(this.getActivity()).setTitle("提醒").setMessage("该车号的密码已存在,是否更新？")
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
                        Toast.makeText(this.getActivity(), "已添加", Toast.LENGTH_SHORT).show();
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
}
