package com.example.myapp.model.common.util.http;


public abstract class RequestCallBack<Result> extends HttpCallBack<Result> {

    public abstract void onNoNetWork();

}
