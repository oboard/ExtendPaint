package oboard.ep

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity(), Runnable {

    private var mTargetDirection: Float = 0.0f
    private var mStopDrawing: Boolean = false
    private var accelerometerValues = FloatArray(3)
    private var magneticFieldValues = FloatArray(3)
    private var gyroscopeValues = FloatArray(3)
    private lateinit var mSensorManager: SensorManager
    private lateinit var mGyroscopeSensor: Sensor
    private lateinit var mMagneticSensor: Sensor
    private lateinit var mOrientationSensor: Sensor

    //    private lateinit var mOrientationListener: MySensorEventListener
    private lateinit var mGyroscopeListener: MySensorEventListener

    //    private lateinit var mMagneticListener: MySensorEventListener
    private val mHandler = Handler()
    private lateinit var doodleView: PaintView
    private var valueAnimator: ValueAnimator? = null

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

        //全屏显示
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val lp = window.attributes
        //下面图1
        //下面图1
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
        doodleView.mTransX += gyroscopeValues[1] * 20
        doodleView.mTransY += gyroscopeValues[0] * 20
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
            Log.e("TAG", "安全区域距离屏幕左边的距离 SafeInsetLeft:" + displayCutout.safeInsetLeft)
            Log.e("TAG", "安全区域距离屏幕右部的距离 SafeInsetRight:" + displayCutout.safeInsetRight)
            Log.e("TAG", "安全区域距离屏幕顶部的距离 SafeInsetTop:" + displayCutout.safeInsetTop)
            Log.e("TAG", "安全区域距离屏幕底部的距离 SafeInsetBottom:" + displayCutout.safeInsetBottom)
            val rects: List<Rect>? = displayCutout.boundingRects
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