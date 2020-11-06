package oboard.ep

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import oboard.ep.doodle.androids.ScaleGestureDetectorApi27
import oboard.ep.doodle.androids.TouchGestureDetector
import java.util.*
import kotlin.math.abs


/**
 * 高级涂鸦
 * 支持对图片涂鸦, 可移动缩放图片
 * Created on 24/06/2018.
 */
@SuppressLint("ViewConstructor")
class PaintView(context: Context?) :
    View(context) {
    private var mPaint = Paint()
    public val mPathList: MutableList<PathItem?> =
        ArrayList<PathItem?>() // 保存涂鸦轨迹的集合
    private val mTouchGestureDetector // 触摸手势监听
            : TouchGestureDetector
    private var mLastX
            : Float = 0.0f
    private var mLastY
            : Float = 0.0f
    private var mCurrentPathItem // 当前的涂鸦轨迹
            : PathItem? = null
    private var mSelectedPathItem // 选中的涂鸦轨迹
            : PathItem? = null
    public var mTransX = 0f
    public var mTransY = 0f
    public var mScale = 1f
    var mLastFocusX: Float? = null
    var mLastFocusY: Float? = null
    var mTouchCentreX = 0f
    var mTouchCentreY = 0f
    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) { //view绘制完成时 大小确定
        super.onSizeChanged(width, height, oldw, oldh)
//        val centerWidth: Float
//        val centerHeight: Float
        // 1.计算使图片居中的缩放值
//        if (nw > nh) {
//            mBitmapScale = 1 / nw
//            centerWidth = getWidth().toFloat()
//            centerHeight = (h * mBitmapScale)
//        } else {
//            mBitmapScale = 1 / nh
//            centerWidth = (w * mBitmapScale)
//            centerHeight = getHeight().toFloat()
//        }
//        // 2.计算使图片居中的偏移值
//        mBitmapTransX = (getWidth() - centerWidth) / 2f
//        mBitmapTransY = (getHeight() - centerHeight) / 2f
        invalidate()
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    fun toX(touchX: Float): Float {
        return (touchX - mTransX) / mScale
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    fun toY(touchY: Float): Float {
        return (touchY - mTransY) / mScale
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val consumed: Boolean = mTouchGestureDetector.onTouchEvent(event) // 由手势识别器处理手势
        return if (!consumed) {
            super.dispatchTouchEvent(event)
        } else true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.scale(mScale, mScale)
//        canvas.translate(mTransX, mTransY)

        // 绘制图片
//        canvas.drawBitmap(mBitmap, 0f, 0f, null)
        for (path in mPathList) { // 绘制涂鸦轨迹
            canvas.save()
            if (path != null) {
                canvas.translate(path.mX + mTransX / mScale, path.mY + mTransY / mScale)
            } // 根据涂鸦轨迹偏移值，偏移画布使其画在对应位置上
            if (mSelectedPathItem === path) {
                mPaint.color = Color.YELLOW // 点中的为黄色
            } else {
                mPaint.color = path!!.mColor
            }
            if (path != null) {
                canvas.drawPath(path.mPath, mPaint)
            }
            canvas.restore()
        }
    }

    /**
     * 封装涂鸦轨迹对象
     */
    class PathItem {
        var mPath = Path() // 涂鸦轨迹
        var mColor = Color.RED;
        var mX = 0f
        var mY = 0f // 轨迹偏移值
    }

    companion object {
        private const val TAG = "AdvancedDoodleView"
    }

    init {

        // 设置画笔
//        mPaint.color = Color.RED
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 20f
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND

        // 由手势识别器处理手势
        mTouchGestureDetector =
            TouchGestureDetector(
                getContext(),
                object : TouchGestureDetector.OnTouchGestureListener(),
                    TouchGestureDetector.IOnTouchGestureListener {
                    var mRectF = RectF()

                    // 缩放手势操作相关
                    override fun onScaleBegin(detector: ScaleGestureDetectorApi27?): Boolean {
                        mLastFocusX = null
                        mLastFocusY = null
                        return true
                    }

                    override fun onScaleEnd(detector: ScaleGestureDetectorApi27?) {
                    }

                    override fun onScale(detector: ScaleGestureDetectorApi27): Boolean { // 双指缩放中
                        // 屏幕上的焦点
                        mTouchCentreX = detector.focusX
                        mTouchCentreY = detector.focusY

                        if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
                            val dx = mTouchCentreX - mLastFocusX!!
                            val dy = mTouchCentreY - mLastFocusY!!

                            mTransX += dx
                            mTransY += dy
                        }

                        if (abs(detector.scaleFactor) < 0.1f) {
                            mScale = 0.1f
                        } else {
                            mScale *= detector.scaleFactor
                        }

                        invalidate()
                        mLastFocusX = mTouchCentreX
                        mLastFocusY = mTouchCentreY
                        return true
                    }

                    override fun onSingleTapUp(e: MotionEvent): Boolean { // 单击选中
                        val x = toX(e.x)
                        val y = toY(e.y)
                        var found = false
                        for (path in mPathList) { // 绘制涂鸦轨迹
                            path?.mPath?.computeBounds(mRectF, true) // 计算涂鸦轨迹的矩形范围
                            mRectF.run {
                                if (path != null) {
                                    offset(path.mX, path.mY)
                                }
                            } // 加上偏移
                            if (mRectF.contains(x, y)) { // 判断是否点中涂鸦轨迹的矩形范围内
                                found = true
                                mSelectedPathItem = if (mSelectedPathItem == path) {
                                    null
                                } else {
                                    path
                                }
                                break
                            }
                        }
                        if (!found) { // 没有点中任何涂鸦
                            mSelectedPathItem = null
                        }
                        invalidate()
                        return true
                    }

                    override fun onScrollBegin(e: MotionEvent) { // 滑动开始
                        val x = toX(e.x)
                        val y = toY(e.y)
                        if (mSelectedPathItem == null) {
                            mCurrentPathItem = PathItem()// 新的涂鸦
                            mCurrentPathItem!!.mColor = MainActivity.currentColor;
                            mPathList.add(mCurrentPathItem) // 添加的集合中
                            mCurrentPathItem!!.mPath.moveTo(x, y)
                        }
                        mLastX = x
                        mLastY = y
                        invalidate() // 刷新
                    }

//                    override fun onLongPress(e: MotionEvent?) {
//                        if (mSelectedPathItem != null) {
//                            mPathList.remove(mSelectedPathItem)
//                            invalidate() // 刷新
//                        }
//                        super.onLongPress(e)
//                    }

                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float
                    ): Boolean { // 滑动中
                        val x = toX(e2.x)
                        val y = toY(e2.y)
                        if (mSelectedPathItem == null) { // 没有选中的涂鸦
                            mCurrentPathItem?.mPath?.quadTo(
                                mLastX,
                                mLastY,
                                (x + mLastX) / 2,
                                (y + mLastY) / 2
                            ) // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
                        } else { // 移动选中的涂鸦
                            mSelectedPathItem!!.mX = mSelectedPathItem!!.mX + x - mLastX
                            mSelectedPathItem!!.mY = mSelectedPathItem!!.mY + y - mLastY
                        }
                        mLastX = x
                        mLastY = y
                        invalidate() // 刷新
                        return true
                    }

                    override fun onScrollEnd(e: MotionEvent) { // 滑动结束
                        val x = toX(e.x)
                        val y = toY(e.y)
                        if (mSelectedPathItem == null) {
                            mCurrentPathItem?.mPath?.quadTo(
                                mLastX,
                                mLastY,
                                (x + mLastX) / 2,
                                (y + mLastY) / 2
                            ) // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
                            mCurrentPathItem = null // 轨迹结束
                        }
                        invalidate() // 刷新
                    }
                })

        // 针对涂鸦的手势参数设置
        // 下面两行绘画场景下应该设置间距为大于等于1，否则设为0双指缩放后抬起其中一个手指仍然可以移动
        mTouchGestureDetector.setScaleSpanSlop(1) // 手势前识别为缩放手势的双指滑动最小距离值
        mTouchGestureDetector.setScaleMinSpan(1) // 缩放过程中识别为缩放手势的双指最小距离值
        mTouchGestureDetector.setIsLongpressEnabled(false)
        mTouchGestureDetector.setIsScrollAfterScaled(false)
    }


}