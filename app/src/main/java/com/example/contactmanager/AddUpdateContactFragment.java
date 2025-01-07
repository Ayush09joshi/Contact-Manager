package com.example.contactmanager;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class AddUpdateContactFragment extends Fragment {

    private TextInputLayout nameInputLayout, phoneInputLayout;
    private TextInputEditText nameEditText, phoneEditText;
    private Button saveButton;
    private boolean isUpdateMode = false;
    private String contactId; // Used when updating an existing contact

    public static AddUpdateContactFragment newInstance(boolean isUpdate, String id) {
        AddUpdateContactFragment fragment = new AddUpdateContactFragment();
        Bundle args = new Bundle();
        args.putBoolean("isUpdate", isUpdate);
        args.putString("contactId", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_update_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        nameInputLayout = view.findViewById(R.id.name_input_layout);
        phoneInputLayout = view.findViewById(R.id.phone_input_layout);
        nameEditText = view.findViewById(R.id.edit_text_name);
        phoneEditText = view.findViewById(R.id.edit_text_phone);
        saveButton = view.findViewById(R.id.button_save);

        // Check if we are in update mode
        if (getArguments() != null) {
            isUpdateMode = getArguments().getBoolean("isUpdate", false);
            contactId = getArguments().getString("contactId");
        }

        if (isUpdateMode) {
            loadContactDetails(contactId);
            saveButton.setText("Update Contact");
        }

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(getContext(), "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isUpdateMode) {
                updateContact(contactId, name, phone);
            } else {
                addNewContact(name, phone);
            }
        });
    }

    private void loadContactDetails(String contactId) {
        // Fetch contact details to populate fields for update
        Context context = getContext();
        if (context == null || contactId == null) return;

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            nameEditText.setText(name);
            phoneEditText.setText(phone);

            cursor.close();
        }
    }

    private void addNewContact(String name, String phone) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        // Add a new raw contact
        operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Add contact's name
        operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // Add contact's phone number
        operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
            Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack(); // Navigate back to the contact list
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to save contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateContact(String contactId, String name, String phone) {
        Context context = getContext();
        if (context == null || contactId == null) return;

        ContentValues values = new ContentValues();
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);

        int updatedRows = context.getContentResolver().update(
                ContactsContract.Data.CONTENT_URI,
                values,
                ContactsContract.Data.CONTACT_ID + " = ?",
                new String[]{contactId}
        );

        if (updatedRows > 0) {
            Toast.makeText(context, "Contact Updated", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack(); // Navigate back to the contact list
        } else {
            Toast.makeText(context, "Failed to update contact", Toast.LENGTH_SHORT).show();
        }
    }
    private void requestPermissionsIfNeeded() {
        if (getContext() != null && (
                getContext().checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                        getContext().checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{
                    android.Manifest.permission.WRITE_CONTACTS,
                    android.Manifest.permission.READ_CONTACTS
            }, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

}
