package com.example.stopsmsspam.ShortMessageService

import android.database.Cursor
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast

//Todo : find & use the proper data struct provided by android
data class Sms (
    val address: String,
    val body: String,
    val creator: String,
    val date: String,
    val date_sent: String,
    val error_code: String,
    val locked: String,
    val message_type_all: Int,
    val message_type_draft: Int,
    val message_type_failed: Int,
    val message_type_inbox: Int,
    val message_type_outbox: Int,
    val message_type_queued: Int,
    val message_type_sent: Int,
    val person: String,
    val Protocol: String,
    val read: String,
    val reply_path_present: String,
    val seen: String,
    val service_center: String,
    val status: String,
    val status_complete: Int,
    val status_failed: Int,
    val status_none: Int,
    val status_pending: Int,
    val subject: String,
    val subscription_id: String,
    val Thread_id: String,
    val type: String) {}

class SmsManager{
    companion object {
        fun parseToSms(cursor: Cursor?): List<Sms> {
            val messages = arrayListOf<Sms>()
            cursor.use {
                if (it?.moveToFirst() != null) {
                    do {
                        messages.add(createSms(it))
                    } while (it.moveToNext())
                }
            }

            return messages
        }

        fun createSms(it: Cursor): Sms {
            return Sms(
                it.getString(2) ?: "",
                it.getString(12) ?: "",
                it.getString(18) ?: "",
                it.getString(4) ?: "",
                it.getString(5) ?: "",
                it.getString(17) ?: "",
                it.getString(14) ?: "",
                it.getString(9).toInt(),
                it.getString(9).toInt(),
                it.getString(9).toInt(),
                it.getString(9).toInt(),
                it.getString(9).toInt(),
                it.getString(9).toInt(),
                it.getString(9).toInt(),
                it.getString(3) ?: "",
                it.getString(6) ?: "",
                it.getString(7) ?: "",
                it.getString(10) ?: "",
                it.getString(19) ?: "",
                it.getString(13) ?: "",
                it.getString(8) ?: "",
                it.getString(8).toInt(),
                it.getString(8).toInt(),
                it.getString(8).toInt(),
                it.getString(8).toInt(),
                it.getString(11) ?: "",
                it.getString(15) ?: "",
                it.getString(1) ?: "",
                it.getString(9) ?: ""
            )
        }

        //TODO: This code should handle multi sim.
        fun sendStopSms(to: String){
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(to, null, "STOP", null, null)
        }
    }
}

enum class StrategySpam{
    LongNumber,
    ShortNumber,
    NoNumberCompanyName,
    None
}