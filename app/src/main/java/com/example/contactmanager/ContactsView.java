package com.example.contactmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ContactsView extends Fragment {

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initializing the fragment’s UI.
        View view = inflater.inflate(R.layout.fragment_contacts_view, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
        toolbar.setTitle("Show Contacts");

        FloatingActionButton fab = view.findViewById(R.id.add_contact);
        fab.setOnClickListener(v -> {
            com.example.contactmanager.Add_Update fragment = new com.example.contactmanager.Add_Update();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Initializing the button for asking permission.
        Button requestPermissionButton = view.findViewById(R.id.request_permission_button);
        requestPermissionButton.setOnClickListener(v -> requestPermissions());

        // Check permissions and disable the button if already granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            requestPermissionButton.setEnabled(false);  // Disable the button
            requestPermissionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));  // Change color to grey
            loadContacts();  // Load contacts as permissions are already granted
        } else {
            requestPermissionButton.setEnabled(true);  // Enable the button
        }

        return view;
    }

    private void requestContactsPermission() {
        // Checking if both the permissions are already granted.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            // Loading Contacts if permission are already granted.
            loadContacts();
        } else {
            // Requesting both the permissions if not available.
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
            });
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readGranted = result.getOrDefault(Manifest.permission.READ_CONTACTS, false);
                Boolean writeGranted = result.getOrDefault(Manifest.permission.WRITE_CONTACTS, false);

                if (readGranted != null && writeGranted != null && readGranted && writeGranted) {
                    // Both permissions are already granted
                    loadContacts();
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) &&
                            !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                        // Permissions are denied permanently, redirect to settings
                        Toast.makeText(getContext(), "Please enable permissions from settings.", Toast.LENGTH_LONG).show();
                        openAppSettings();
                    } else {
                        // Permissions denied without selecting "Don't ask again"
                        Toast.makeText(getContext(), "Permissions denied!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS});
        } else {
            loadContacts();
        }
    }

    // Obtaining the list of contacts from the device’s contacts database.
    private void loadContacts() {
        List<com.example.contactmanager.Contact> contactList = fetchContacts();
        com.example.contactmanager.ContactAdaptor adapter = new com.example.contactmanager.ContactAdaptor(contactList);
        recyclerView.setAdapter(adapter);
    }

    // Accessing the contacts stored on the device and prepare them for display.
    private List<com.example.contactmanager.Contact> fetchContacts() {
        List<com.example.contactmanager.Contact> contactList = new ArrayList<>();
        Cursor cursor = requireContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            HashSet<Long> uniqueContacts = new HashSet<>();
            while (cursor.moveToNext()) {
                @SuppressLint("Range") long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (uniqueContacts.add(contactId)) {
                    contactList.add(new com.example.contactmanager.Contact(contactId, name, phoneNumber));
                }
            }
            cursor.close();
        }
        return contactList;
    }
}