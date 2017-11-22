package com.ak.ta.salaamswipe.models.res;


public class ObjResp<T> extends GeneralResp {
    T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
