package me.aluceps.practicedraw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*

/**
 * SurfaceView で実装した View の上にコントロールなどの
 * View が配置できないことがわかったので gist を参考に作成
 * https://gist.github.com/johobemax/547660
 */
class PictView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnTouchListener {

    private var path: Path? = null

    private val paint by lazy {
        Paint().setup()
    }

    private val background by lazy {
        Paint().apply {
            color = Color.WHITE
        }
    }

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private var currentColor = ColorPallet.Black
    private var currentStrokeRatio = 0f

    data class DrewInfo(val path: Path, val color: ColorPallet, val ratio: Float)

    private val undoStack = ArrayDeque<DrewInfo>()
    private val redoStack = ArrayDeque<DrewInfo>()

    private var isClear = false

    init {
        setOnTouchListener(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        logWrite("onSizeChanged")
        setup(w, h)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                path = Path().apply { moveTo(event.x, event.y) }
            }
            MotionEvent.ACTION_MOVE -> {
                path?.lineTo(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                path?.also {
                    it.lineTo(event.x, event.y)
                    if (undoStack.isNotEmpty()) redoStack.clear()
                    undoStack.add(it, currentColor, currentStrokeRatio)
                }
            }
            else -> Unit
        }
        v?.invalidate()
        return true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        logWrite("onDraw")
        isReset {
            canvas?.drawBitmap(bitmap!!, 0f, 0f, background)
            drawHistory(canvas)
            drawLatest(canvas)
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        logWrite("onWindowFocusChanged")
        if (hasWindowFocus) {
            // same as onResume
            setup(width, height)
        } else {
            // same as onPause
            bitmap?.recycle()
            bitmap = null
            canvas = null
        }
    }

    private fun setup(width: Int, height: Int) {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        if (canvas == null) {
            canvas = Canvas(bitmap!!)
        }
    }

    /**
     * undoStack の内容を canvas に再現する
     */
    private fun drawHistory(c: Canvas?) {
        undoStack.toList().forEach { d ->
            Paint().setup().also { p ->
                p.setColor(d.color)
                p.setStrokeRatio(d.ratio)
                c?.drawPath(d.path, p)
            }
        }
    }

    /**
     * 現在の色と太さで path を描画する
     * path は undo の時に null にしている
     */
    private fun drawLatest(c: Canvas?) {
        path?.also {
            setColor(currentColor)
            setStrokeWidth(currentStrokeRatio)
            c?.drawPath(it, paint)
        }
    }

    fun setColor(color: ColorPallet) {
        currentColor = color
        paint.setColor(color)
        logWrite("stroke color: $color")
    }

    fun setStrokeWidth(ratio: Float) {
        currentStrokeRatio = ratio
        paint.setStrokeRatio(ratio)
        logWrite("stroke ratio: $ratio")
    }

    fun reset() {
        isClear = true
        undoStack.clear()
        redoStack.clear()
        postInvalidate()
    }

    /**
     * undo 直後は直前の path が残っているので null にしている
     * 対処しないと undo しても直前の描画が表示されたままになる
     */
    fun undo() {
        if (undoStack.isEmpty()) return
        undoStack.removeLast()?.let {
            redoStack.addLast(it)
        }
        path = null
        postInvalidate()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        redoStack.removeLast()?.let {
            undoStack.addLast(it)
        }
        postInvalidate()
    }

    private fun isReset(drawing: () -> Unit) {
        if (isClear) {
            isClear = false
        } else {
            drawing.invoke()
        }
    }

    private fun logWrite(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("PictView", message)
        }
    }

    private fun Paint.setup(): Paint = apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private fun Paint.setColor(color: ColorPallet) {
        this.color = getColor(color)
    }

    private fun Paint.setStrokeRatio(ratio: Float) {
        this.strokeWidth = BASE_STROKE_WIDTH * (1 + ratio)
    }

    private fun ArrayDeque<DrewInfo>.add(p: Path, c: ColorPallet, r: Float) {
        addLast(DrewInfo(p, c, r))
    }

    @SuppressLint("ResourceType")
    private fun getColor(color: ColorPallet): Int =
        ResourcesCompat.getColor(resources, color.resId, null)

    companion object {
        private const val BASE_STROKE_WIDTH = 16.0f
    }
}