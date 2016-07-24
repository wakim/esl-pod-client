package br.com.wakim.eslpodclient.util.extensions

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

fun String.secondsFromHourMinute() : Int {
    val split = split(":")
    return (split[0].toInt() * 60) + split[1].toInt()
}

fun Int.secondsToElapsedTime(): String {
    var seconds = this
    val minutes = seconds / 60

    seconds %= 60

    return (if (minutes < 10) "0" + minutes.toString() else minutes.toString()) + ":" + seconds.toString().padStart(2, '0')
}

fun Int.millisToElapsedTime(): String {
    var seconds = this / 1000
    val minutes = seconds / 60

    seconds %= 60

    return (if (minutes < 10) "0" + minutes.toString() else minutes.toString()) + ":" + seconds.toString().padStart(2, '0')
}