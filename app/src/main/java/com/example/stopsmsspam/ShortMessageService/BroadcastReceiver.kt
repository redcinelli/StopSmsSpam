package com.example.stopsmsspam.ShortMessageService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

private const val TAG = "MyBroadcastReceiver"

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("message received !")
    }
}
