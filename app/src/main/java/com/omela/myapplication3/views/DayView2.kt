package com.omela.myapplication3.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.CountDownTimer
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DayView2(context: Context) : View(context) {

    private var cdt: CountDownTimer? = null

    private val mPaint = Paint()
    private val mRect = Rect()

    private val skyDayTimeColor by lazy { Color.rgb(41, 121, 170) }
    private val skyNightTimeColor by lazy { Color.rgb(37, 40, 80) }
    private val grassDayColor by lazy { Color.rgb(234,162,59) }
    private val grassNightColor by lazy { Color.rgb(76, 76, 65) }
    private val sunColor by lazy { Color.rgb(255, 207, 72) }
    private val moonColor by lazy { Color.rgb(252, 254, 218) }

    private var isDayTime = true
    private var direction = Direction.FORWARD

    // CelestialBody
    private val cbPathRadius by lazy { (min(width, height) / 2) - DEF_HORIZONTAL_X }
    private val cbStartAngle: Double = 195 / (180 / PI)
    private var cbAngle: Double = cbStartAngle

    init {
        setOnClickListener { direction = direction.reversed() }
        postDelayed({ startTimer() }, 3000L)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSky(canvas)
        drawCelestialBody(canvas)
        drawWheat(canvas)
    }

    private fun drawCelestialBody(canvas: Canvas) {
        val centerX = width / 2
        val cbX: Float = centerX +
                (cbPathRadius * cos(cbAngle * direction.value))
                    .toFloat()

        val centerY = height / 2
        val cbY: Float = centerY +
                (cbPathRadius * sin(cbAngle * direction.value))
                    .toFloat()

        mPaint.style = Paint.Style.FILL
        mPaint.color = if (isDayTime) sunColor else moonColor

        canvas.drawCircle(cbX, cbY, CB_RADIUS, mPaint)
    }

    private fun startTimer() {
        cdt?.cancel()

        val cycleTime = when {
            isDayTime -> DAY_IN_MS * DAY_TIME_RATIO
            else -> DAY_IN_MS * (FULL_DAY - DAY_TIME_RATIO)
        }.toLong()

        val inc = (220 / (180 / PI)) / (cycleTime / INTERVAL)

        cdt = object : CountDownTimer(cycleTime, INTERVAL) {
            override fun onTick(time: Long) {
                cbAngle -= inc
                invalidate()
            }

            override fun onFinish() = nextDayTime()
        }
        cdt?.start()
    }

    private fun nextDayTime() {
        cbAngle = cbStartAngle
        isDayTime = !isDayTime
        startTimer()
    }

    private fun drawSky(canvas: Canvas) {
        mRect.set(0, 0, width, (height / 2))
        mPaint.style = Paint.Style.FILL
        mPaint.color = if (isDayTime) skyDayTimeColor else skyNightTimeColor
        canvas.drawRect(mRect, mPaint)
    }

    private fun drawWheat(canvas: Canvas) {
        val centerY = height / 2

        mRect.set(0, centerY, width, height)
        mPaint.style = Paint.Style.FILL
        mPaint.color = if (isDayTime) grassDayColor else grassNightColor
        canvas.drawRect(mRect, mPaint)

        mPaint.color = Color.DKGRAY
        mPaint.strokeWidth = 5f
        canvas.drawLine(0f, centerY.toFloat(), width.toFloat(), centerY.toFloat(), mPaint)
    }

    companion object {
//        val l = (PI * CB_RADIUS / 180) * 230

        private const val DEF_HORIZONTAL_X = 150f
        private const val CB_RADIUS = 100f
        private const val INTERVAL = 16L
        private const val DAY_IN_MS = 5_000L
        private const val DAY_TIME_RATIO = 0.6f
        private const val FULL_DAY = 1f

        enum class Direction(val value: Int) {
            FORWARD(-1), BACK(1);
        }

        fun Direction.reversed() = when (this) {
            Direction.FORWARD -> Direction.BACK
            else -> Direction.FORWARD
        }
    }
}