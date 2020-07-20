package com.example.madcampweek2.ui.contact;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.example.madcampweek2.api.RetroApi;
import com.example.madcampweek2.model.Contact;
import com.example.madcampweek2.model.User;
import com.facebook.Profile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.facebook.FacebookSdk.getApplicationContext;


public class ContactFragment extends Fragment implements View.OnClickListener{

    ContactViewModel contactViewModel;
    private RecyclerAdapter adapter = new RecyclerAdapter(getActivity());;

    private FloatingActionButton fab_main, fab_sub1, fab_sub2;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    private ArrayList<Contact> jsonphoneBook, devicephoneBook;

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
        adapter = new RecyclerAdapter(getActivity());
        ContactView.setAdapter(adapter);

        /*
        // Recycler adapter contact data setting
        // From json file
        jsonphoneBook = loadJSONcontacts("contacts.json");
        setDeviceContacts(adapter, jsonphoneBook);
            // From device
        devicephoneBook = loadDeviceContacts();
        setDeviceContacts(adapter, devicephoneBook);
         */

        // test용~
        Contact con1 = new Contact();
        con1.setName("only");
        con1.setPhoneNumber("102013123");

        adapter.addItem(con1);
        adapter.notifyDataSetChanged();

        return root;
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
                profileId = Profile.getCurrentProfile().getId();
                contactViewModel.setPid(profileId);
                contactViewModel.ReloadContacts(profileId);
                break;
            case R.id.fab_sub2:
                switchFab();
                Toast.makeText(getActivity(), "Add contact", Toast.LENGTH_SHORT).show();
                FabaddContact();
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

    // Load contact data from json file
    // Return jsonphoneBook in ArrayList<Contact>
    public ArrayList<Contact> loadJSONcontacts(String filename){
        String json = "";
        ArrayList<Contact> jsonphoneBook = new ArrayList<Contact>();

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
//                contact.setProfile(getResources().getIdentifier(
//                        contactObject.getString("PROFILE"),"drawable",
//                        getActivity().getPackageName()));

                jsonphoneBook.add(contact);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonphoneBook;
    }

    // Load the user device's contact list
    // Return phoneBook in ArrayList<Contact>
    public ArrayList<Contact> loadDeviceContacts(){
        ContactUtil contactUtil = new ContactUtil(getActivity());

        ArrayList<Contact> phoneBook;
        phoneBook = contactUtil.getContactList();

        return phoneBook;
    }

    // Adapt contact data into recycler adapter
    public void setDeviceContacts(RecyclerAdapter adapter, ArrayList<Contact> phoneBook){
        for(int i=0; i<phoneBook.size(); i++)
        {
            adapter.addItem(phoneBook.get(i));
        }
    }

    public void postUserContacts(RetroApi retroApi, User User){
        Call<User> call = retroApi.registerUser(User);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    User result = response.body();
                    Toast.makeText(getActivity()
                            ,"registerUser Succeess\n Result:" + result.toString(),
                            Toast.LENGTH_LONG ).show();
                    Log.d(TAG, "registerUser Suceess, Result: " + result.toString());
                } else{
                    Toast.makeText(getActivity()
                            ,"registerUser response Fail", Toast.LENGTH_LONG ).show();
                    Log.d(TAG, "registerUser response Fail");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getActivity()
                        ,"registerUser Fail: " + t.getMessage(), Toast.LENGTH_LONG ).show();
                Log.d(TAG, "registerUser Fail:" +t.getMessage());
            }
        });
    }

    public void getContactList(RetroApi retroApi, String uid){
        Call<List<Contact>> call = retroApi.getUserContacts(uid);

        call.enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if(response.isSuccessful()){
                    List<Contact> result = response.body();
                    Toast.makeText(getActivity()
                            ,"getUserContacts Succeess\n Result: " + result.toString()
                            , Toast.LENGTH_LONG ).show();
                    Log.d(TAG, "getUserContacts Succeess\n Result: " + result.toString());
//                    adapter.setData(result);
                } else{
                    Toast.makeText(getActivity()
                            ,"getUserContacts Fail", Toast.LENGTH_LONG ).show();
                    Log.d(TAG, "getUserContacts Fail");
                }
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                Toast.makeText(getActivity()
                        ,"getUserContacts Fail: "+ t.getMessage(), Toast.LENGTH_LONG ).show();
                Log.d(TAG, "getUserContacts Fail: "+t.getMessage());
            }
        });
    }


}