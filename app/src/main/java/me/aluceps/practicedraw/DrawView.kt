package me.aluceps.practicedraw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.annotation.IdRes
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

class DrawView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private var surfaceHolder: SurfaceHolder
    private var paint: Paint
    private var path: Path

    private var lastDrawBitmap: Bitmap? = null
    private var lastDrawCanvas: Canvas? = null

    data class DrewInfo(val path: Path, val color: ColorPallet, val width: Float)

    private val undoStack = ArrayDeque<DrewInfo>()
    private val redoStack = ArrayDeque<DrewInfo>()

    private var currentColor: ColorPallet? = null
    private var currentWidth: Float = 0f

    val isUndoable
        get() = undoStack.isNotEmpty()

    val isRedoable
        get() = redoStack.isNotEmpty()

    init {
        setZOrderOnTop(true)

        surfaceHolder = holder
        surfaceHolder.let {
            it.addCallback(this)
            it.setFormat(PixelFormat.TRANSPARENT)
        }

        path = Path()
        paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        color(ColorPallet.Black)
        strokeWidth(0f)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        clearLastDrawBitmap(paint.color)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        lastDrawBitmap?.recycle()
        lastDrawBitmap = null
        lastDrawCanvas = null
        undoStack.clear()
        redoStack.clear()
    }

    private fun clearLastDrawBitmap(@IdRes resId: Int) {
        if (lastDrawBitmap == null) {
            lastDrawBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        if (lastDrawCanvas == null) {
            lastDrawBitmap?.let { lastDrawCanvas = Canvas(it) }
        }
        lastDrawCanvas?.drawColor(resId, PorterDuff.Mode.CLEAR)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> touchDown(event.x, event.y)
            MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
            MotionEvent.ACTION_UP -> touchUp(event.x, event.y)
            else -> Unit
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
            drawLine(p, paint)
        }
    }

    private fun touchUp(x: Float, y: Float) {
        path.also { p ->
            p.lineTo(x, y)
            drawLine(p, paint)
            lastDrawCanvas?.drawPath(p, paint)
            undoStack.addLast(DrewInfo(p, currentColor!!, currentWidth))
            if (isUndoable && isRedoable) redoStack.clear()
        }
        Logger.d("touchUp: x=$x y=$y undo=${undoStack.size}")
    }

    private fun redraw(action: ((canvas: Canvas) -> Unit)? = null) {
        surfaceHolder.lockCanvas().apply {
            drawColor(0, PorterDuff.Mode.CLEAR)
            action?.invoke(this)
        }.let { surfaceHolder.unlockCanvasAndPost(it) }
    }

    private fun drawLine(path: Path, paint: Paint) {
        redraw { c ->
            lastDrawBitmap?.let { c.drawBitmap(it, 0f, 0f, null) }
            c.drawPath(path, paint)
        }
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
        clearLastDrawBitmap(paint.color)
        redraw()
        Logger.d("reset: undo=${undoStack.size} redo=${redoStack.size}")
    }

    fun undo() {
        if (!isUndoable) return
        undoStack.removeLast()?.let {
            redoStack.addLast(it)
        }
        redraw { canvas ->
            clearLastDrawBitmap(paint.color)
            undoStack.toList().forEachIndexed { i, d ->
                paint.color = getColor(d.color)
                paint.strokeWidth = d.width
                canvas.drawPath(d.path, paint)
                lastDrawCanvas?.drawPath(d.path, paint)
                Logger.d("undo: i=$i color=${d.color}")
            }
        }
        Logger.d("undo: undo=${undoStack.size} redo=${redoStack.size}")
    }

    fun redo() {
        if (!isRedoable) return
        redoStack.removeLast()?.let { d ->
            undoStack.addLast(d)
            paint.color = getColor(d.color)
            paint.strokeWidth = d.width
            drawLine(d.path, paint)
            lastDrawCanvas?.drawPath(d.path, paint)
        }
        Logger.d("redo: undo=${undoStack.size} redo=${redoStack.size}")
    }

    @SuppressLint("ResourceType")
    private fun getColor(color: ColorPallet): Int =
        ResourcesCompat.getColor(resources, color.resId, null)

    fun color(color: ColorPallet) {
        currentColor = color
        paint.color = getColor(color)
        Logger.d("currentColor: $color")
    }

    fun strokeWidth(size: Float) {
        val width = BASE_STROKE_WIDTH * (1 + size)
        currentWidth = width
        paint.strokeWidth = width
        Logger.d("currentWidth: $width")
    }

    companion object {
        private const val BASE_STROKE_WIDTH = 12.0f
    }

    private object Logger {
        private const val TAG = "DrawView"

        fun d(message: String) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, message)
            }
        }
    }
}