package com.example.madcampweek2.ui.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.madcampweek2.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ContactFragment extends Fragment implements View.OnClickListener{

    private RecyclerAdapter adapter;
    private FloatingActionButton fab_main, fab_sub1, fab_sub2;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contact, container, false);

        final androidx.recyclerview.widget.RecyclerView ContactView =
                root.findViewById(R.id.recyclerview_contacts);

        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);

        fab_main = (FloatingActionButton) root.findViewById(R.id.fab_main);
        fab_sub1 = (FloatingActionButton) root.findViewById(R.id.fab_sub1);
        fab_sub2 = (FloatingActionButton) root.findViewById(R.id.fab_sub2);

        fab_main.setOnClickListener(this);
        fab_sub1.setOnClickListener(this);
        fab_sub2.setOnClickListener(this);

        // RecyclerView setting
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        ContactView.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerAdapter();
        ContactView.setAdapter(adapter);

        // Recycler adapter contact data setting
        setJSONcontacts(adapter, "contacts.json");  // Load contact data from contacts.json
        //setDeviceContacts(adapter);     // Load the user's contact list
        adapter.notifyDataSetChanged(); // Notify the adapter data modification

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_main:
                switchFab();
                break;
            case R.id.fab_sub1:
                switchFab();
                Toast.makeText(getActivity(), "First Fab", Toast.LENGTH_SHORT).show();
                /*
                * Do something
                */
                break;
            case R.id.fab_sub2:
                switchFab();
                Toast.makeText(getActivity(), "Second Fab", Toast.LENGTH_SHORT).show();
                /*
                 * Do something
                 */
                break;
        }
    }

    // Fab open/close switch
    private void switchFab() {
        if (isFabOpen) {
            fab_main.setImageResource(R.drawable.ic_baseline_add_circle_24);

            fab_sub1.startAnimation(fab_close);
            fab_sub2.startAnimation(fab_close);

            fab_sub1.setClickable(false);
            fab_sub2.setClickable(false);

            isFabOpen = false;
        } else {
            fab_main.setImageResource(R.drawable.ic_baseline_cancel_24);

            fab_sub1.startAnimation(fab_open);
            fab_sub2.startAnimation(fab_open);

            fab_sub1.setClickable(true);
            fab_sub2.setClickable(true);

            isFabOpen = true;
        }
    }


    // Load contact data from json database
    // Read and parse json file -> fill adapter with Contacts
    public void setJSONcontacts(RecyclerAdapter adapter, String filename){
        String json = "";

        try {   // Open and load json file
            InputStream is = getActivity().getAssets().open(filename);
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        try{    // json data parsing
            JSONObject jsonObject = new JSONObject(json);

            JSONArray contactArray = jsonObject.getJSONArray("Contacts");

            for(int i=0; i<contactArray.length(); i++)
            {
                JSONObject contactObject = contactArray.getJSONObject(i);

                Contact contact = new Contact();

                contact.setName(contactObject.getString("NAME"));
                contact.setPhoneNumber(contactObject.getString("PHONE"));
                contact.setProfile(getResources().getIdentifier(
                        contactObject.getString("PROFILE"),"drawable",
                        getActivity().getPackageName()));

                adapter.addItem(contact);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Load the user's contact list
    public void setDeviceContacts(RecyclerAdapter adapter){
        ContactUtil contactUtil = new ContactUtil(getActivity());

        ArrayList<Contact> phoneBook;
        phoneBook = contactUtil.getContactList();

        for(int i=0; i<phoneBook.size(); i++)
        {
            adapter.addItem(phoneBook.get(i));
        }
    }

}