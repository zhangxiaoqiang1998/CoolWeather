package com.coolweather.android.util;

/**
 * Created by Administrator on 2018/8/10.
 */


import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment implements HandleBackInterface {


    @Override
    public boolean onBackPressed() {
        return HandleBackUtil.handleBackPress(this);
    }
}