package oboard.ep

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.main_activity.*
import oboard.ep.ColorPicker.OnColorSelectedListener
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class MainActivity : AppCompatActivity(), Runnable {
    companion object {
        var currentColor: Int = Color.RED
    }

    //    private var mTargetDirection: Float = 0.0f
    private var mStopDrawing: Boolean = false
    private var accelerometerValues = FloatArray(3)
    private var magneticFieldValues = FloatArray(3)
    private var gyroscopeValues = FloatArray(3)
    private lateinit var mSensorManager: SensorManager
    private lateinit var mGyroscopeSensor: Sensor
//    private lateinit var mMagneticSensor: Sensor
//    private lateinit var mOrientationSensor: Sensor

    //    private lateinit var mOrientationListener: MySensorEventListener
    private lateinit var mGyroscopeListener: MySensorEventListener

    //    private lateinit var mMagneticListener: MySensorEventListener
    private val mHandler = Handler()
    private lateinit var doodleView: PaintView
//    private var valueAnimator: ValueAnimator? = null

    override fun run() {
//        if (mDrawPointer != null && !mStopDrawing) {
//            mDrawPointer.updateDirection(360 - mTargetDirection)
        calculateOrientation()
        mHandler.postDelayed(this, 20)
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        getNotchParams()

//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val lp = window.attributes
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.attributes = lp

//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.thelittleprince2)
//        val bitmap: Bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.RGB_565)
        doodleView = PaintView(this)
        container_doodle.addView(
            doodleView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        pay_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            var str = ""
            try {
                str = "alipays://platformapi/startapp?saId=10000007&qrcode=" + URLEncoder.encode(
                    "https://qr.alipay.com/tsx00700h1zpzwoodysyhda",
                    "UTF-8"
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            //str是支付宝或微信链接，直接在微信里面点击链接，也可调起
            val uri = Uri.parse(str)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = uri
            startActivity(intent)
        }

        delete_button.setOnClickListener{
            doodleView.mPathList.clear()
        }


        color_picker.setOnColorSelectedListener(object : OnColorSelectedListener {
            fun set(red: Int, green: Int, blue: Int) {
                var value = "红色：$red\t绿色：$green\t蓝色:$blue"
                val color = Color.rgb(red, green, blue)
                value += """
             
             颜色值：0x${Integer.toHexString(color)}
             """.trimIndent()
                color_value_txt.text = value
                currentColor = color
                color_indicator.setImageDrawable(ColorDrawable(color))
                color_indicator.shape = 2
            }

            override fun onColorSelecting(red: Int, green: Int, blue: Int) {
                set(red, green, blue)
            }

            override fun onColorSelected(red: Int, green: Int, blue: Int) {
                set(red, green, blue)
            }
        })
        val bottomSheetBehavior: BottomSheetBehavior<*> =
            BottomSheetBehavior.from(findViewById<View>(R.id.bottom_sheet))
        //设置默认先隐藏
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                //拖动
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //状态变化
            }
        })
//        menu_button.setOnClickListener {
//            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
//            } else {
//                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
//            }
//        }
//        menu_button.setOnClickListener {
        //根据状态不同显示隐藏

        //设置监听事件

//            change_color_btn.setOnClickListener {
//                val colorArray = arrayOf(
//                    arrayOf(
//                        "#fef5ce", "#fff3cd", "#feeeca", "#fdeac9", "#fee7c7", "#fce3c4",
//                        "#fbddc1", "#fad7c3", "#fad0c2", "#f2ced0", "#e6cad9",
//                        "#d9c7e1", "#d2c3e0", "#cfc6e3", "#cac7e4", "#c9cde8",
//                        "#c7d6ed", "#c7dced", "#c7e3e6", "#d2e9d9", "#deedce",
//                        "#e7f1cf", "#eef4d0", "#f5f7d0"
//                    ), arrayOf(
//                        "#ffeb95", "#fee591", "#fcdf8f", "#fcd68d", "#facd89", "#f9c385",
//                        "#f7b882", "#f5ab86", "#f29a82", "#e599a3", "#ce93b3",
//                        "#b48cbe", "#a588be", "#9d8cc2", "#9491c6", "#919dcf",
//                        "#89abd9", "#85bada", "#86c5ca", "#9fd2b1", "#bada99",
//                        "#cbe198", "#dde899", "#edf099"
//                    ), arrayOf(
//                        "#fee250", "#fed84f", "#fbce4d", "#f9c04c", "#f7b24a", "#f6a347",
//                        "#f39444", "#f07c4d", "#ec614e", "#d95f78", "#b95b90",
//                        "#96549e", "#7c509d", "#6e59a4", "#5c60aa", "#5572b6",
//                        "#3886c8", "#1c99c7", "#0daab1", "#57ba8b", "#90c761",
//                        "#b0d35f", "#ccdd5b", "#e5e756"
//                    ), arrayOf(
//                        "#FDD900", "#FCCC00", "#fabd00", "#f6ab00", "#f39801", "#f18101",
//                        "#ed6d00", "#e94520", "#e60027", "#cf0456", "#a60b73",
//                        "#670775", "#541b86", "#3f2b8e", "#173993", "#0c50a3",
//                        "#0168b7", "#0081ba", "#00959b", "#03a569", "#58b530",
//                        "#90c320", "#b8d201", "#dadf00"
//                    ), arrayOf(
//                        "#DBBC01", "#DAB101", "#D9A501", "#D69400", "#D28300", "#CF7100",
//                        "#CD5F00", "#CA3C18", "#C7001F", "#B4004A", "#900264",
//                        "#670775", "#4A1277", "#142E82", "#0A448E", "#005AA0",
//                        "#0070A2", "#018287", "#02915B", "#4A9D27", "#7DAB17",
//                        "#9EB801", "#BCC200", "#DBBC01"
//                    )
//                )
//                color_picker.setColor(colorArray)
//            }

//        }

        initServices()
    }

    private fun calculateOrientation() {
//        val values = FloatArray(3)
//        val rValues = FloatArray(9)
//        SensorManager.getRotationMatrix(rValues, null, accelerometerValues, magneticFieldValues)
//        SensorManager.getOrientation(rValues, values)
//        values[0] = Math.toDegrees(values[0].toDouble()).toFloat()
//        val direction = -values[0]
//        valueAnimator?.pause()
//        valueAnimator = ValueAnimator.ofFloat(doodleView.rotation, direction)
//        if (abs(doodleView.rotation - direction) > 10) {
//            valueAnimator?.addUpdateListener { animation ->
//                doodleView.rotation = animation.animatedValue as Float
//            }
//            valueAnimator?.duration = 100
//            valueAnimator?.interpolator = LinearInterpolator()
//            valueAnimator?.start()
//        }
        doodleView.mTransX += gyroscopeValues[1] * 40
        doodleView.mTransY += gyroscopeValues[0] * 40
//        doodleView.mRotate = direction
        doodleView.invalidate()
    }


    private fun initServices() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) //加速度传感器
