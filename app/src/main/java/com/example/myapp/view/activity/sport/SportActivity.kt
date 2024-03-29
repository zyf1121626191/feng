package com.example.myapp.view.activity.sport

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapp.App
import com.example.myapp.R
import com.example.myapp.model.common.enums.ResultCode
import com.example.myapp.model.common.util.MPAChartUtil
import com.example.myapp.model.common.util.http.RequestCallBack
import com.example.myapp.model.entity.HealthSport
import com.example.myapp.model.eventbus.EveryDayStepValueChange
import com.example.myapp.model.eventbus.StepValueChange
import com.example.myapp.view.base.ContainerActivity
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.youth.xframe.utils.XDateUtils
import com.youth.xframe.utils.XPreferencesUtils
import kotlinx.android.synthetic.main.activity_sport.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat


class SportActivity : ContainerActivity() ,SensorEventListener{

    private lateinit var viewModel: SportViewModel
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var textView_step: TextView
    private var current_value = 0.0
    private var step = 0;
    private var original_value =0.0;
    private var last_value =0.0;
    private var motionState=true; //是否处于运动状态
    private var processState=false;


    override fun getLayoutId(): Int {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        return R.layout.activity_sport
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider.AndroidViewModelFactory(application)
            .create(SportViewModel::class.java)
        viewModel.todayStepValue.observe(this, Observer {
            text_view_step_value.text = it.toString()
            completed_view_sport.setProgress(it)
            text_view_cal.text = String.format("%.2f", it / 20F)
            text_view_distance.text = String.format("%.2f", it * 0.7F / 1000)
        })
        viewModel.initData()
        completed_view_sport.setmTotalProgress(viewModel.everyDayStepValue)
    }

    override fun initView() {
        ctl.setCollapsedTitleTextColor(getColor(android.R.color.white))
        ctl.setExpandedTitleColor(getColor(android.R.color.white))
        smart_refresh_layout.setOnRefreshListener {
            getUserTodaySportData(false)
            getUserAllSportData(false)
        }
        if (userId == App.user.id) {
            material_card_view_analyze_sport.setOnClickListener {
                startActivity(Intent(this, SportReportActivity::class.java))
            }
        } else {
            material_card_view_analyze_sport.visibility = View.GONE
        }
        getUserTodaySportData(true)
        getUserAllSportData(true)
    }

    private fun getUserAllSportData(needShowContent: Boolean) {
        HealthSport.queryAllByUserId(userId, object : RequestCallBack<List<HealthSport>>() {
            override fun onSuccess(result: List<HealthSport>) {
                updateStepValueChart(result)
                updateCalorieChart(result)
                showSuccess(needShowContent)
            }

            override fun onFailed(errorCode: Int, error: String) {
                showFailed()
            }

            override fun onNoNetWork() {
                showNoNetWork()
            }
        })
    }

    private fun showFailed() {
        smart_refresh_layout.finishRefresh(false)
        loadingView.setOnRetryClickListener {
            loadingView.showLoading()
            getUserTodaySportData(true)
            getUserAllSportData(true)
        }.showError()
    }

    private fun getUserTodaySportData(needShowContent: Boolean) {
        HealthSport.queryAllByUserIdAndDate(
            userId, System.currentTimeMillis(), object : RequestCallBack<HealthSport>() {
                override fun onSuccess(result: HealthSport) {
                    //本地数据的保存时间
                    val updateTime = XPreferencesUtils.get(
                        getString(R.string.key_health_step_value_update_time),
                        0L
                    ) as Long
                    if (userId != App.user.id || !XDateUtils.isSameDay(updateTime) || viewModel.todayStepValue.value ?: 0 < result.stepValue) {
                        viewModel.todayStepValue.value = result.stepValue
                    }
                    showSuccess(needShowContent)
                }

                override fun onFailed(errorCode: Int, error: String) {
                    if (errorCode == ResultCode.SUCCESS.code) {
                        showSuccess(needShowContent)
                    } else {
                        showFailed()
                    }
                }

                override fun onNoNetWork() {
                    showNoNetWork()
                }
            })
    }

    private fun showSuccess(needShowContent: Boolean) {
        smart_refresh_layout.finishRefresh()
        if (needShowContent) {
            loadingView.showContent()
        }
    }

