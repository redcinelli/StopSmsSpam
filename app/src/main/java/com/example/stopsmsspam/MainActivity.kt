package com.example.stopsmsspam

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.stopsmsspam.Domain.Domain
import com.example.stopsmsspam.ShortMessageService.MyBroadcastReceiver
import com.example.stopsmsspam.ShortMessageService.Sms

// Todo: Clean the whole class, copy pasta from stackoverflow everywhere
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Still needed ?
        createNotificationChannel()

        val br: BroadcastReceiver = MyBroadcastReceiver()


        setContentView(R.layout.activity_main)

        // Todo: I would most probably want a lister on the event of incoming Sms, not a bath function
        val listner = findViewById(R.id.smsListner) as Switch

        listner.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                listner.text  = "Sms Listener: ON"
//                val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
//                filter.priority = 1000
                val filter = IntentFilter()
                filter.addAction("android.provider.Telephony.SMS_RECEIVED")
                filter.priority = 2147483647
                registerReceiver(br, filter)

                println("registering complete")
            } else {
                listner.text  = "Sms Listener: OFF"
                unregisterReceiver(br)
                println("no registered anymore")
            }

        }

        val btn_click_me = findViewById(R.id.btnStart) as Button
// set on-click listener
        btn_click_me.setOnClickListener {
            //Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
            //createNotif()
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
                Domain.Unsuscribe(number)
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

    // Todo : not fully implemented, where do I use it ?
    //  - missing message to justify the need for those permission
    //  - how do I cleverly implement it ? (before every interaction with the Sms ?)
    fun checkPermission(permission: String, messageExplain: String){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(permission),1)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

    }

    // Todo: this function should not be here, it allow access to the database, this is more model like.
    fun readSms() : List<Sms> {
        checkPermission(Manifest.permission.READ_SMS, "")
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
