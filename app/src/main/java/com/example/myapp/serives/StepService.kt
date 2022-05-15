package com.example.myapp.serives

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import androidx.annotation.Nullable
import com.example.myapp.R
import com.example.myapp.App

class StepService : Service(), SensorEventListener {
    class ConstantData {
        companion object{
            const val MSG_FROM_CLIENT = 0
            const val MSG_FROM_SERVER = 1
            const val MSG_INIT_SERVICE = 2
            const val NOTIFY_ID = 110

            const val CHANNEL_ID = "120"
            const val CHANNEL_NAME = "StepService"
        }
    }
    //当前步数
    private var currentStep: Int = 0
    //传感器
    private var sensorManager: SensorManager? = null
    //计步传感器类型 0-counter 1-detector
    private var stepSensor = -1
    //发送消息，用来和Service之间传递步数
    private val messenger = Messenger(MessengerHandler())
    //是否有当天的记录
    private var hasRecord: Boolean = false
    //未记录之前的步数
    private var hasStepCount: Int = 0
    //下次记录之前的步数
    private var previousStepCount: Int = 0
    private var builder: Notification.Builder? = null

    private var notificationManager: NotificationManager? = null
    private var nfIntent: Intent? = null


    override fun onCreate() {
        super.onCreate()
        Thread { getStepDetector() }.start()
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        /**
         * 此处设将Service为前台，不然当APP结束以后很容易被GC给干掉，
         * 这也就是大多数音乐播放器会在状态栏设置一个原理大都是相通的
         */
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        //----------------  针对8.0 新增代码 --------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = Notification.Builder(this.applicationContext, ConstantData.CHANNEL_ID)
            val notificationChannel =
                NotificationChannel(
                    ConstantData.CHANNEL_ID,
                    ConstantData.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
                )
            notificationChannel.enableLights(false)//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false)//是否显示角标
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            notificationManager?.createNotificationChannel(notificationChannel)
            builder?.setChannelId(ConstantData.CHANNEL_ID)
        } else {
            builder = Notification.Builder(this.applicationContext)
        }

        /**
         * 设置点击通知栏打开的界面，此处需要注意了，
         * 如果你的计步界面不在主界面，则需要判断app是否已经启动，
         * 再来确定跳转页面，这里面太多坑
         */
        nfIntent = Intent(this, App::class.java)
        setStepBuilder()
        // 参数一：唯一的通知标识；参数二：通知消息。
        startForeground(ConstantData.NOTIFY_ID, builder?.build())// 开始前台服务
        return START_STICKY
    }

    /**
     * 自定义handler
     */
    private inner class MessengerHandler : Handler() {
        override fun handleMessage(msg: Message) = when (msg.what) {
            ConstantData.MSG_FROM_CLIENT -> try {
                //这里负责将当前的步数发送出去，可以在界面或者其他地方获取，我这里是在MainActivity中获取来更新界面
                val messenger = msg.replyTo
                val replyMsg = Message.obtain(null, ConstantData.MSG_FROM_SERVER)
                val bundle = Bundle()
                bundle.putInt("steps", currentStep)
                replyMsg.data = bundle
                messenger.send(replyMsg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            else -> super.handleMessage(msg)
        }
    }

    /**
     * 获取传感器实例
     */
    private fun getStepDetector() {
        if (sensorManager != null) {
            sensorManager = null
        }
        // 获取传感器管理器的实例
        sensorManager = this
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //android4.4以后可以使用计步传感器
        if (Build.VERSION.SDK_INT >= 19) {
            addCountStepListener()
        }
    }

    /**
     * 添加传感器监听
     */
    private fun addCountStepListener() {
        val countSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (countSensor != null) {
            stepSensor = 0
            sensorManager!!.registerListener(
                this@StepService,
                countSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else if (detectorSensor != null) {
            stepSensor = 1
            sensorManager!!.registerListener(
                this@StepService,
                detectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * 由传感器记录当前用户运动步数，注意：该传感器只在4.4及以后才有，并且该传感器记录的数据是从设备开机以后不断累加，
     * 只有当用户关机以后，该数据才会清空，所以需要做数据保护
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (stepSensor == 0) {
            val tempStep = event.values[0].toInt()
            if (!hasRecord) {
                hasRecord = true
                hasStepCount = tempStep
            } else {
                val thisStepCount = tempStep - hasStepCount
                currentStep += thisStepCount - previousStepCount
                previousStepCount = thisStepCount
            }
            saveStepData()
        } else if (stepSensor == 1) {
            if (event.values[0].toDouble() == 1.0) {
                currentStep++
                saveStepData()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }


    /**
     * 保存当天的数据到数据库中，并去刷新通知栏
     */
    private fun saveStepData() {
        // todo: 走网络请求存入数据库
        setStepBuilder()
    }

    private fun setStepBuilder() {
        builder?.setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                nfIntent,
                0
            )
        ) // 设置PendingIntent
            ?.setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.ic_launcher
                )
            )
            ?.setContentTitle("今日步数" + currentStep + "步")
            ?.setSmallIcon(R.mipmap.ic_launcher)
            ?.setContentText("加油，要记得勤加运动哦")
        // 获取构建好的Notification
        val stepNotification = builder?.build()
        //调用更新
        notificationManager?.notify(ConstantData.NOTIFY_ID, stepNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        //主界面中需要手动调用stop方法service才会结束
        stopForeground(true)
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }
}