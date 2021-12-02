package com.omela.myapplication3.views

import kotlin.math.PI

val Int.radian get() = this / (180 / PI)

fun DayDirection.reversed() = when (this) {
    DayDirection.FORWARD -> DayDirection.BACK
    else -> DayDirection.FORWARD
}