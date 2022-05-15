package com.example.myapp.view.activity.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private var username: String = ""
    private var password: String = ""
}