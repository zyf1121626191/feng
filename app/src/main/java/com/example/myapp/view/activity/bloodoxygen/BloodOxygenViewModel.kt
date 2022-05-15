package com.example.myapp.view.activity.bloodoxygen

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.myapp.model.common.util.http.RequestCallBack
import com.example.myapp.model.entity.HealthBloodOxygen
import com.example.myapp.view.custom.MyHealthAndroidViewModel
import com.youth.xframe.utils.XNetworkUtils

class BloodOxygenViewModel(application: Application) : MyHealthAndroidViewModel(application) {

    val mldBloodOxygens = MutableLiveData<List<HealthBloodOxygen>>()
    val mldBloodOxygen = MutableLiveData<HealthBloodOxygen>()

    override fun initData() {

    }

    fun getUserAllBloodOxygenData(
        userId: Long, requestCallBack: RequestCallBack<List<HealthBloodOxygen>>
    ) {
        if (XNetworkUtils.isAvailable()) {
            HealthBloodOxygen.queryAllByUserId(userId, requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

    fun getUserLatestBloodOxygenData(
        userId: Long, requestCallBack: RequestCallBack<HealthBloodOxygen>
    ) {
        if (XNetworkUtils.isAvailable()) {
            HealthBloodOxygen.queryLatestByUserId(userId, requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

}