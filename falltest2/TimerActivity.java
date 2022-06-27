package com.example.falltest2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerActivity extends AppCompatActivity {

    TextView timerText;
    Button cancelBtn;

    private SQLiteDatabase sql;
    String[] projection = {
            ContactContract.COLUMN_CONTACT
    };
    String phoneNum = "7002050329";
    String textMsg;
    String latitude;
    String longitude;
    private String prevNumber;
    private int sendCount=0;
    SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_timer);

        Intent intent= new Intent(this, IService2.class);
        stopService(intent);

        Log.d("Chk", "Passed");

        timerText = findViewById(R.id.timerText1);
        cancelBtn = findViewById(R.id.cancelBtn);
        long duration = TimeUnit.SECONDS.toMillis(31);

        CountDownTimer cdt = new CountDownTimer(duration, 1000) {

            @Override
            public void onTick(long l) {

                String sDuration = String.format(Locale.ENGLISH, "%02d",TimeUnit.MILLISECONDS.toSeconds(l));
                timerText.setText(sDuration);
                Log.d("Chk", "Passed");
            }

            @Override
            public void onFinish() {

                timerText.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),
                        "Countdown Ended, Sending SMS", Toast.LENGTH_SHORT).show();

                DBHelper dpHelper = new DBHelper(TimerActivity.this);
                sql = dpHelper.getReadableDatabase();

                Bundle coordinates = getIntent().getExtras();
                if(coordinates != null) {
                    latitude = coordinates.getString("latitude");
                    longitude = coordinates.getString("longitude");
                }

                Cursor cursor = sql.query(ContactContract.TABLE_NAME, projection, null, null, null, null, null);
                List itemIds = new ArrayList<>();
                while (cursor.moveToNext()) {
                    long itemId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(ContactContract.COLUMN_CONTACT));
                    itemIds.add(itemId);
                }
                Log.d("chk2", String.valueOf(itemIds));
                cursor.close();
                Iterator it = itemIds.iterator();

                while (it.hasNext()) {
//                        if (sendCount < 5) {
//                                textMsg = "Sensed Danger here => "+"http://maps.google.com/?q=<"+latitude+">,<"+longitude+">";

                    phoneNum = it.next().toString();
                    if (phoneNum != prevNumber && phoneNum != null) {
                        //https://www.google.com/maps/search/?api=1&query=37.4219983, -122.084
                        //textMsg = "Sensed Danger here => " + "http://maps.google.com/?q=<" + String.valueOf(latitude) + ">,<" + String.valueOf(longitude) + ">";
                        textMsg = "Sensed Danger here => " + "https://www.google.com/maps/search/?api=1&query=" + latitude+ "," + longitude;
                        Log.d("Sending-MSG", "onSensorChanged: " + sendCount);
                        smsManager.sendTextMessage(phoneNum, null, textMsg, null, null);
                        prevNumber = phoneNum;
                        sendCount++;
                    }

                }
                Intent mainscrn= new Intent(getApplicationContext(), MainActivity.class);
                mainscrn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainscrn);

            }
        }.start();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cdt.cancel();
                Intent intent= new Intent(getApplicationContext(), IService2.class);
                stopService(intent);
                Intent mainscrn= new Intent(getApplicationContext(), MainActivity.class);
                mainscrn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainscrn);
            }
        });
    }
}