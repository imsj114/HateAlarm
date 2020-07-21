package com.example.madcampweek2.ui.maps.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private var lastList: List<MapUser> = listOf()
    private var triggered: Boolean = false

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
            adapter = UserListViewAdapter(context, model._users_blacklist.value ?: listOf())
        }

        model.getBlacklist().observe(viewLifecycleOwner, Observer {
            triggered = true
        })

        model.getUsers().observe(viewLifecycleOwner, Observer<List<MapUser>>{ list ->
            if(triggered){
                updateList(list)
                triggered = false
            }

        })

        val touchListener = RecyclerTouchListener(requireActivity(), recyclerView)
        touchListener.setClickable(object: RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
                //Toast.makeText(requireContext().applicationContext, "pos $position", Toast.LENGTH_SHORT).show()
            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {

            }

        }).setSwipeOptionViews(R.id.delete_task)
            .setSwipeable(R.id.rowFG, R.id.rowBG, object:RecyclerTouchListener.OnSwipeOptionsClickListener{
                override fun onSwipeOptionClicked(viewID: Int, position: Int) {
                    when(viewID) {
                        R.id.delete_task -> {
                            //Toast.makeText(requireContext().applicationContext, "delete", Toast.LENGTH_SHORT).show()
                            val user = (recyclerView.adapter as UserListViewAdapter).userList[position]
                            val uid = user.uid
                            if(user.blocked){
                                model.removeBlacklist(uid)
                            }else{
                                model.addBlacklist(uid)
                            }
                        }
                    }
                }

            })
        recyclerView.addOnItemTouchListener(touchListener)


        return view
    }

    private fun updateList(list: List<MapUser>) {
        recyclerView.apply{
            adapter = UserListViewAdapter(context, list)
        }
    }
}