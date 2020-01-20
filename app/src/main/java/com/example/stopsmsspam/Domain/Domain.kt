package com.example.stopsmsspam.Domain

import com.example.stopsmsspam.ShortMessageService.Sms
import com.example.stopsmsspam.ShortMessageService.SmsManager
import com.example.stopsmsspam.ShortMessageService.StrategySpam

class Domain {
    // Todo: These function should be private and handle by another, to hide "complexity to the user"
    companion object{

        //Todo: This function is bit naive
        fun findSpamSms(messages: List<Sms>) : List<Sms>{
            return messages.filter { it.body.contains("STOP")}
        }

        fun findStrategyToReply(messages: List<Sms>) : List<Pair<StrategySpam, Sms>>{
            val stratPerMessage = arrayListOf<Pair<StrategySpam, Sms>>()

            for (message in messages)
                if (message.address.length > 10)
                    stratPerMessage.add(Pair(StrategySpam.LongNumber, message))

                else if (message.address.length == 5 && message.address[0] == '3')
                    stratPerMessage.add(Pair(StrategySpam.ShortNumber, message))

                else if ("""STOP\s.{0,7}\d{4,5}""".toRegex().containsMatchIn(message.body))
                    stratPerMessage.add(Pair(StrategySpam.NoNumberCompanyName, message))

                else
                    stratPerMessage.add(Pair(StrategySpam.None, message))

            return stratPerMessage
        }

        fun extractAddressToReply(message: Sms, strategy: StrategySpam): String{
            if (strategy == StrategySpam.None)
                return ""
            if (strategy == StrategySpam.LongNumber)
                return message.address
            if (strategy == StrategySpam.ShortNumber)
                return message.address
            if (strategy == StrategySpam.NoNumberCompanyName)
                return extractStopNumberFormText(message.body)

            return ""
        }

        fun extractStopNumberFormText(content: String): String{
            val matchNumberAtTheEnd = """\d{4,5}$""".toRegex()

            return matchNumberAtTheEnd.find(content, 0)?.value ?: ""
        }

        // Todo: I feel this funciton does not work as intented,
        //  - I need more info on how sms are handle
        //  - Does the thread_id really means what it means ?
        //  - Should i create my own database of text so i can avoid going through the all databse of text each time ?
        fun alreadyUnsuscribed(history : List<Sms>, threadId: String): Boolean{
            return history.filter { it.Thread_id == threadId }.any { it.body.trim().toUpperCase() == "STOP"}
        }

        fun Unsuscribe(number: String) {
            //Todo: perform checks on the number ?
            SmsManager.sendStopSms(number)
        }
    }
}