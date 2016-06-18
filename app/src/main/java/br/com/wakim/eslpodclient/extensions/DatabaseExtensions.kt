package br.com.wakim.eslpodclient.extensions

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.RowParser

class LongAnyParser: RowParser<Any> {
    override fun parseRow(columns: Array<Any?>): Any = columns[0] as? Long ?: Any()
}

fun SQLiteDatabase.insertIgnoringConflict(tableName: String, vararg values: Pair<String, Any?>): Long {
    return insertWithOnConflict(tableName, null, values.toContentValues(), SQLiteDatabase.CONFLICT_IGNORE)
}

// Extracted from Anko
fun Pair<String, Any?>.toContentValues(): ContentValues {
    return arrayOf(this).toContentValues()
}

fun Array<out Pair<String, Any?>>.toContentValues(): ContentValues {
    val values = ContentValues()

    for ((key, value) in this) {
        when(value) {
            null -> values.putNull(key)
            is Boolean -> values.put(key, value)
            is Byte -> values.put(key, value)
            is ByteArray -> values.put(key, value)
            is Double -> values.put(key, value)
            is Float -> values.put(key, value)
            is Int -> values.put(key, value)
            is Long -> values.put(key, value)
            is Short -> values.put(key, value)
            is String -> values.put(key, value)
            else -> throw IllegalArgumentException("Non-supported value type: ${value.javaClass.name}")
        }
    }

    return values
}