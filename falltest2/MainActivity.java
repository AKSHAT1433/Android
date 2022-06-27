package com.example.falltest2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_FINE = 2;

    private Button start,stop,addContacts;
    ListView lv;
    EditText edit;
    private SQLiteDatabase sql;
    String provider;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Before Permission Check", "onCreate: ");

        //SMS and GPS Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS};
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//            return;
        }
        Log.d("After Permission Check", "onCreate:");

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        lv = (ListView) findViewById(R.id.contacts);
        edit = (EditText) findViewById(R.id.editText);
        addContacts = (Button) findViewById(R.id.add);

        DBHelper dpHelper = new DBHelper(this);
        sql = dpHelper.getWritableDatabase();
        Cursor cursor = getAllContacts();
        final ArrayAdapter<String> arrayAdapter;

        Toast.makeText(getApplicationContext(),"Wear Your Helmet and Start Tracking",Toast.LENGTH_SHORT).show();


        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d("Start Button", "Pressed");
                String count = "SELECT count(*) FROM "+ContactContract.TABLE_NAME;
                Cursor mcursor = sql.rawQuery(count, null);
                mcursor.moveToFirst();
                int icount = mcursor.getInt(0);
                if(icount>0){
                    Toast.makeText(getApplicationContext(),"Safe riding! We track you for safety",Toast.LENGTH_SHORT).show();
                    Intent intent= new Intent(getApplicationContext(), IService2.class);
                    startService(intent);
                    //startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Add at least one contact then try again",Toast.LENGTH_SHORT).show();
                }
                mcursor.close();
            }
        });
        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d("Stop Button", "Pressed");
                Intent intent= new Intent(getApplicationContext(), IService2.class);
                stopService(intent);
            }
        });

        ArrayList<String> list = new ArrayList<String>();
        addContacts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ArrayList<String> list = new ArrayList<String>();
                Log.d("Adding Contacts", addContacts.toString());
                //Insert into db
                if(edit.getText().toString().length() != 10 ){
                    Toast.makeText(getApplicationContext(),"Please enter again!",Toast.LENGTH_SHORT).show();
                }else{
                    addNewContact(edit.getText().toString());
                    Toast.makeText(getApplicationContext(),"Contact Added",Toast.LENGTH_SHORT).show();
                    Cursor cursor = getAllContacts();
                    if (cursor.moveToFirst()){
                        do{
                            @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("contact"));
                            list.add(data);
                            // do what ever you want here
                        }while(cursor.moveToNext());
                    }
                    cursor.close();
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simplerow);
                    arrayAdapter.addAll(list);
                    lv.setAdapter(arrayAdapter);
                }
                edit.setText("");
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("long clicked","pos: " + i  + " long value is :"+l);
                //Toast.makeText(getApplicationContext(),lv.getItemAtPosition(i).toString(), Toast.LENGTH_LONG).show();
                removeContact(lv.getItemAtPosition(i).toString());
                //lv.remove(i);
                Object remove = lv.getAdapter().getItem(i);
                ArrayAdapter arrayAdapter1 = (ArrayAdapter)lv.getAdapter();
                arrayAdapter1.remove(remove);
                return false;
            }
        });
        if (cursor.moveToFirst()){
            do{
                @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("contact"));
                list.add(data);
                // do what ever you want here
            }while(cursor.moveToNext());
        }
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simplerow);
        arrayAdapter.addAll(list);
        lv.setAdapter(arrayAdapter);
    }

    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }
    
    public Cursor getAllContacts(){
        return sql.query(ContactContract.TABLE_NAME,null,null,null,null,null,ContactContract.COLUMN_CONTACT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemThatwasSelected = item.getItemId();
        if(menuItemThatwasSelected == R.string.action_contacts){
            Context context = MainActivity.this;
            startActivity(new Intent(this, com.example.falltest2.ContactActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public long addNewContact(String contact){
        ContentValues cv = new ContentValues();
        cv.put(ContactContract.COLUMN_CONTACT,contact);
        return sql.insert(ContactContract.TABLE_NAME,null,cv);
    }

    public void removeContact(String contact){
        sql.delete(ContactContract.TABLE_NAME, "contact"+"=?",new String[]{contact});
        Toast.makeText(getApplicationContext(),"Number Deleted!!",Toast.LENGTH_SHORT).show();
    }
}
