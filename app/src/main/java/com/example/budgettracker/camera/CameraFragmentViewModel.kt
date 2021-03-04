package com.example.budgettracker.camera

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.budgettracker.camera.CameraFragment.Companion.TAG
import com.example.budgettracker.data.ReceiptDao
import com.google.mlkit.nl.entityextraction.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition

class CameraFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao): ViewModel() {

    private lateinit var mEntityParams: EntityExtractionParams
    lateinit var mTextEntityExtractor: EntityExtractor
    var mIsModelAvailable = false

    //Pass the captured image to MLKit OCR
    fun textRecognition(image: Bitmap) {
        val image = InputImage.fromBitmap(image, 0)
        val recognizer = TextRecognition.getClient()

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                processTextBlock(visionText)
                /*for (block in visionText.textBlocks){
                    val blockText = block.text
                    for (line in block.lines) {
                        val lineText = line.text
                        //productLists.add(lineText)
                        //Log.i("ImageViewFragment",lineText)
                    }
                }*/
            }
            .addOnFailureListener { e ->
            }

    }



    //Process MLKit text
    private fun processTextBlock(result: Text) {
        //
        if (mIsModelAvailable.not()){
            return
        }
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            for (line in block.lines) {
                val lineText = line.text
                //Log.i("ImageViewFragment",lineText)
                 mEntityParams =
                    EntityExtractionParams.Builder(lineText)
                        //.setEntityTypesFilter(setOf(Entity.TYPE_MONEY))
                        .build()
                            mTextEntityExtractor
                            .annotate(mEntityParams)
                        .addOnSuccessListener { entityAn ->
                            for (entityAnnotation in entityAn) {
                                val entities: List<Entity> = entityAnnotation.entities
                                for (entity in entities) {
                                    when (entity) {
                                        is MoneyEntity -> {
                                            Log.d(TAG, "Currency: ${entity.unnormalizedCurrency}")
                                            Log.d(TAG, "Integer part: ${entity.integerPart}")
                                            Log.d(TAG, "Fractional Part: ${entity.fractionalPart}")
                                        }
                                        else -> {
                                            Log.d(TAG, "  $entity")
                                        }
                                    }
                                }
                            }
                        }
            }
        }
    }



    //Use entity extraction to extract the prices from the captured string


}