package com.example.contactmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class ContactListFragment extends Fragment {

    private ListView contactListView;
    private Button addContactButton;

    // ContactAdapter will be used to display contacts in the ListView
    private ContactAdapter contactAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);

        contactListView = view.findViewById(R.id.contact_list_view);
        addContactButton = view.findViewById(R.id.button_add_contact);

        // Initialize the adapter with the contact list
        contactAdapter = new ContactAdapter(requireContext(), MainActivity.contactList);
        contactListView.setAdapter(contactAdapter);

        // Handle Add Contact button click
        addContactButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddUpdateContactFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Handle ListView item clicks for editing an existing contact
        contactListView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            // Pass selected contact to AddUpdateContactFragment for editing
            Contact selectedContact = MainActivity.contactList.get(position);
            AddUpdateContactFragment fragment = new AddUpdateContactFragment();

            Bundle bundle = new Bundle();
            bundle.putString("name", selectedContact.getName());
            bundle.putString("phone", selectedContact.getPhoneNumber());
            bundle.putInt("position", position);
            fragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    // Call this method when the data is changed (e.g., after adding or updating a contact)
    public void updateContactList() {
        contactAdapter.notifyDataSetChanged();
    }
}