//        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)   //地磁场传感器
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)   //陀螺仪传感器
    }

    override fun onResume() {
        super.onResume()
//        mOrientationListener = MySensorEventListener()
        mGyroscopeListener = MySensorEventListener()
//        mMagneticListener = MySensorEventListener()
//        mSensorManager.registerListener(
//            mOrientationListener,
//            mOrientationSensor,
//            Sensor.TYPE_ACCELEROMETER
//        )
//        mSensorManager.registerListener(
//            mMagneticListener,
//            mMagneticSensor,
//            Sensor.TYPE_MAGNETIC_FIELD
//        )
        mSensorManager.registerListener(
            mGyroscopeListener,
            mGyroscopeSensor,
            Sensor.TYPE_ACCELEROMETER
        )
        mStopDrawing = false
        mHandler.postDelayed(this, 20)
    }

    override fun onPause() {
        super.onPause()
        mStopDrawing = true

//        mSensorManager.unregisterListener(mOrientationListener)
        mSensorManager.unregisterListener(mGyroscopeListener)
//        mSensorManager.unregisterListener(mMagneticListener)
    }

    inner class MySensorEventListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values
            }
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                gyroscopeValues = event.values
            }
        }

    }


    private fun onChange() {
//        val decorView = window.decorView
//        window.navigationBarColor = Color.TRANSPARENT

        //全屏显示
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) onChange()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onChange()
    }


    @TargetApi(28)
    fun getNotchParams() {
        val decorView = window.decorView
        decorView.post(Runnable {
            val rootWindowInsets = decorView.rootWindowInsets
            if (rootWindowInsets == null) {
                Log.e("TAG", "rootWindowInsets为空了")
                return@Runnable
            }
            val displayCutout = rootWindowInsets.displayCutout
            Log.e("TAG", "安全区域距离屏幕左边的距离 SafeInsetLeft:" + displayCutout?.safeInsetLeft)
            Log.e("TAG", "安全区域距离屏幕右部的距离 SafeInsetRight:" + displayCutout?.safeInsetRight)
            Log.e("TAG", "安全区域距离屏幕顶部的距离 SafeInsetTop:" + displayCutout?.safeInsetTop)
            Log.e("TAG", "安全区域距离屏幕底部的距离 SafeInsetBottom:" + displayCutout?.safeInsetBottom)
            val rects: List<Rect>? = displayCutout?.boundingRects
            if (rects == null || rects.isEmpty()) {
                Log.e("TAG", "不是刘海屏")
            } else {
                Log.e("TAG", "刘海屏数量:" + rects.size)
                for (rect in rects) {
                    Log.e("TAG", "刘海屏区域：$rect")
                }
            }
        })
    }

}