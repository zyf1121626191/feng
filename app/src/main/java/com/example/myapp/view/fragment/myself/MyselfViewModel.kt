package com.example.myapp.view.fragment.myself

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.example.myapp.App
import com.example.myapp.R
import com.example.myapp.model.common.util.http.RequestCallBack
import com.example.myapp.model.entity.SysRole
import com.example.myapp.model.entity.SysUser
import com.example.myapp.model.entity.SysUserRoleRelation
import com.example.myapp.view.custom.MyAndroidViewModel
import com.youth.xframe.utils.XNetworkUtils
import com.youth.xframe.utils.XPreferencesUtils

class MyselfViewModel(application: Application) : MyAndroidViewModel(application) {

    val mldSysUser = MutableLiveData<SysUser>()

    override fun initData() {
        mldSysUser.value = JSONObject.parseObject(
            XPreferencesUtils.get(
                getApplication<App>().getString(R.string.key_user_json), "{}"
            ) as String, SysUser::class.java
        )
    }

    fun queryAllRole(requestCallBack: RequestCallBack<List<SysRole>>) {
        if (XNetworkUtils.isAvailable()) {
            SysRole.queryAllRole(requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

    fun applicationToBeAProfessional(
        userId: Long, requestCallBack: RequestCallBack<Boolean>
    ) {
        if (XNetworkUtils.isAvailable()) {
            SysUserRoleRelation.insert(2, userId, requestCallBack)
        } else {
            requestCallBack.onNoNetWork()
        }
    }

}