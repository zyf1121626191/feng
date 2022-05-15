package com.example.myapp.view.activity.sport

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.myapp.App
import com.example.myapp.R
import com.example.myapp.model.common.util.http.HttpCallBack
import com.example.myapp.model.common.util.http.RequestCallBack
import com.example.myapp.model.entity.HealthSport
import com.example.myapp.view.custom.MyHealthAndroidViewModel
import com.youth.xframe.utils.XNetworkUtils
import com.youth.xframe.utils.XPreferencesUtils

class SportViewModel(application: Application) : MyHealthAndroidViewModel(application) {

    val todayStepValue = MutableLiveData(0)
    var everyDayStepValue = 0

    override fun initData() {
        todayStepValue.value = XPreferencesUtils.get(
            getApplication<App>().getString(R.string.key_health_step_value),
            getApplication<App>().resources.getInteger(R.integer.default_step_value)
        ) as Int
        everyDayStepValue = XPreferencesUtils.get(
            getApplication<App>().getString(R.string.key_health_every_day_step_value),
            getApplication<App>().resources.getInteger(R.integer.default_every_day_step_value)
        ) as Int
    }
    fun uploadTodayStepValue(userId: Long) {
        todayStepValue.value = XPreferencesUtils.get(
            getApplication<App>().getString(R.string.key_health_step_value),
            getApplication<App>().resources.getInteger(R.integer.default_step_value)
        ) as Int
        everyDayStepValue = XPreferencesUtils.get(
            getApplication<App>().getString(R.string.key_health_every_day_step_value),
            getApplication<App>().resources.getInteger(R.integer.default_every_day_step_value)
        ) as Int
        HealthSport.insertOrReplace(HealthSport().apply {
            this.userId = userId
            date = System.currentTimeMillis()
            stepValue = todayStepValue.value
        }, object : HttpCallBack<HealthSport>() {
            override fun onSuccess(result: HealthSport) {
                //ignore
            }

            override fun onFailed(errorCode: Int, error: String) {
                //ignore
            }
        })
    }

}