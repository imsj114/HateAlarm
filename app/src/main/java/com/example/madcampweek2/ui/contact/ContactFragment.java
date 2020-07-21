package com.example.madcampweek2.ui.contact;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcampweek2.R;
import com.example.madcampweek2.model.Contact;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import static android.content.ContentValues.TAG;
import static com.facebook.FacebookSdk.getApplicationContext;


public class ContactFragment extends Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener{

    ContactViewModel contactViewModel;
    private RecyclerAdapter adapter = new RecyclerAdapter(getActivity(), this);;

    private FloatingActionButton fab_main, fab_sub1, fab_sub2;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    private String BASE_URL = "http://192.249.19.240:3080/";
    private String profileId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contact, container, false);

        final RecyclerView ContactView =
                root.findViewById(R.id.recyclerview_contacts);

        // Floating buttons setting
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
        adapter = new RecyclerAdapter(getActivity(), this);
        ContactView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //you can get the clicked item from the adapter using its position
        Contact item = adapter.getItem(position);

        // Something to do
        Toast.makeText(getActivity(), "item clicked!!", Toast.LENGTH_LONG).show();
        Log.d(TAG, "item clicked!!!");
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        contactViewModel.getContacts().observe(getViewLifecycleOwner(), new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> _contacts) {
                adapter.setData(_contacts);
                adapter.notifyDataSetChanged();
            }
        });

        if (Profile.getCurrentProfile() == null) {
            ProfileTracker mProfileTracker = null;
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    Log.d("facebook - profile", currentProfile.getFirstName());
                    this.stopTracking();
                    // TODO
                    profileId = Profile.getCurrentProfile().getId();
                    contactViewModel.setProfileId(profileId);
                    contactViewModel.ReloadContacts(profileId);
                }
            };
            // no need to call startTracking() on mProfileTracker
            // because it is called by its constructor, internally.
        } else {
            Profile profile = Profile.getCurrentProfile();
            Log.v("facebook - profile", profile.getFirstName());
            // TODO
            profileId = Profile.getCurrentProfile().getId();
            contactViewModel.setProfileId(profileId);
            contactViewModel.ReloadContacts(profileId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_main:
                switchFab();
                break;
            case R.id.fab_sub1:
                switchFab();
                Toast.makeText(getActivity(), "Relaod your contacts", Toast.LENGTH_SHORT).show();
                contactViewModel.setProfileId(profileId);
                contactViewModel.ReloadContacts(profileId);
                break;
            case R.id.fab_sub2:
                switchFab();
                Toast.makeText(getActivity(), "Add contact", Toast.LENGTH_SHORT).show();
                FabaddContact();
                contactViewModel.ReloadContacts(profileId);
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


    public void FabaddContact(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_contact, null);
        builder.setView(view);

        final Button submit = (Button) view.findViewById(R.id.buttonSubmit);
        final Button cancel = (Button) view.findViewById(R.id.buttonCancel);
        final EditText editTextName = (EditText) view.findViewById(R.id.editTextAddName);
        final EditText editTextPhone = (EditText) view.findViewById(R.id.editTextAddPhone);

        final AlertDialog dialog = builder.create();

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String strName = editTextName.getText().toString();
                String strPhone = editTextPhone.getText().toString();

                if(strName.equals("") || strPhone.equals("")){
                    Toast.makeText(getApplicationContext()
                            , "Type infomation for new contact", Toast.LENGTH_SHORT).show();
                } else{
                    Contact new_contact = new Contact();
                    new_contact.setName(strName);
                    new_contact.setPhoneNumber(strPhone);
                    contactViewModel.addContact(new_contact);
                    Toast.makeText(getApplicationContext()
                            , "Name: "+ strName+ "\nPhonenumber: "+ strPhone
                            , Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }


}