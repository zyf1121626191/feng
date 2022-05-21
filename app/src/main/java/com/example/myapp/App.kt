package com.example.myapp

import android.app.ActivityManager
import android.content.*
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.myapp.bluetooth.BluetoothControl
import com.example.myapp.bluetooth.TransferController
import com.example.myapp.model.common.util.OkHttpEngine
import com.example.myapp.model.common.util.http.Http
import com.example.myapp.model.entity.SysUser
import com.example.myapp.model.eventbus.StepValueChange
import com.example.myapp.serives.StepService
import com.youth.xframe.BuildConfig
import com.youth.xframe.XFrame
import com.youth.xframe.base.XApplication
import com.youth.xframe.utils.XDateUtils
import com.youth.xframe.utils.XPreferencesUtils
import com.youth.xframe.widget.XToast
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*

class App : XApplication(), ViewModelStoreOwner, Handler.Callback {
    private var Tag = "AppDebug"
    private var bluetoothControl: BluetoothControl? = null
    private var transCtrl: TransferController = TransferController()
    private val mGetReplyMessenger = Messenger(Handler(this))
    private var messenger: Messenger? = null

    /**
     * 定时任务
     */
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null

    override fun onCreate() {
        super.onCreate()
        XFrame.initXLog().isDebug = BuildConfig.DEBUG
        Http.init(OkHttpEngine())
        transCtrl.setListener { array ->
            run {
                if (array != null) {
                    val ir = array[0] / 1000.0
                    Toast.makeText(this, ir.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("BluetoothDebug", "data == $ir")
                }
            }
        }
    }

    var isInited = false
    public fun initStepSensor() {
        if (isInited)
        {
            return
        }
        isInited = true
        Log.d(Tag,"initStepSensor")

        startStepService()

        try {
            bluetoothControl = BluetoothControl()
        } catch (e: IOException) {
            Log.e(TAG, "Bluetooth init error: $e")
            Toast.makeText(this, "蓝牙初始化错误，请检查是否打开蓝牙！", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        Thread(Runnable {
            Looper.prepare()
            while (true) {
                var bytes: ByteArray? = null
                if (bluetoothControl == null) {
                    Log.e(TAG, "Bluetooth error !!!")
                    XToast.error( "蓝牙错误!", Toast.LENGTH_LONG)
                    return@Runnable
                }
                try {
                    bytes = bluetoothControl!!.readBytes()
                    if (bytes == null) continue
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "read error!")
                }
                if (bytes != null && bytes.size != 0) {
                    //Log.d(TAG, "read == " + Arrays.toString(bytes));
                    val len = bytes.size
                    //Log.d(TAG,"len == " + len);
                    for (i in 0 until len) {
                        //Log.d(TAG, String.format("0X%02X,", bytes[i]));
                        transCtrl.push_back(bytes[i].toInt())
                    }
                }
            }
        }).start()
    }

    public fun startStepService() {
        val intent = Intent(this, StepService::class.java)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private val conn = object : ServiceConnection {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            timerTask = object : TimerTask() {
                override fun run() {
                    try {
                        messenger = Messenger(service)
                        val msg = Message.obtain(null, StepService.ConstantData.MSG_FROM_CLIENT)
                        msg.replyTo = mGetReplyMessenger
                        messenger!!.send(msg)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }
            timer = Timer()
            timer!!.schedule(timerTask, 0, 500)
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            //这里用来获取到Service发来的数据
            StepService.ConstantData.MSG_FROM_SERVER ->
            {
                //记录运动步数
                var steps = msg.data.getInt("steps")
                Log.d("Step", "step values == $steps")
                if (!XDateUtils.isSameDay(
                        XPreferencesUtils.get(
                            getString(R.string.key_health_step_value_update_time),
                            0L
                        ) as Long
                    )
                ) {
                    steps = 0
                    XPreferencesUtils.put(
                        getString(R.string.key_health_step_value_update_time),
                        System.currentTimeMillis()
                    )
                }
                EventBus.getDefault().post(StepValueChange(steps))
                XPreferencesUtils.put(getString(R.string.key_health_step_value), steps)
            }
        }
        return false
    }

    private fun shouldInit(): Boolean {
        val am =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos =
            am.runningAppProcesses
        val mainProcessName = applicationInfo.processName
        val myPid = Process.myPid()
        for (info in processInfos) {
            if (info.pid == myPid && mainProcessName == info.processName) {
                return true
            }
        }
        return false
    }

    companion object {
        const val APP_ID = "2882303761518395774"
        const val APP_KEY = "5981839552774"
        const val APP_SECRET = "mm9vdHCYJLGfRHETrpwuvg=="
        const val TAG = "AppDebug"

        @JvmStatic
        lateinit var user: SysUser

        /**
         * 文字复制到剪切板
         *
         * @param context context
         * @param text    要复制的文字
         */
        fun copyToClipboard(context: Context, text: String?) {
            val systemService =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            systemService.setPrimaryClip(ClipData.newPlainText("text", text))
            XToast.success(String.format(XFrame.getString(R.string.copy_to_clipboard), text))
        }

        /**
         * 获取状态栏高度
         *
         * @param activity activity
         * @return 高度
         */
        fun getStatusHeight(activity: AppCompatActivity): Int {
            var statusHeight: Int
            val rect = Rect()
            activity.window.decorView
                .getWindowVisibleDisplayFrame(rect)
            statusHeight = rect.top
            if (0 == statusHeight) {
                val localClass: Class<*>
                try {
                    localClass = Class.forName("com.android.internal.R\$dimen")
                    val `object` = localClass.newInstance()
                    val height = localClass
                        .getField("status_bar_height")[`object`]
                        .toString().toInt()
                    statusHeight = activity.resources
                        .getDimensionPixelSize(height)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return statusHeight
        }
    }

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }

    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
    }
}