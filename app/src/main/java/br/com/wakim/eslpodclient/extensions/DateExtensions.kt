package br.com.wakim.eslpodclient.extensions

fun String.dayOfWeek() : Int =
    when (toUpperCase()) {
        "SUNDAY" -> 0
        "MONDAY" -> 1
        "TUESDAY" -> 2
        "WEDNESDAY" -> 3
        "THURSDAY" -> 4
        "FRIDAY" -> 5
        "SATURDAY" -> 6
        else -> -1
    }

fun String.monthOfYear() : Int =
        when (toUpperCase()) {
            "JANUARY" -> 0
            "FEBRUARY" -> 1
            "MARCH" -> 2
            "APRIL" -> 3
            "MAY" -> 4
            "JUNE" -> 5
            "JULY" -> 6
            "AUGUST" -> 7
            "SEPTEMBER" -> 8
            "OCTOBER" -> 9
            "NOVEMBER" -> 10
            "DECEMBER" -> 11
            else -> -1
        }