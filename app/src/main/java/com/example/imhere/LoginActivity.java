package com.example.imhere;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginButton;

    EditText loginText;
    private EditText passwordText;

    DataBaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);

        loginText = findViewById(R.id.loginText);
        passwordText = findViewById(R.id.passwordText);

        loginButton.setOnClickListener(this);

        dbHelper = new DataBaseHelper(this);
    }

    @Override
    public void onClick(View v) {
        String loginStr = loginText.getText().toString();
        int hashCode = passwordText.getText().toString().hashCode();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("accountTable", null, "login == ?", new String[] { loginStr }, null, null, null);
        if (c == null || c.getCount() == 0) {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_LONG).show();
            return;
        }
        c.moveToFirst();
        int inputHashCode = c.getInt(c.getColumnIndex("password"));
        if (inputHashCode != hashCode){
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_LONG).show();
            return;
        }
        c.close();
        dbHelper.close();
        SharedPreferences sp = getSharedPreferences("autoriz", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("auton", true);
        ed.putBoolean("task", true);
        ed.apply();
        super.finish();
    }

    @Override
    public void onBackPressed(){
    }
}
