package com.example.savepassword;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.csvreader.CsvReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by re on 2016/3/28.
 * 这是导入导出的Fragment
 */
public class ExportFragment extends Fragment implements View.OnClickListener {

    public static String exportFolder;
    public static String importFolder;

    public static final String exportCSVName = "export.csv";
    public static String importCSVName = null;

    public static final String argKey = "pathImport";

    private Button btnExport, btnImport, btnBrowse;
    private SQLiteDatabase dbReader, dbWriter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exportlayout, container, false);
        btnExport = (Button) view.findViewById(R.id.btn_export);
        btnImport = (Button) view.findViewById(R.id.btn_import);
        btnBrowse = (Button) view.findViewById(R.id.btn_browse);
        btnExport.setOnClickListener(this);
        btnImport.setOnClickListener(this);
        btnBrowse.setOnClickListener(this);

        Db db = new Db(getActivity());
        dbReader = db.getReadableDatabase();
        dbWriter = db.getWritableDatabase();

        importCSVName = getArguments().getString(argKey);
        System.out.println("传递过来的参数是：" + importCSVName);
        if (!importCSVName.equals("null")) {
            //当传递过来的不是空值的时候，启动导入操作
            //询问是否导入，导入数据是否覆盖
            AskImportMode(null);
        }

        //创建外部目录
        exportFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SavePassword/Export";
        importFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SavePassword/Import";
        File newFolder = new File(exportFolder);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        newFolder = new File(importFolder);
        if (!newFolder.exists())
            newFolder.mkdirs();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_browse:
                ShowFileChooser();
                break;
            case R.id.btn_export:
                Cursor cursor = dbReader.rawQuery("SELECT * FROM " + Db.TABLENAME, null);
                ExportToCSV(cursor, exportCSVName);
                break;
            case R.id.btn_import:
                //导入功能要求用户把csv文件放入到savepassword/Import文件夹中
                File file = new File(importFolder);
                final File[] files = file.listFiles();
                if (files != null) {
                    int count = files.length;
                    System.out.println("文件数量：" + count);
                    if (count < 1) {
                        break;
                    } else {
                        for (int i = 0; i < count; i++) {
                            importCSVName = importFolder + "/" + files[i].getName();
                            System.out.println("导入文件名称：" + importCSVName);
                            //先询问是否导入，再询问是否覆盖
                            AskImportMode(files[i]);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            Uri uri = data.getData();
            System.out.println(data.getDataString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void ShowFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        getActivity().startActivityForResult(Intent.createChooser(intent, "请选择一个CSV文件！"), 10);
    }

    /**
     * @param finalFile 传入的要导入的文件名称
     */
    private void AskImportMode(final File finalFile) {
        //先询问是否导入，再询问是否覆盖
        final String localfileName = (finalFile == null ? "" : finalFile.getName());
        new AlertDialog.Builder(getActivity()).setTitle("提醒").setMessage("是否从文件" + localfileName + "导入数据？")
                .setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AlertDialog.Builder(getActivity()).setTitle("提醒").setMessage("导入" + localfileName + "时,重复数据是否覆盖？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ImportFromCSV(false);
                            }
                        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ImportFromCSV(true);
                    }
                }).show();
            }
        }).show();
    }

    /**
     * 从CSV导入到数据库,Exception List:FileNotFoundException,
     *
     * @param cover,是否覆盖掉数据库中的数据
     * @throws Exception
     */
    private void ImportFromCSV(boolean cover) {
        //已知文件名的开头是file://，此名称不能获取到文件，类似一个uri地址
        importCSVName = importCSVName.replace("file://", "");
        try {
            CsvReader reader = null;

            reader = new CsvReader(importCSVName, ',', Charset.forName("GBK"));

            reader.readHeaders();
            String[] headers = reader.getHeaders();

            List<Object[]> list = new ArrayList<Object[]>();
            while (reader.readRecord()) {
                list.add(reader.getValues());
            }

            Object[][] datas = new String[list.size()][];

            for (int i = 0; i < list.size(); i++) {
                datas[i] = list.get(i);
            }

            //导入数据库
            for (int i = 0; i < datas.length; i++) {
                Object[] data = datas[i];
                String num = (String) data[0];
                String password = (String) data[1];

                //首先查询数据库中有没有数据
                Cursor lCursor = dbReader.rawQuery("SELECT * FROM " + Db.TABLENAME + " WHERE " + Db.BIKENUM + "=?",
                        new String[]{num});
                if (lCursor.moveToNext()) {
                    //存在的时候该怎么办,有cover=true则覆盖，cover=false则不覆盖
                    if (cover) {
                        //直接更新数据库数据
                        ContentValues cv = new ContentValues();
                        cv.put(Db.BIKEPASSWORD, password);
                        String whereClause = Db.BIKENUM + "=?";
                        dbWriter.update(Db.TABLENAME, cv, whereClause, new String[]{num});
                    } else {
                        //不覆盖则不进行操作
                    }
                } else {
                    //不存在的时候直接插入数据
                    ContentValues values = new ContentValues();
                    values.put(Db.BIKENUM, num);
                    values.put(Db.BIKEPASSWORD, password);
                    dbWriter.insert(Db.TABLENAME, null, values);
                }
            }
            Toast.makeText(getActivity(), "导入完毕", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "导入失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void ExportToCSV(Cursor cursor, String fileName) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        int rowCount = 0;
        int colCount = 0;
        FileWriter fw;
        BufferedWriter bfw;
        File sdCardDir = new File(exportFolder);//放到外部目录
        File saveFile = new File(sdCardDir, str + fileName);

        try {
            rowCount = cursor.getCount();
            colCount = cursor.getColumnCount();
            fw = new FileWriter(saveFile);
            bfw = new BufferedWriter(fw);

            if (rowCount > 0) {
                cursor.moveToFirst();
                //写入表头
                for (int i = 1; i < colCount; i++) {
                    if (i != colCount - 1)
                        bfw.write(cursor.getColumnName(i) + ',');
                    else
                        bfw.write(cursor.getColumnName(i));
                }
                //写好表头后换行
                bfw.newLine();
                //写入数据
                for (int i = 0; i < rowCount; i++) {
                    cursor.moveToPosition(i);
//                    Log.v("导出数据", "正在导出第" + (i + 1) + "条");
                    for (int j = 1; j < colCount; j++) {
                        if (j != colCount - 1)
                            bfw.write(cursor.getString(j) + ',');
                        else
                            bfw.write(cursor.getString(j));
                    }
                    //写入一行后换行
                    bfw.newLine();
                }
            }
            //将缓存数据写入文件
            bfw.flush();
            //释放缓存
            bfw.close();
            //导出完毕
            Toast.makeText(getActivity(), "导出完毕,导出文件存放在" + exportFolder, Toast.LENGTH_LONG).show();
            Log.v("导出数据", "导出完毕！");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }
}
