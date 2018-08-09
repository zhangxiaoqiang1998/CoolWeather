package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2018/8/9.
 */

public class Province extends DataSupport {
    private int id;
    private String proincecName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProincecName() {
        return proincecName;
    }

    public void setProincecName(String proincecName) {
        this.proincecName = proincecName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
