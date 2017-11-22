package com.ak.ta.salaamswipe.models.res;

import java.util.List;


public class ListResp<T> extends GeneralResp {
    List<T> data;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
