package com.example.myapp.view.fragment.health

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HealthViewModel : ViewModel() {
    val mldStepValue = MutableLiveData(0)
}