package com.omela.myapplication3.views

import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.scaleMatrix
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.omela.myapplication3.R
import kotlin.math.*

class DayView(context: Context) : View(context) {

    private var cdt: CountDownTimer? = null

    private val mPaint = Paint()
    private val mRect = Rect()
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var progress: Float = 0f
    private var isReversedCycle = false
    private var isDayTime = true
    private var direction = DayDirection.FORWARD

    private val skyDayTimeColor by lazy { getColor(context, R.color.skyDayTimeColor) }
    private val skyNightTimeColor by lazy { getColor(context, R.color.skyNightTimeColor) }
    private val grassDayColor by lazy { getColor(context, R.color.grassDayColor) }
    private val grassNightColor by lazy { getColor(context, R.color.grassNightColor) }
    private val sunColor by lazy { getColor(context, R.color.sunColor) }
    private val moonColor by lazy { getColor(context, R.color.moonColor) }

    // CelestialBody
    private val cbPathRadius by lazy { (min(width, height) / 2) - DEF_HORIZONTAL_X }
    private var cacheAngle: Double = 0.0

    init {
        setOnClickListener { reverseDirection() }
        postDelayed({ startTimer(cycleTime = getCycleTime()) }, 3000L)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSky(canvas)
        drawCelestialBody(canvas)
        drawGrass(canvas)
    }

    private fun reverseDirection() {
        cdt?.cancel()
        direction = direction.reversed()
        cacheAngle = getCBAngle(1 - progress)
        isReversedCycle = true
        startTimer(getCycleTime(percent = progress))
    }

    private fun drawCelestialBody(canvas: Canvas) {
        val cbAngle = getCBAngle(progress)
        val centerX = width / 2
        val cbX: Float = centerX + (cbPathRadius * cos(cbAngle * direction.value)).toFloat()
        val centerY = height / 2
        val cbY: Float = centerY + (cbPathRadius * sin(cbAngle * direction.value)).toFloat()

        mPaint.style = Paint.Style.FILL
        mPaint.color = if (isDayTime) sunColor else moonColor
        canvas.drawCircle(cbX, cbY, CB_RADIUS, mPaint)

        gradientPaint.shader = RadialGradient(
            cbX,
            cbY,
            CB_GRADIENT_RADIUS,
            if (isDayTime) sunColor else moonColor,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cbX, cbY, CB_GRADIENT_RADIUS, gradientPaint)
    }

    private fun startTimer(cycleTime: Long) {
        cdt?.cancel()
        cdt = object : CountDownTimer(cycleTime, INTERVAL) {
            override fun onTick(time: Long) {
                progress = 1f - (time.toFloat() / cycleTime)
                postInvalidateOnAnimation()
            }
            override fun onFinish() = nextDayTime()
        }
        cdt?.start()
    }

    private fun nextDayTime() {
        isDayTime = !isDayTime
        isReversedCycle = false
        startTimer(getCycleTime())
    }

    private fun getCBAngle(progress: Float): Double {
        return when {
            isReversedCycle -> cacheAngle
            direction == DayDirection.FORWARD -> CB_START_FORWARD_IN_DEGREE.radian
            else -> CB_START_BACK_IN_DEGREE.radian
        }
            .let { it - (HARDCODED_ARC_DEGREES.radian * progress) }
    }

    private fun getCycleTime(percent: Float = 1f): Long {
        return when {
            isDayTime -> DAY_IN_MS * DAY_TIME_RATIO
            else -> DAY_IN_MS * (FULL_DAY - DAY_TIME_RATIO)
        }
            .toLong()
            .let { (it * percent).roundToLong() }
    }

    private fun drawSky(canvas: Canvas) {
        colorPaintByDayTime(skyDayTimeColor, skyNightTimeColor)
        mPaint.style = Paint.Style.FILL
        mRect.set(0, 0, width, (height / 2))
        canvas.drawRect(mRect, mPaint)
    }

    private fun drawGrass(canvas: Canvas) {
        val centerY = height / 2

        colorPaintByDayTime(grassDayColor, grassNightColor)
        mPaint.style = Paint.Style.FILL
        mRect.set(0, centerY, width, height)
        canvas.drawRect(mRect, mPaint)

        mPaint.color = Color.DKGRAY
        mPaint.strokeWidth = 1f
        canvas.drawLine(0f, centerY.toFloat(), width.toFloat(), centerY.toFloat(), mPaint)
    }

    private fun colorPaintByDayTime(dayColor: Int, nightColor: Int) {
        when {
            isDayTime && progress in 0f..0.4f -> {
                val percent = (progress * 100) / (0.4f * 100)
                val color = ArgbEvaluatorCompat.getInstance().evaluate(percent, nightColor, dayColor)
                mPaint.color = color
            }
            isDayTime && progress in 0.6f..1f -> {
                val percent = ((progress - 0.6f) * 100) / (0.4f * 100)
                val color = ArgbEvaluatorCompat.getInstance().evaluate(percent, dayColor, nightColor)
                mPaint.color = color
            }
            isDayTime && progress in 0.4f..0.6f -> {
                mPaint.color = dayColor
            }
            else -> {
                mPaint.color = nightColor
            }
        }
    }

    companion object {
        private const val HARDCODED_ARC_DEGREES = 210
        private const val DEF_HORIZONTAL_X = 150f
        private const val CB_START_FORWARD_IN_DEGREE = 195
        private const val CB_START_BACK_IN_DEGREE = 15
        private const val CB_RADIUS = 100f
        private const val CB_GRADIENT_RADIUS = CB_RADIUS * 1.5f
        private const val INTERVAL = 16L
        private const val DAY_IN_MS = 5_000L
        private const val DAY_TIME_RATIO = 0.7f
        private const val FULL_DAY = 1f
    }
}