package com.example.imhere;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    public DataBaseHelper(Context context) {
        super(context, "imHereDataBase", null, 1);
    }

    String[] login = new String[] { "strontium2001@mail.ru", "elizaveta.ezhova01@mail.ru", "admin"};
    int[] password = new int[] { "Elzahaggard13@".hashCode(), "Elzahaggard13!".hashCode(), "admin".hashCode() };
    int[] status = new int[] { 1, 1, 1 };

    String[] institutions = new String[] { "IRIT-RTF","GUK","TEPLOFAK","VSHEM","STROYFAK","HIMFAK" };
    double[] latitube = new double[] { 56.840751353604674, 56.843914249505055, 56.8424766026804, 56.843110347677786, 56.84516994481896, 56.84218319858506 };
    double[] longitube = new double[] { 60.650839805603034, 60.65374732017518, 60.655345916748054, 60.65316796302796, 60.65071105957032, 60.649048089981086 };

    String[] name = new String[] { "Программирование", "Программирование", "Алгебра и геометрия", "Алгебра и геометрия", "Математический анализ", "Математический анализ", "Английский язык" };
    String[] type = new String[] { "Практика", "Практика", "Лекция", "Практика", "Лекция", "Практика", "Практика" };
    String[] auditory = new String[] { "Р-125", "Р-125", "ГУК-404", "ГУК-404", "И-306", "И-106", "Р-129"};
    String[] lecturer = new String[] {"Чирышев Ю.В.", "Чирышев Ю.В.", "Борич М.А.", "Борич М.А.", "Рыжкова Н.Г.", "Рыжкова Н.Г.", "Чернова О.В."};
    String[] time = new String[] { "8:30", "10:15", "12:00", "14:15", "16:00", "17:45", "19:30" };
    String[] inst = new String[] { "IRIT-RTF", "IRIT-RTF", "GUK", "GUK", "VSHEM", "VSHEM", "IRIT-RTF" };

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table accountTable(login text primary key, password integer, status integer);");
        db.execSQL("create table interviewTable(id integer primary key autoincrement, interview text);");
        db.execSQL("create table institutionTable(institution text primary key, lat real, long real);");
        db.execSQL("create table studentsTable(id text primary key, login text, hashId integer);");
        db.execSQL("create table scheduleTable(id integer primary key autoincrement, name text, type text, auditory text, lecturer text, time text, institution text);");

        for (int i = 0; i < login.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put("login", login[i]);
            cv.put("password", password[i]);
            cv.put("status", status[i]);
            db.insert("accountTable", null, cv);
        }

        for (int i = 0; i < institutions.length; i++){
            ContentValues cv = new ContentValues();
            cv.put("institution", institutions[i]);
            cv.put("lat", latitube[i]);
            cv.put("long", longitube[i]);
            db.insert("institutionTable", null, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
