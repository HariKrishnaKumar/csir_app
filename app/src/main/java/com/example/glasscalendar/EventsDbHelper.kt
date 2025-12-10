package com.example.glasscalendar

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EventsDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "events.db"
        const val DATABASE_VERSION = 2

        const val TABLE_EVENTS = "events"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_IS_USER_ADDED = "is_user_added"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_EVENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_START_TIME INTEGER NOT NULL,
                $COLUMN_END_TIME INTEGER NOT NULL,
                $COLUMN_IS_USER_ADDED INTEGER NOT NULL DEFAULT 1
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

    fun insertEvent(event: Event): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, event.title)
            put(COLUMN_DESCRIPTION, event.description)
            put(COLUMN_START_TIME, event.startTimeMillis)
            put(COLUMN_END_TIME, event.endTimeMillis)
            put(COLUMN_IS_USER_ADDED, if (event.isUserAdded) 1 else 0)
        }
        return db.insert(TABLE_EVENTS, null, values)
    }

    fun getAllEvents(): List<Event> {
        val events = mutableListOf<Event>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_EVENTS,
            arrayOf(
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_DESCRIPTION,
                COLUMN_START_TIME,
                COLUMN_END_TIME,
                COLUMN_IS_USER_ADDED
            ),
            null,
            null,
            null,
            null,
            "$COLUMN_START_TIME ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val start = it.getLong(it.getColumnIndexOrThrow(COLUMN_START_TIME))
                val end = it.getLong(it.getColumnIndexOrThrow(COLUMN_END_TIME))
                val isUserAddedInt = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_USER_ADDED))
                val isUserAdded = isUserAddedInt == 1

                events += Event(
                    id = id,
                    title = title,
                    description = description,
                    startTimeMillis = start,
                    endTimeMillis = end,
                    isUserAdded = isUserAdded
                )
            }
        }
        return events
    }

    fun deleteEvent(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_EVENTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}
