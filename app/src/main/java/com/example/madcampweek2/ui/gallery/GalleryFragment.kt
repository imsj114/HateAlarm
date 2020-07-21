package com.example.madcampweek2.ui.gallery

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madcampweek2.R
import com.example.madcampweek2.model.Image
import com.facebook.Profile
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class GalleryFragment : Fragment(), View.OnClickListener {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    private lateinit var recyclerView : RecyclerView
    private val galleryViewModel : GalleryViewModel by activityViewModels()
    private lateinit var adapter : GalleryViewAdapter

    lateinit var fab_open : Animation
    lateinit var fab_close : Animation
    lateinit var fab: FloatingActionButton
    lateinit var fab1: FloatingActionButton
    lateinit var fab2: FloatingActionButton
    lateinit var currentPhotoPath: String
    var isFabOpen = false

    private lateinit var profileId: String

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
                (recyclerView.adapter as GalleryViewAdapter).setData(_images as List<Image>)
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
                profileId = Profile.getCurrentProfile().id
                galleryViewModel.setProfileId(profileId)
                galleryViewModel.ReloadImages(profileId)
                Toast.makeText(requireContext(), "Reload Images", Toast.LENGTH_SHORT)
            }
            R.id.fab_gal2 -> {
                switchFab()
                uploadDialog()
                Toast.makeText(requireContext(), "Upload Image", Toast.LENGTH_SHORT)
            }
        }
    }

    fun uploadDialog() {
        val builder =
            AlertDialog.Builder(activity!!)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_imageselect, null)
        builder.setView(view)

        val fromgallery =
            view.findViewById<View>(R.id.fromgallery) as Button
        val fromcamera =
            view.findViewById<View>(R.id.fromcamera) as Button

        val dialog = builder.create()
        fromgallery.setOnClickListener {
            /*
            *   Load device gallery
            */
            uploadFromFile()
            dialog.dismiss()
        }
        fromcamera.setOnClickListener {
            /*
            *   Load device camera
            */
            uploadFromCamera()
            dialog.dismiss()
        }
        dialog.show()
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context?.cacheDir!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun uploadFromCamera() {
        Log.d("newImage", ">>getNewImage")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also{ takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also{
                val photoFile: File? = try{
                    createImageFile()
                } catch (e: IOException) {
                    null
                }
                Log.d("newImage", photoFile.toString())
                photoFile?.also{
                    try{
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.example.madcampweek2.fileprovider",
                            it
                        )
                        Log.d("newImage", photoURI.toString())
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        Log.d("newImage", "successful")
                    }catch(e: Exception) {
                        Log.d("newImage", e.toString())
                    }
                }
            }
        }
    }

    private fun uploadFromFile() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        val pickIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val chooserIntent = Intent.createChooser(getIntent, "Select Image").putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Intent>(pickIntent))
        startActivityForResult(Intent.createChooser(chooserIntent, "Select Picture"), REQUEST_PICK_IMAGE)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_IMAGE_CAPTURE -> {
                if(resultCode == Activity.RESULT_OK){
                    // Successfully got image from camera app!
                    // Now add file to the server.
                    Log.i("newImage", "OK")
                    galleryViewModel.addImage(File(currentPhotoPath))
                }else {
                    File(currentPhotoPath).delete()
                    Log.i("newImage", "Not OK")
                }
            }
            REQUEST_PICK_IMAGE -> {
                if(resultCode == Activity.RESULT_OK){
                    //Log.i("newImage", "OK")
                    val uri = data!!.data!!
                    Log.i("newImage", uri.toString())
                    val file = File(getPath(requireContext(), uri)!!)
                    galleryViewModel.addImage(file)
                    Log.i("newImage", "OK")
                }
            }
        }
    }

    private fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        try{
            val outputStream = FileOutputStream(file)
            var read: Int
            val bytes = byteArrayOf()
            while ( inputStream.read(bytes).also { read = it } != -1) {
                outputStream.write(bytes, 0, read)
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context?, uri: Uri): String? {
        val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id: String = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(requireContext(), contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs: Array<String?> = arrayOf(
                    split[1]
                )
                return getDataColumn(requireContext(), contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(requireContext(), uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = uri?.let {
                context.getContentResolver().query(
                    it, projection, selection, selectionArgs,
                    null
                )
            }
            if (cursor != null && cursor.moveToFirst()) {
                val column_index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}