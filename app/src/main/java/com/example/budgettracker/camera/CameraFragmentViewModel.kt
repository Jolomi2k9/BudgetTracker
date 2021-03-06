package com.example.budgettracker.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.budgettracker.data.Product
import com.example.budgettracker.data.Receipt
import com.example.budgettracker.data.ReceiptDao
import com.example.budgettracker.data.Shop
import com.example.budgettracker.di.ApplicationScope
import com.google.mlkit.nl.entityextraction.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CameraFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao,
    @ApplicationScope private val applicationScope: CoroutineScope
): ViewModel() {
    lateinit var mTextEntityExtractor: EntityExtractor
    //
    var mIsModelAvailable = false
    //list to hold all items detected in a receipt
    private var productList = mutableListOf<String>()
    //list to hold only prices detected
    private var priceList = mutableListOf<String>()
    //list to hold only names of product
    private var productNameList = mutableListOf<String>()
    //list to hold items of class "Product"
    private var products = mutableListOf<Product>()
    //Pass the captured image to MLKit OCR
    fun textRecognition(image: Bitmap) {
        val image = InputImage.fromBitmap(image, 0)
        val recognizer = TextRecognition.getClient()

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                tescoProcessTextBlock(visionText)
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

    //Process MLKit text using algorithm for Tesco Store receipts
    private fun tescoProcessTextBlock(result: Text) {

        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            for (line in block.lines) {
                val lineText = line.text
                //store all lines in productList
                productList.add(lineText)

                //Take every line that starts with an "EUR" or "FUR"and assume this to be the price
                if(lineText[0] == 'E' || lineText[0] == 'F' && lineText[1] == 'U' && lineText[2] == 'R'){
                    //remove the "EUR" from the price
                    var sansEUR = String()
                    for (i in lineText.indices){
                        if (i > 2){
                            sansEUR += lineText[i].toString()
                        }
                    }
                    //if any char is an 'O' or 'o', substitute with a "0"
                    var finalPrice = String()
                    for (i in sansEUR.indices){
                        finalPrice += if (sansEUR[i] == 'O' || sansEUR[i] == 'o'){
                            "0"
                        }else{
                            sansEUR[i].toString()
                        }
                    }
                    //add this to the priceList
                    priceList.add(finalPrice)
                }
            }
        }
        //Extract only the names of the product from the product list
        for (i in productList.indices){
            if (i <= priceList.size ){
                productNameList.add(productList[i])
            }
        }

        /*Log.i("ImageViewFragment","${productList.size}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
        Log.i("ImageViewFragment Price","${priceList.size}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2")
        Log.i("ImageViewFragment name","${productNameList.size}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!3")
        for (i in productList){
            Log.i("ImageViewFragment","$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4")
        }
        for(i in priceList){
            Log.i("ImageViewFragment Price","$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!5")
        }
        for(i in productNameList){
            Log.i("ImageViewFragment names","$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!6")
        }*/

        //insert into shops
        val shops = listOf(Shop("Tesco"))
        //get shops primary key
        val hey = shops[0]
        val sKey = hey.shopId
        //insert into receipts
        val receipts = listOf(Receipt(sKey))
        //get receipt primary key
        val hey1 = receipts[0]
        val rKey = hey1.receiptId

        //prepare products to be inserted into database
        for (i in priceList.indices){
             //products = listOf(Product(productNameList[i],priceList[i],rKey))
             products.add(Product(productNameList[i],priceList[i],rKey))
        }


        //Insert extracted data into database
        applicationScope.launch{
            Log.i("ImageView appScope","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
            shops.forEach { receiptDao.insertShop(it)}
            receipts.forEach { receiptDao.insertReceipt(it)}
            products.forEach { receiptDao.insertProduct(it)}
            //products.forEach { receiptDao.insertProduct(it)}
        }
    }
    //Use entity extraction to extract the prices from the captured string
}