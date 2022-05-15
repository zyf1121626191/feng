package com.example.myapp.model.common.util.http;


import org.jetbrains.annotations.NotNull;

public abstract class HttpCallBack<Result> {

    public abstract void onSuccess(@NotNull Result result);

    public abstract void onFailed(@NotNull Integer errorCode, @NotNull String error);

}
