package com.suraj.simplenotes.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.suraj.simplenotes.models.Note
import java.util.*


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "NoteDatabase"
        private const val TABLE_NAME = "NoteTable"
        const val COLUMN_ID = "id"
        const val COLUMN_NOTE = "note"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NOTE + " TEXT,"
                + COLUMN_EMAIL + " TEXT," + COLUMN_TIMESTAMP + "  DATETIME DEFAULT CURRENT_TIMESTAMP" + ")")

        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertNote(note: String?, email: String?): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NOTE, note)
        values.put(COLUMN_EMAIL, email)
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getNote(id: Long): Note? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_NOTE, COLUMN_EMAIL, COLUMN_TIMESTAMP), "$COLUMN_ID=?", arrayOf(id.toString()), null, null, null, null)
        cursor?.moveToFirst()
        val note = Note(
                cursor!!.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)))
        cursor.close()
        return note
    }


    fun getNotesCount(): Int {
        val countQuery = "SELECT  * FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    fun updateNote(note: Note): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NOTE, note.note)

        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(java.lang.String.valueOf(note.id)))
    }

    fun deleteNote(note: Note) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(java.lang.String.valueOf(note.id)))
        db.close()
    }

    fun readAllNotes(email: String?): ArrayList<Note> {
        val users = ArrayList<Note>()
        val db = writableDatabase
        var cursor: Cursor? = null
        cursor = db.rawQuery("select * from $TABLE_NAME where $COLUMN_EMAIL = ?", arrayOf(email))

        if (cursor!!.moveToFirst()) {
            while (!cursor.isAfterLast) {
                var id = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
                var note = cursor.getString(cursor.getColumnIndex(COLUMN_NOTE))
                var email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL))
                var time = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))

                users.add(Note(id.toInt(), note, email, time))
                cursor.moveToNext()
            }
        }
        return users
    }


}