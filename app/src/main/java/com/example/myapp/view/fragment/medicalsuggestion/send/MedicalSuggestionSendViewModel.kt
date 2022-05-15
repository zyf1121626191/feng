package com.example.myapp.view.fragment.medicalsuggestion.send

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONArray
import com.example.myapp.App
import com.example.myapp.R
import com.example.myapp.model.entity.CustomMedicalSuggestion
import com.example.myapp.model.entity.SysUserRoleRelation
import com.example.myapp.view.custom.MyAndroidViewModel
import com.youth.xframe.utils.XPreferencesUtils

class MedicalSuggestionSendViewModel(application: Application) : MyAndroidViewModel(application) {

    val customMedicalSuggestionList = ArrayList<CustomMedicalSuggestion>()
    var isProfessional = MutableLiveData<Boolean>()
    var page = 0
    var size = 10

    fun resetPageAndSize() {
        page = 0
        size = 10
    }

    override fun initData() {
        updateRoleRelation()
    }

    fun updateRoleRelation() {
        val list = JSONArray.parseArray(
            XPreferencesUtils.get(
                getApplication<App>().getString(R.string.key_user_role_relation_json),
                "[]"
            ) as String, SysUserRoleRelation::class.java
        )
        list.forEach {
            if (it.roleId == 2L) {
                isProfessional.value = true
                return
            }
        }
        isProfessional.value = false
    }

}