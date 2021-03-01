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
import com.example.budgettracker.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.fragment_imageview.*
import java.io.File

private const val FILE_NAME = "image.jpg"
private const val REQUEST_CODE = 10
private lateinit var imageFile: File
class ImageViewFragment : Fragment(R.layout.fragment_imageview) {

    private lateinit var safeContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            textRecognition(takenImage)
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

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


    private fun textRecognition(image: Bitmap) {

        val image = InputImage.fromBitmap(image, 0)
        val recognizer = TextRecognition.getClient()

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks){
                    val blockText = block.text
                    //Log.i("ImageViewFragment",blockText)
                    for (line in block.lines) {
                        val lineText = line.text
                        Log.i("ImageViewFragment",lineText)
                    }
                }
            }
            .addOnFailureListener { e ->
            }
    }

}