package com.omela.myapplication3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.omela.myapplication3.views.DayView
import com.omela.myapplication3.views.DayView2

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(DayView(this))
    }
}