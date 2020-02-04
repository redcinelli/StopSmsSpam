package com.example.stopsmsspam

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.stopsmsspam.Domain.Domain
import com.example.stopsmsspam.ShortMessageService.Sms
import com.example.stopsmsspam.ShortMessageService.SmsManager
import com.example.stopsmsspam.Utils.Security.PermissionHelper


class SimInfo(val id_: Int, val display_name: String, val icc_id: String, val slot: Int) {

    override fun toString(): String {
        return "SimInfo{" +
                "id_=" + id_ +
                ", display_name='" + display_name + '\'' +
                ", icc_id='" + icc_id + '\'' +
                ", slot=" + slot +
                '}'
    }

}
// Todo: Clean the whole class, copy pasta from stackoverflow everywhere
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Still needed ?
        createNotificationChannel()

        setContentView(R.layout.activity_main)

        // checkPermission(Manifest.permission.READ_PHONE_STATE, "")

        // Todo: I would most probably want a lister on the event of incoming Sms, not a bath function
        val btn_click_me = findViewById(R.id.btnStart) as Button
        btn_click_me.setOnClickListener {
            mainFunc()
        }
    }

    fun mainFunc(){
        val spams = Domain.findSpamSms(readSms())
        val strat = Domain.findStrategyToReply(spams)
        println(strat.count())
        strat.forEach{
            //println(it.first.name + " : " +it.second.address + " : " + it.second.body + " :: " + Domain.extractAddressToReply(it.second, it.first))
            val number = Domain.extractAddressToReply(it.second, it.first)
            if (!Domain.alreadyUnsuscribed(readSms(), it.second.Thread_id) && number != "")
                //Todo: do not send sms twice, find out how to get the information form the sms received !
                Domain.Unsuscribe(SmsManager.GetSmsManagerByNumber(this, this, "+33620942284"), number)
                Domain.Unsuscribe(SmsManager.GetSmsManagerByNumber(this, this, "+33769484978"), number)
                println("Unsuscribe from : "+ number + "::" + it.second.address)
        }
    }

    // Todo : delete it, I should first define a work flow for my app
    fun createNotif(){
        // Create an explicit intent for an Activity in your app

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        var builder = NotificationCompat.Builder(this, 1.toString())
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Some title")
            .setContentText("Did it work ?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }


    // Todo: this function should not be here, it allow access to the database, this is more model like.
    fun readSms() : List<Sms> {

        PermissionHelper.checkPermission(this, Manifest.permission.READ_SMS, "")
        this.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null).use {
            return com.example.stopsmsspam.ShortMessageService.SmsManager.parseToSms(it)
        }
    }

    // Todo : delete it
    fun findStop(messges: List<Sms>): List<Sms> {
        return messges.filter { it.body.contains("STOP") }
    }

    // Todo : copy pasta not fully undertood, but needed to create
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(1.toString(), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}
