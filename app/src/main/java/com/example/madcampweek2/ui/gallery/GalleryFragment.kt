package com.example.madcampweek2.ui.gallery

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madcampweek2.R


class GalleryFragment : Fragment() {
    private lateinit var recyclerView : RecyclerView
//    private lateinit var images : MutableList<Image>s

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        images = mutableListOf<Image>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.apply{
            layoutManager = GridLayoutManager(activity, 2)
            adapter = GalleryViewAdapter(context, mutableListOf())
        }
        recyclerView.addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.spacing_width)))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}