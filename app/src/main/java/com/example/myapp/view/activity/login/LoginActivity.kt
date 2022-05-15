package com.example.myapp.view.activity.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.myapp.App
import com.example.myapp.R
import com.example.myapp.view.base.AbstractActivity
import com.youth.xframe.utils.permission.XPermission

class LoginActivity : AbstractActivity() {

    override fun preFinish(): Boolean {
        return true
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun initData(savedInstanceState: Bundle?) {
        val list = mutableListOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.add(Manifest.permission.ACTIVITY_RECOGNITION)
//            if(ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
//                //ask for permission
//                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1);
//            }
        }

        XPermission.requestPermissions(
            this,
            1,
            list.toTypedArray(),
            object : XPermission.OnPermissionListener {
                override fun onPermissionGranted() {
                    //ignore
                    val app = application as App
                    app.initStepSensor()
                }

                override fun onPermissionDenied() {
                    XPermission.showTipsDialog(this@LoginActivity)
                }
            }
        )
    }

    override fun initView() {
    }

}