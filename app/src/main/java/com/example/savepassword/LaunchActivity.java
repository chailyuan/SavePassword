package com.example.savepassword;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by re on 2016/3/28.
 */
public class LaunchActivity extends Activity {

    private DrawerLayout mDrawerLayout;
    private ListView listView;
    private ArrayList<String> menuLists;
    private ArrayAdapter<String> arrayAdapter;
    private CharSequence mDrawerTitle;


    private ActionBarDrawerToggle mDrawerToggle;

    private AdapterView.OnItemClickListener onitemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //从position获取当前要打开的是哪个fragment
            switch (position){
                case 0:
                    LoadFragment(0,null);
                    break;
                case 1:
                    Bundle bundle = new Bundle();
                    bundle.putString(ExportFragment.argKey,"null");
                    LoadFragment(1,bundle);
                    break;

                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawerlayout);

        mDrawerTitle=getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView = (ListView) findViewById(R.id.left_drawer);

        menuLists = new ArrayList<String>();
        menuLists.add("密码显示");
        menuLists.add("数据导入导出");
        menuLists.add("设置");
        menuLists.add("关于");

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,menuLists);
        listView.setAdapter(arrayAdapter);

        //抽屉的单击事件
        listView.setOnItemClickListener(onitemClickListener);

        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,
                R.drawable.ic_drawer,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();//onprepareoptionsmenu
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerView.getWindowToken(),0);

                getActionBar().setTitle(R.string.drawer_open);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //开启activitybar上的app icon的功能
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        //CSV文件的打开事件
        Intent intentCSV = getIntent();
        String actionCSV = intentCSV.getAction();
        if (Intent.ACTION_VIEW.equals(actionCSV)) {
            //携带参数
            Bundle bundle = new Bundle();
            bundle.putString(ExportFragment.argKey,intentCSV.getDataString());//传递返回的文件路径

            LoadFragment(1,bundle);
        } else {
            LoadFragment(0,null);
        }
    }

    /**
     *
     * @param fragmentNum Fragment的编号，与侧拉菜单中的编号相对应
     * @param bundle 传递到fragment的参数
     */
    private void LoadFragment(int fragmentNum,Bundle bundle){
        Fragment fragment = null;
        switch (fragmentNum){
            case 0:
                fragment = new ShowFragment();
                break;
            case 1:
                fragment = new ExportFragment();
                break;
            case 2:
                break;
            case 3:
                break;

            default:
                break;
        }
        if (bundle != null){
            fragment.setArguments(bundle);
        }
        if ( fragment != null){
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
            mDrawerLayout.closeDrawer(listView);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10){
            System.out.println(data.getDataString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //用来显示和隐藏menu图标
        boolean isDrawOpen = mDrawerLayout.isDrawerOpen(listView);
        menu.findItem(R.id.action_websearch).setVisible(!isDrawOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)){

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
