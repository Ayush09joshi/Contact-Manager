package com.example.contactmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdaptor extends RecyclerView.Adapter<ContactAdaptor.ContactViewHolder> {
    private final List<com.example.contactmanager.Contact> contactList;

    public ContactAdaptor(List<com.example.contactmanager.Contact> contactList) {
        this.contactList = contactList;
    }

    // Updating the contactList with a new list.
    @SuppressLint("NotifyDataSetChanged")
    public void updateContactList(List<com.example.contactmanager.Contact> newContactList) {
        this.contactList.clear();
        this.contactList.addAll(newContactList);
        notifyDataSetChanged(); // Notify RecyclerView to refresh
    }

    // Inflating the layout for each item in the list (contact_card.xml).
    @NonNull
    @Override
    public ContactAdaptor.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdaptor.ContactViewHolder holder, int position) {
        // Bind data to the views
        com.example.contactmanager.Contact contact = contactList.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhoneNumber());

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            if (context != null && context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;

                // Create a new instance of AddUpdateContactFragment
                com.example.contactmanager.Add_Update fragment = new com.example.contactmanager.Add_Update();

                // Pass data using a Bundle
                Bundle bundle = new Bundle();
                bundle.putLong(String.valueOf(R.string.contact_id_key), contact.getContactId());
                bundle.putString(String.valueOf(R.string.contact_name_key), contact.getName());
                bundle.putString(String.valueOf(R.string.contact_number_key), contact.getPhoneNumber());

                // Attaching the bundle to the fragment.
                fragment.setArguments(bundle);

                // Begin the transaction to replace the current fragment
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)  // Replace with your fragment container's ID
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.e("ContactAdapter", "Context is null or not a FragmentActivity!");
            }
        });

    }

    // Counting the number of items in the RecyclerView.
    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // ViewHolder class to hold item views
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, phoneTextView;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            phoneTextView = itemView.findViewById(R.id.textViewNumber);
        }
    }
}
