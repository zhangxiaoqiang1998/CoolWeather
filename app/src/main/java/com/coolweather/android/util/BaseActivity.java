package com.coolweather.android.util;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2018/8/10.
 */


public class BaseActivity extends AppCompatActivity {


    @Override
    public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            super.onBackPressed();
        }
    }
}