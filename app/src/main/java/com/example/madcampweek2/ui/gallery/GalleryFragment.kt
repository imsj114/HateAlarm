package com.example.madcampweek2.ui.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madcampweek2.R
import com.example.madcampweek2.model.Contact
import com.example.madcampweek2.model.Image
import com.example.madcampweek2.ui.contact.ContactViewModel
import com.example.madcampweek2.ui.maps.SocketService.Companion.TAG
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*


class GalleryFragment : Fragment(), View.OnClickListener {
    private lateinit var recyclerView : RecyclerView
    private lateinit var images : MutableList<Image>
    private val galleryViewModel : GalleryViewModel by activityViewModels()
    private lateinit var adapter : GalleryViewAdapter

    lateinit var fab_open : Animation
    lateinit var fab_close : Animation
    lateinit var fab: FloatingActionButton
    lateinit var fab1: FloatingActionButton
    lateinit var fab2: FloatingActionButton
    var isFabOpen = false

    override fun onCreateView(inflater: LayoutInflater
                              , container: ViewGroup?
                              , savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.apply{
            layoutManager = GridLayoutManager(activity, 2)
            adapter = GalleryViewAdapter(context, mutableListOf())
        }

        recyclerView.addItemDecoration(SpacesItemDecoration(
            resources.getDimensionPixelSize(R.dimen.spacing_width)))

        fab_open =
            AnimationUtils.loadAnimation(activity, R.anim.fab_open)
        fab_close =
            AnimationUtils.loadAnimation(activity, R.anim.fab_close)

        fab = view.findViewById(R.id.fab_gal) as FloatingActionButton
        fab1 = view.findViewById(R.id.fab_gal1) as FloatingActionButton
        fab2 = view.findViewById(R.id.fab_gal2) as FloatingActionButton

        fab.setOnClickListener(this)
        fab1.setOnClickListener(this)
        fab2.setOnClickListener(this)

        return view
    }

    override fun onViewCreated(view: View
                               , savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val galleryViewModel = ViewModelProvider(requireActivity()).get(
            GalleryViewModel::class.java
        )
        galleryViewModel.getImages().observe(
            viewLifecycleOwner,
            Observer<List<Image?>?> { _images ->
                adapter.setData(_images as List<Image>)
            })
    }

    // Fab open/close switch
    private fun switchFab() {
        if (isFabOpen) {
            fab.setImageResource(R.drawable.ic_baseline_add_circle_24)
            fab1.startAnimation(fab_close)
            fab2.startAnimation(fab_close)
            fab1.setClickable(false)
            fab2.setClickable(false)
            isFabOpen = false
        } else {
            fab.setImageResource(R.drawable.ic_baseline_cancel_24)
            fab1.startAnimation(fab_open)
            fab2.startAnimation(fab_open)
            fab1.setClickable(true)
            fab2.setClickable(true)
            isFabOpen = true
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_gal -> switchFab()
            R.id.fab_gal1 -> {
                switchFab()
                Toast.makeText(activity, "fab1", Toast.LENGTH_SHORT)
            }
            R.id.fab_gal2 -> {
                switchFab()
                Toast.makeText(activity, "fab2", Toast.LENGTH_SHORT)
            }
        }
    }

    fun getDrawableFile(draw: Drawable) :File {
        //val drawable = resources.getDrawable(R.drawable.jordy, null)
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.jordy)
        val filename = "test.jpeg"
        val file = File(requireContext().cacheDir, filename)
        file.createNewFile()
        val bos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 0, bos)
        val bitmapdata = bos.toByteArray()
        var fos: FileOutputStream? = null
        try{
            fos = FileOutputStream(file)
        }catch(e: FileNotFoundException){
            e.printStackTrace()
        }
        try{
            fos!!.write(bitmapdata)
            fos.flush()
            fos.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

//        galleryViewModel.addImage(file)
        return file
    }

}