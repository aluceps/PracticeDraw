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

    data class DrewInfo(val path: Path, val paint: Paint)

    private val undoStack = ArrayDeque<DrewInfo>()
    private val redoStack = ArrayDeque<DrewInfo>()

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
            isAntiAlias = true
        }
        color(ColorPallet.Black)
        strokeWidth(12f)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        clearLastDrawBitmap(paint.color)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        lastDrawBitmap?.recycle()
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
        }
        return true
    }

    private fun touchDown(x: Float, y: Float) {
        Logger.d("touchDown: x=$x y=$y")
        path = Path().apply {
            moveTo(x, y)
        }
    }

    private fun touchMove(x: Float, y: Float) {
        Logger.d("touchMove: x=$x y=$y")
        path.also { p ->
            p.lineTo(x, y)
            drawLine(p, paint)
        }
    }

    private fun touchUp(x: Float, y: Float) {
        Logger.d("touchUp: x=$x y=$y undo=${undoStack.size}")
        path.also { p ->
            p.lineTo(x, y)
            drawLine(p, paint)
            lastDrawCanvas?.drawPath(p, paint)
            undoStack.addLast(DrewInfo(p, paint))
        }
    }

    private fun redraw(action: ((canvas: Canvas) -> Unit)? = null) {
        surfaceHolder.lockCanvas().apply {
            drawColor(0, PorterDuff.Mode.CLEAR)
            action?.invoke(this)
        }.let { surfaceHolder.unlockCanvasAndPost(it) }
    }

    private fun drawLine(path: Path, paint: Paint) {
        Logger.d("drawLine")
        redraw { c ->
            lastDrawBitmap?.let { c.drawBitmap(it, 0f, 0f, null) }
            c.drawPath(path, paint)
        }
    }

    fun reset() {
        Logger.d("reset")
        undoStack.clear()
        redoStack.clear()
        clearLastDrawBitmap(paint.color)
        redraw()
    }

    fun undo() {
        if (!isUndoable) return
        undoStack.removeLast()?.let {
            redoStack.addLast(it)
        }
        clearLastDrawBitmap(paint.color)
        redraw { c ->
            undoStack.forEach { d ->
                c.drawPath(d.path, d.paint)
                lastDrawCanvas?.drawPath(d.path, d.paint)
            }
        }
    }

    fun redo() {
        if (!isRedoable) return
        redoStack.removeLast()?.let {
            undoStack.addLast(it)
            drawLine(it.path, it.paint)
            lastDrawCanvas?.drawPath(it.path, it.paint)
        }
    }

    @SuppressLint("ResourceType")
    fun color(pallet: ColorPallet) {
        Logger.d("color: $pallet")
        paint.color = ResourcesCompat.getColor(resources, pallet.resId, null)
    }

    fun strokeWidth(size: Float) {
        paint.strokeWidth = size
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