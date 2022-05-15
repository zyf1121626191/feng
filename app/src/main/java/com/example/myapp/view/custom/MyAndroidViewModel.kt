package com.example.myapp.view.custom

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class MyAndroidViewModel(application: Application) : AndroidViewModel(application) {
    abstract fun initData()
}