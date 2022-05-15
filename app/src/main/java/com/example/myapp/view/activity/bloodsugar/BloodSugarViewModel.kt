package com.example.myapp.view.activity.bloodsugar

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.myapp.model.common.util.http.RequestCallBack
import com.example.myapp.model.entity.HealthBloodSugar
import com.example.myapp.view.custom.MyHealthAndroidViewModel
import com.youth.xframe.utils.XNetworkUtils

class BloodSugarViewModel(application: Application) : MyHealthAndroidViewModel(application) {

    val mldBloodSugars = MutableLiveData<List<HealthBloodSugar>>()
    val mldBloodSugar = MutableLiveData<HealthBloodSugar>()

    override fun initData() {
    }

    fun getUserAllBloodSugarData(
        userId: Long, requestCallBack: RequestCallBack<List<HealthBloodSugar>>
    ) {
        if (XNetworkUtils.isAvailable()) {
            HealthBloodSugar.queryAllByUserId(userId, requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

    fun getUserLatestBloodSugarData(
        userId: Long, requestCallBack: RequestCallBack<HealthBloodSugar>
    ) {
        if (XNetworkUtils.isAvailable()) {
            HealthBloodSugar.queryLatestByUserId(userId, requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

}