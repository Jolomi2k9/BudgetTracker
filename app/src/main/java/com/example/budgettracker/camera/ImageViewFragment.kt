package com.example.budgettracker.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.budgettracker.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.fragment_imageview.*

private const val REQUEST_CODE = 10
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
            val takenImage = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(takenImage)
            textRecognition(takenImage)
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    private fun takePicture(){

        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //check for a camera
        //if (takePicture.resolveActivity(this.packageManager) != null) {
        startActivityForResult(takePicture, REQUEST_CODE)
        //} else {
        // Toast.makeText(this, "No camera detected!", Toast.LENGTH_LONG).show()
        //}
    }

    private fun textRecognition(image: Bitmap) {

        val image = InputImage.fromBitmap(image, 0)
        val recognizer = TextRecognition.getClient()

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks){
                    val blockText = block.text
                    Toast.makeText(safeContext,blockText, Toast.LENGTH_LONG).show()
                    for (line in block.lines) {
                        val lineText = line.text
                        Toast.makeText(safeContext,lineText, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener { e ->

            }
    }

}