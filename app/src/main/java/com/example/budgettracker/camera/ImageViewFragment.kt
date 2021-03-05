package com.example.budgettracker.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.budgettracker.R
import com.example.budgettracker.data.ReceiptDatabase
import com.example.budgettracker.databinding.FragmentImageviewBinding
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_imageview.*
import java.io.File

private const val FILE_NAME = "image.jpg"
private const val REQUEST_CODE = 10
private lateinit var imageFile: File

@AndroidEntryPoint
class ImageViewFragment : Fragment(R.layout.fragment_imageview) {

    private val viewModel: CameraFragmentViewModel by viewModels()
    lateinit var jTextEntityExtractor: EntityExtractor
    private var jIsModelAvailable = false
    //testing

    private lateinit var safeContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //
        //EntityExtractor object configured with EntityExtractorOptions
         jTextEntityExtractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
                .build())
        //Confirming the needed model is downloaded on the device
        jTextEntityExtractor.downloadModelIfNeeded()
            .addOnSuccessListener { _ ->
                 jIsModelAvailable = true
            }
        //
        if (ContextCompat.checkSelfPermission(safeContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA),110)
            takePicture()
        }else{
            takePicture()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //check if user has successfully taken a picture with the camera
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
           // val takenImage = data?.extras?.get("data") as Bitmap
            val takenImage = BitmapFactory.decodeFile(imageFile.absolutePath)
            imageView.setImageBitmap(takenImage)
            //update mIsModelAvailable and mTextEntityExtractor in viewModel
            viewModel.mIsModelAvailable = jIsModelAvailable
            viewModel.mTextEntityExtractor = jTextEntityExtractor
            //pass the captured image to textRecognition in viewModel
            viewModel.textRecognition(takenImage)
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    //
    private fun takePicture(){

        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageFile = getImageFile(FILE_NAME)
        //check for a camera
        val fileProvider = FileProvider.getUriForFile(safeContext, "com.example.fileprovider", imageFile)
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        //if (takePicture.resolveActivity(this.packageManager) != null) {
        startActivityForResult(takePicture, REQUEST_CODE)
        //} else {
        // Toast.makeText(this, "No camera detected!", Toast.LENGTH_LONG).show()
        //}
    }

    private fun getImageFile(fileName: String): File {
        //access package-specific directories
        val storageDirectory = safeContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

}