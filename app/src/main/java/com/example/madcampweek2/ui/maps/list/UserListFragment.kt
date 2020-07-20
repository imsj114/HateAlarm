package com.example.madcampweek2.ui.maps.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madcampweek2.R
import com.example.madcampweek2.model.MapUser
import com.example.madcampweek2.ui.gallery.GalleryViewAdapter
import com.example.madcampweek2.ui.gallery.SpacesItemDecoration
import com.example.madcampweek2.ui.maps.ALERT_RADIUS
import com.example.madcampweek2.ui.maps.MapsViewModel
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class UserListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val model: MapsViewModel by activityViewModels()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val callback = requireActivity().onBackPressedDispatcher.addCallback(this){
//            findNavController().navigateUp()
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_userlist, container, false)

        recyclerView = view.findViewById(R.id.user_list)
        recyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = UserListViewAdapter(context, mutableListOf())
        }

        model.getUsers().observe(viewLifecycleOwner, Observer<List<MapUser>>{ list ->
            recyclerView.apply{
                adapter = UserListViewAdapter(context, list)
            }
        })

        return view
    }
}