    private fun updateStepValueChart(sportList: List<HealthSport>) {
        val entries = ArrayList<Entry>()
        val xValues = ArrayList<String>()
        sportList.forEachIndexed { index, healthSport ->
            entries.add(Entry(index.toFloat(), healthSport.stepValue.toFloat()))
            xValues.add(XDateUtils.millis2String(healthSport.date, "yyyy/MM/dd"))
        }
        MPAChartUtil.updateLineChart(
            line_chart_step,
            this,
            entries,
            xValues,
            "",
            MPAChartUtil.defaultChartColors,
            "",
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString() + getString(R.string.step)
                }
            },
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString() + getString(R.string.step)
                }
            }
        )
    }

    private fun updateCalorieChart(sportList: List<HealthSport>) {
        val entries = ArrayList<Entry>()
        val xValues = ArrayList<String>()
        sportList.forEachIndexed { index, healthSport ->
            entries.add(Entry(index.toFloat(), healthSport.calorie))
            xValues.add(XDateUtils.millis2String(healthSport.date, "yyyy/MM/dd"))
        }
        MPAChartUtil.updateLineChart(
            line_chart_cal,
            this,
            entries,
            xValues,
            "",
            MPAChartUtil.defaultChartColors,
            "",
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString() + getString(R.string.cal)
                }
            },
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return DecimalFormat("0.##${getString(R.string.cal)}").format(value)
                }
            }
        )
    }

    private fun showNoNetWork() {
        loadingView.setOnRetryClickListener {
            loadingView.showLoading()
            getUserTodaySportData(true)
            getUserAllSportData(true)
        }.showNoNetwork()
    }

    override fun preFinish(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.setEveryDayStepValue) {
            var everyDayStepValue = XPreferencesUtils.get(
                getString(R.string.key_health_every_day_step_value),
                resources.getInteger(R.integer.default_every_day_step_value)
            ) as Int
            val rate = 1000
            val max = 100
            val min = 1
            val inflate =
                layoutInflater.inflate(R.layout.dialog_sport_every_day_step_value, null, false)
            val numberPicker = inflate.findViewById<NumberPicker>(R.id.number_picker).apply {
                maxValue = max
                minValue = min
                value = everyDayStepValue / rate
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            }
            numberPicker.setFormatter {
                return@setFormatter (it * rate).toString()
            }
            numberPicker.setOnValueChangedListener { _, _, newVal ->
                everyDayStepValue = newVal * rate
            }
            AlertDialog.Builder(this).setCancelable(false)
                .setTitle(R.string.set_every_day_step_value)
                .setView(inflate)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    XPreferencesUtils.put(
                        getString(R.string.key_health_every_day_step_value),
                        everyDayStepValue
                    )
                    completed_view_sport.setmTotalProgress(everyDayStepValue)
                    EventBus.getDefault().post(EveryDayStepValueChange(everyDayStepValue))
                }.setNegativeButton(R.string.cancel, null).show()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return if (userId == App.user.id) {
            R.menu.sport
        } else {
            0
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStepValueChange(stepValueChange: StepValueChange) {
        if (userId == App.user.id) {
            viewModel.todayStepValue.value = stepValueChange.stepValue
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        viewModel.uploadTodayStepValue(userId)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val range= 1; //设置一个精度范围
        val value = event.values
        current_value =magnitude(value[0],value[1],value[2]); //计算当前的模

        //向上加速的状态
        if(motionState==true){
            if (current_value >= last_value)
                last_value = current_value;
            else {
                //检测到一次峰值
                if(Math.abs(current_value-last_value)>range){
                    original_value=current_value;
                    motionState=false;
                }
            }
        }
        //向下加速的状态
        if(motionState==false){
            if (current_value <= last_value)
                last_value = current_value;
            else {
                //检测到一次峰值
                if(Math.abs(current_value-last_value)>range){
                    original_value=current_value;
                    if (processState==true){
                        step++; //检测到开始记录，步数加1
                        if(processState==true){
                            onStepValueChange(stepValueChange = StepValueChange(step))
                            textView_step.setText(step.toString()); //更新读数
                        }
                    }
                    motionState=true;
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    fun magnitude(x :Float,y : Float, z :Float) : Double {
        var magnitude = 0.0
        magnitude= Math.sqrt((x*x+y*y+z*z).toDouble()).toDouble();
        return magnitude;
    }
}
