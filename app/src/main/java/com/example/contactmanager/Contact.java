package com.example.contactmanager;

import androidx.appcompat.app.AppCompatActivity;

public class Contact extends AppCompatActivity {
    private final String name;
    private final String phoneNumber;
    private final long contactId;

    public Contact(long contactId, String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getContactId() {
        return contactId;
    }
}
