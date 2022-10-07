package us.spaceclouds42.playtime_tracker.extension

import  kotlin.time.Duration
import kotlin.time.toKotlinDuration

fun java.time.Duration.prettyPrint(): String = toKotlinDuration().prettyPrint()

fun Duration.prettyPrint(): String {
    val prettyString = toComponents { days, hours, minutes, _, _ ->
        buildString {
            if (days == 1L)
                append("1 day ")
            else if (days > 1)
                append("$days days ")
            
            if (hours == 1)
                append("1 hour ")
            else if (hours > 1)
                append("$hours hours ")
            
            if (minutes == 1)
                append("1 minute")
            else if (minutes > 1)
                append("$minutes minutes")
        }
    }
    
    return prettyString.ifEmpty {
        "less than 1 minute"
    }.trim()
}
