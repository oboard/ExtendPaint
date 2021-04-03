package oboard.ep

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.min

class CircleDrawable(private val mWidth: Int, private val mHeight: Int) : Drawable() {
    private val mPaint: Paint = Paint()

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(
            (mWidth / 2).toFloat(), (mWidth / 2).toFloat(),
            (mWidth / 2).toFloat(), mPaint
        ) //绘制圆形
    }

    override fun setAlpha(i: Int) {
        mPaint.alpha = i
    }

    public fun setColor(color: Int) {
        mPaint.color = color
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT //设置系统默认，让drawable支持和窗口一样的透明度
    }

    //还需要从重写以下2个方法，返回drawable实际宽高
    override fun getIntrinsicWidth(): Int {
        return mWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mWidth
    }

    init {
        mPaint.isAntiAlias = true
    }
}