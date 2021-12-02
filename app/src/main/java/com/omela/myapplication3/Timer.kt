package com.omela.myapplication3

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicLong

class CountDownTimer(
    private val countdownInterval: Long,
    private val onTick: (millisUntilFinished: Long) -> Unit,
    private val onFinish: () -> Unit,
) {
    private val stopTimeInFuture: AtomicLong = AtomicLong(0)
    private var isCancelled = false
    private var onHandlerTick: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper()) {
        onHandlerTick?.invoke()
        false
    }

    @Synchronized
    fun cancel() {
        isCancelled = true
        handler.removeMessages(MSG)
        onHandlerTick = null
    }

    @Synchronized
    fun start(millisInFuture: Long) {
        isCancelled = false
        if (millisInFuture <= 0) {
            onTick(0L)
        } else {
            onHandlerTick = ::onHandlerTick
            stopTimeInFuture.set(SystemClock.elapsedRealtime() + millisInFuture)
            handler.sendMessage(handler.obtainMessage(MSG))
        }
    }

    @Synchronized
    private fun onHandlerTick() {
        if (isCancelled) {
            return
        }
        val millisLeft = stopTimeInFuture.get() - SystemClock.elapsedRealtime()
        if (millisLeft <= 0) {
            onTick(0)
            cancel()
        } else {
            val lastTickStart = SystemClock.elapsedRealtime()
            onTick(millisLeft)
            val lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart
            var delay: Long
            if (millisLeft < countdownInterval) {
                delay = millisLeft - lastTickDuration
                if (delay < 0) delay = 0
            } else {
                delay = countdownInterval - lastTickDuration
                while (delay < 0) delay += countdownInterval
            }
            handler.sendMessageDelayed(
                handler.obtainMessage(MSG),
                delay
            )
        }
    }

    companion object {
        private const val MSG = 1
    }
}
