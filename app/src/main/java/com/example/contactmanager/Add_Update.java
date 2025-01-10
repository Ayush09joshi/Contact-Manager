package com.example.contactmanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class Add_Update extends Fragment {
    private TextInputEditText editContactName;
    private TextInputEditText editContactNumber;

    public Add_Update() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_update, container, false);

        editContactName = view.findViewById(R.id.editName);
        editContactNumber = view.findViewById(R.id.editNumber);

        Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
        toolbar.setTitle("Add/Update Contact");
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        Button addContactButton = view.findViewById(R.id.addContactButton);
        Button deleteContactButton = view.findViewById(R.id.deleteContactButton);

        Bundle arguments = getArguments();
        long contactId = -1;
        String contactName = null;
        String contactNumber = null;

        if (arguments != null) {
            contactId = arguments.getLong(String.valueOf(R.string.contact_id_key), -1);
            contactName = arguments.getString(String.valueOf(R.string.contact_name_key));
            contactNumber = arguments.getString(String.valueOf(R.string.contact_number_key));
        }

        if (contactId != -1) {
            // Existing contact: populate fields and setup update and delete actions
            editContactName.setText(contactName);
            editContactNumber.setText(contactNumber);

            final long finalContactId = contactId;
            addContactButton.setOnClickListener(v -> updateContact(finalContactId));
            deleteContactButton.setVisibility(View.VISIBLE);
            deleteContactButton.setOnClickListener(v -> deleteContact(finalContactId));
        } else {
            // New contact: setup add action
            addContactButton.setOnClickListener(v -> addContact());
            deleteContactButton.setVisibility(View.GONE);
        }

        return view;
    }

    private void deleteContact(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        int rowsDeleted = requireContext().getContentResolver().delete(contactUri, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Contact deleted successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to delete contact", Toast.LENGTH_SHORT).show();
        }
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void updateContact(long contactId) {
        String contactName = editContactName.getText().toString().trim();
        String contactNumber = editContactNumber.getText().toString().trim();

        if (TextUtils.isEmpty(contactName) || TextUtils.isEmpty(contactNumber)) {
            Toast.makeText(getContext(), "Please enter both name and number", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName);

        requireContext().getContentResolver().update(
                ContactsContract.Data.CONTENT_URI,
                values,
                ContactsContract.Data.CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{
                        String.valueOf(contactId),
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                }
        );

        ContentValues phoneValues = new ContentValues();
        phoneValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        phoneValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber);
        phoneValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

        requireContext().getContentResolver().update(
                ContactsContract.Data.CONTENT_URI,
                phoneValues,
                ContactsContract.Data.CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{
                        String.valueOf(contactId),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                }
        );

        Toast.makeText(getContext(), "Contact updated successfully!", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void addContact() {
        String contactName = editContactName.getText().toString().trim();
        String contactNumber = editContactNumber.getText().toString().trim();

        if (TextUtils.isEmpty(contactName) || TextUtils.isEmpty(contactNumber)) {
            Toast.makeText(getContext(), "Please enter both name and number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ContentValues values = new ContentValues();
            values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, (String) null);
            values.put(ContactsContract.RawContacts.ACCOUNT_NAME, (String) null);
            Uri rawContactUri = requireContext().getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);

            if (rawContactUri != null) {
                long rawContactId = ContentUris.parseId(rawContactUri);

                ContentValues nameValues = new ContentValues();
                nameValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                nameValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                nameValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName);
                requireContext().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, nameValues);

                ContentValues phoneValues = new ContentValues();
                phoneValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                phoneValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                phoneValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber);
                phoneValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                requireContext().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, phoneValues);

                Toast.makeText(getContext(), "Contact added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to add contact", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Add_Update", "Error adding contact", e);
            Toast.makeText(getContext(), "An error occurred while adding the contact", Toast.LENGTH_SHORT).show();
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }
}