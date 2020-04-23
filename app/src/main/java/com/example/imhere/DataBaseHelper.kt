package com.example.imhere

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//База данных приложения
//Готовы базы данных паролей(если не выйдет с ЛК) и база данных с координатами некоторых институтов
class DataBaseHelper(context: Context?) : SQLiteOpenHelper(context, "imHereDataBase", null, 1) {
    var login = arrayOf("strontium2001@mail.ru", "elizaveta.ezhova01@mail.ru", "admin")
    var password = intArrayOf("Elzahaggard13@".hashCode(), "Elzahaggard13!".hashCode(), "admin".hashCode())
    var status = intArrayOf(1, 1, 1)
    var institutions = arrayOf("IRIT-RTF", "GUK", "TEPLOFAK", "VSHEM", "STROYFAK", "HIMFAK")
    var latitude = doubleArrayOf(56.840751353604674, 56.843914249505055, 56.8424766026804, 56.843110347677786, 56.84516994481896, 56.84218319858506)
    var longitude = doubleArrayOf(60.650839805603034, 60.65374732017518, 60.655345916748054, 60.65316796302796, 60.65071105957032, 60.649048089981086)
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table accountTable(login text primary key, password integer, status integer);")
        db.execSQL("create table interviewTable(id integer primary key autoincrement, interview text);")
        db.execSQL("create table institutionTable(institution text primary key, lat real, long real);")
        db.execSQL("create table studentsTable(id text primary key, login text, hashId integer);")
        db.execSQL("create table scheduleTable(id integer primary key autoincrement, name text, type text, auditory text, lecturer text, time text, institution text);")
        for (i in login.indices) {
            val cv = ContentValues()
            cv.put("login", login[i])
            cv.put("password", password[i])
            cv.put("status", status[i])
            db.insert("accountTable", null, cv)
        }
        for (i in institutions.indices) {
            val cv = ContentValues()
            cv.put("institution", institutions[i])
            cv.put("lat", latitude[i])
            cv.put("long", longitude[i])
            db.insert("institutionTable", null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}