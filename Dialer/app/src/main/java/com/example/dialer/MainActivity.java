package com.example.dialer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // permission codes
    private final int CALL_PHONE_PERMISSION_CODE = 100;
    private final int VOICE_CALL_PERMISSION_CODE = 101;
    private final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 102;
    private final int READ_CONTACTS_PERMISSION_CODE = 103;

    private final String INPUT_NUMBER = "Введите номер";
    private final ArrayList<Contact> contactsList = new ArrayList<>();
    private final RecordWorker recordWorker = new RecordWorker();

    private TextView phone; // text view with phone number
    private ListView contactsListView; // list view with contacts

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "У приложения нет разрешения на чтение списка контактов", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "У приложения нет разрешения на запись аудио", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, VOICE_CALL_PERMISSION_CODE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "У приложения нет разрешения на сохранение файлов", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
        }

        // text view with phone number
        phone = findViewById(R.id.tv_phone);

        // add on click listeners for number buttons
        findViewById(R.id.button0).setOnClickListener(view -> addStringToPhoneNumber("0"));
        findViewById(R.id.button1).setOnClickListener(view -> addStringToPhoneNumber("1"));
        findViewById(R.id.button2).setOnClickListener(view -> addStringToPhoneNumber("2"));
        findViewById(R.id.button3).setOnClickListener(view -> addStringToPhoneNumber("3"));
        findViewById(R.id.button4).setOnClickListener(view -> addStringToPhoneNumber("4"));
        findViewById(R.id.button5).setOnClickListener(view -> addStringToPhoneNumber("5"));
        findViewById(R.id.button6).setOnClickListener(view -> addStringToPhoneNumber("6"));
        findViewById(R.id.button7).setOnClickListener(view -> addStringToPhoneNumber("7"));
        findViewById(R.id.button8).setOnClickListener(view -> addStringToPhoneNumber("8"));
        findViewById(R.id.button9).setOnClickListener(view -> addStringToPhoneNumber("9"));

        // plus button
        // plus is adding only in start of number
        findViewById(R.id.button_plus).setOnClickListener(view -> {
            if (phone.getText().equals(INPUT_NUMBER))
                addStringToPhoneNumber("+");
        });

        // delete button: remove last character, if it's possible
        findViewById(R.id.button_delete).setOnClickListener(view -> {
            final String phoneNumber = phone.getText().toString();

            if (phoneNumber.equals(INPUT_NUMBER))
                return;

            if (phoneNumber.length() == 1) {
                phone.setText(INPUT_NUMBER);
                return;
            }

            // remove last character
            phone.setText(phoneNumber.substring(0, phoneNumber.length() - 1));
        });

        // call button
        findViewById(R.id.button_call).setOnClickListener(view -> makePhoneCall());

        // add contacts to list view
        contactsListView = findViewById(R.id.lv_contacts);
        getContactsList();

        // buttons for record and play
        findViewById(R.id.button_record_start).setOnClickListener(view -> recordWorker.recordStart());
        findViewById(R.id.button_record_stop).setOnClickListener(view -> recordWorker.recordStop());
        findViewById(R.id.button_player_start).setOnClickListener(view -> recordWorker.playStart());
        findViewById(R.id.button_player_stop).setOnClickListener(view -> recordWorker.playStop());
    }

    /**
     * This function add string to text view with phone number
     *
     * @param addStr
     */
    @SuppressLint("SetTextI18n")
    private void addStringToPhoneNumber(String addStr) {
        String newNumber = phone.getText().toString();
        if (newNumber.equals(INPUT_NUMBER))
            newNumber = "";
        phone.setText(newNumber + addStr);
    }

    /**
     * Function to get contacts from phone memory and place them to list view
     */
    private void getContactsList() {
        String[] PROJECTION = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            try {
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name, number;
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex);
                    number = cursor.getString(numberIndex);
                    contactsList.add(new Contact(name, number));
                }
            } finally {
                cursor.close();
            }

            ArrayAdapter<Contact> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
            contactsListView.setAdapter(adapter);

            contactsListView.setOnItemClickListener((parent, v, position, id) -> {
                Contact selectedItem = contactsList.get(position); // get clicked item
                phone.setText(selectedItem.getNumber()); // set selected number to text view
            });
        }
    }

    /**
     * Make phone call
     */
    private void makePhoneCall() {
        // cannot call without permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "У приложения нет разрешений", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_PERMISSION_CODE);
            }
            return;
        }

        String phoneNumber = phone.getText().toString();
        if (phoneNumber.equals(INPUT_NUMBER))
            return;

        phoneNumber = "tel:" + phoneNumber;

        recordWorker.recordStart(); // start recording

        // start call
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber)));
    }

    /**
     * Function that launches on close app
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordWorker.releasePlayer();
        recordWorker.releaseRecorder();
    }
}