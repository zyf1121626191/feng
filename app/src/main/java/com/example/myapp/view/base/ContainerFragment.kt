package com.example.myapp.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapp.App
import com.example.myapp.view.custom.LoadingView
import com.youth.xframe.base.ICallback

abstract class ContainerFragment : Fragment(), ICallback {

    var userId = 0L
    lateinit var loadingView: LoadingView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userId = if (savedInstanceState != null && savedInstanceState.containsKey(KEY_USER_ID)) {
            savedInstanceState.getLong(KEY_USER_ID)
        } else {
            App.user.id
        }
        return if (layoutId > 0) {
            inflater.inflate(layoutId, container, false)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(KEY_USER_ID, userId)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadingView = LoadingView.wrap(this)
        loadingView.showLoading()
        initData(savedInstanceState)
        initView()
    }

    companion object {
        const val KEY_USER_ID = "USER_ID"
    }

}