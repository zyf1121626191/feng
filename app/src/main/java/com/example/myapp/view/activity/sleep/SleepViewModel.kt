package com.example.myapp.view.activity.sleep

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.myapp.model.common.util.http.RequestCallBack
import com.example.myapp.model.entity.HealthSleep
import com.example.myapp.view.custom.MyHealthAndroidViewModel
import com.youth.xframe.utils.XNetworkUtils

class SleepViewModel(application: Application) : MyHealthAndroidViewModel(application) {

    val mldHealthSleep = MutableLiveData<HealthSleep>()

    fun getUserSleepData(userId: Long, requestCallBack: RequestCallBack<HealthSleep>) {
        if (XNetworkUtils.isAvailable()) {
            HealthSleep.queryByUserIdAndDate(userId, null, requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

    fun getUserAllSleepData(userId: Long, requestCallBack: RequestCallBack<List<HealthSleep>>) {
        if (XNetworkUtils.isAvailable()) {
            HealthSleep.queryAllByUserId(userId, requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

    override fun initData() {
    }

}