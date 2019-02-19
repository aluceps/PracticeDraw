package me.aluceps.practicedraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class DrawView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private var surfaceHolder: SurfaceHolder
    private var paint: Paint
    private var path: Path

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    init {
        setZOrderOnTop(true)

        surfaceHolder = holder
        surfaceHolder.let {
            it.addCallback(this)
            it.setFormat(PixelFormat.TRANSPARENT)
        }

        paint = Paint()
        path = Path()

        with(paint) {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            strokeWidth = 10F
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d("DrawView", "surfaceCreated")
        clearLastDrawBitmap()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d("DrawView", "surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d("DrawView", "surfaceDestroyed")
        bitmap?.recycle()
    }

    private fun clearLastDrawBitmap() {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        if (canvas == null) {
            bitmap?.let { canvas = Canvas(it) }
        }
        canvas?.drawColor(paint.color, PorterDuff.Mode.CLEAR)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> touchDown(event.x, event.y)
            MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
            MotionEvent.ACTION_UP -> touchUp(event.x, event.y)
        }
        return true
    }

    private fun touchDown(x: Float, y: Float) {
        path = Path().apply {
            moveTo(x, y)
        }
    }

    private fun touchMove(x: Float, y: Float) {
        path.also { p ->
            p.lineTo(x, y)
            drawLine(p)
        }
    }

    private fun touchUp(x: Float, y: Float) {
        path.also { p ->
            p.lineTo(x, y)
            drawLine(p)
            canvas?.drawPath(p, paint)
        }
    }

    private fun drawLine(path: Path) {
        surfaceHolder.lockCanvas().apply {
            drawColor(0, PorterDuff.Mode.CLEAR)
            bitmap?.let { drawBitmap(it, 0f, 0f, null) }
            drawPath(path, paint)
        }.let { surfaceHolder.unlockCanvasAndPost(it) }
    }

    fun reset() {
        clearLastDrawBitmap()
        surfaceHolder.lockCanvas().apply {
            drawColor(0, PorterDuff.Mode.CLEAR)
        }.let { surfaceHolder.unlockCanvasAndPost(it) }
    }
}