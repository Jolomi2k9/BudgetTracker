package com.example.budgettracker.camera

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.*
import com.example.budgettracker.di.ApplicationScope
import com.google.mlkit.nl.entityextraction.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class CameraFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao,
    @ApplicationScope private val applicationScope: CoroutineScope
): ViewModel() {

    //Use channels to send data between two coroutines
    private val cameraEventChannel = Channel<CameraEvent>()
    val cameraEvent = cameraEventChannel.receiveAsFlow()
    //
    //lateinit var mTextEntityExtractor: EntityExtractor
    //
    //var mIsModelAvailable = false
    //list to hold all items detected in a receipt
    private var productList = mutableListOf<String>()
    //list to hold only prices detected
    private var priceList = mutableListOf<String>()
    //Filtered priceList
    private var filteredPriceList = mutableListOf<String>()
    //list to hold only names of product
    private var productNameList = mutableListOf<String>()
    //list to hold items of class "Product"
    private var products = mutableListOf<Product>()
    //
    private var rets = listOf<Receipt>()

    //Pass the captured image to MLKit OCR
    fun textRecognition(image: Bitmap) {
        val image = InputImage.fromBitmap(image, 0)
        val recognizer = TextRecognition.getClient()

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onImageProcessingCompleted()
                processTextBlock(visionText)
            }
            .addOnFailureListener { e ->
            }
    }

    //Process MLKit text using algorithm for Tesco Store receipts
    private fun processTextBlock(result: Text) {
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            for (line in block.lines) {
                val lineText = line.text
                //store all lines in productList
                productList.add(lineText)

                Log.i("ReceiptImageView","${lineText}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
                /*//Take every line that starts with an "EUR" or "FUR"and assume this to be the price
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
                }*/
            }
        }
        //Identify which receipt has been scanned and call the associated function or display
        //an error message
        for (i in productList) {
            when{
                i.toUpperCase(Locale.ROOT) == "TESCO" || i.toUpperCase(Locale.ROOT) == "TESC" -> {
                    tescoReceipt()
                    break
                }
                i.toUpperCase(Locale.ROOT).contains("ALDI") -> {
                    aldiReceipt()
                    break
                }
                i.toUpperCase(Locale.ROOT).contains("LODL") ||
                        i.toUpperCase(Locale.ROOT).contains("LDL") -> {
                    lidlReceipt()
                    break
                }

            }

        }
        //Extract only the names of the product from the product list
        /*for (i in productList.indices){
            if (i <= priceList.size ){
                productNameList.add(productList[i])
            }
            //Log.i("ImageViewFragment","${productList}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
        }*/

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
        /*val shops = listOf(Shop("Tesco4"))
        //get shops primary key
        val hey = shops[0]
        val sKey = hey.shopId
        //insert into receipts
        val receipts = listOf(Receipt(sKey))
        //get receipt primary key
        val hey1 = receipts[0]
        val rKey = hey1.receiptId*/

        //prepare products to be inserted into database
        /*for (i in priceList.indices){
             //products = listOf(Product(productNameList[i],priceList[i],rKey))
             products.add(Product(productNameList[i],priceList[i],rKey))
        }*/


        //Insert extracted data into database
        /*applicationScope.launch{
            Log.i("ImageView appScope","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
            shops.forEach { receiptDao.insertShop(it)}
            receipts.forEach { receiptDao.insertReceipt(it)}
            products.forEach { receiptDao.insertProduct(it)}
            //products.forEach { receiptDao.insertProduct(it)}
        }*/

        /*applicationScope.launch {
            var sKey = 0
            var rKey = 0
            //create shop list
            val shops = listOf(Shop("Tesco"))
            //Insert into shops table
            shops.forEach { receiptDao.insertShop(it)}
            //get auto generated primary key from Shops table
            val bKey = receiptDao.getShopsId()
            bKey.forEach {
                sKey = it.shopId
            }
            //create receipt list
            val receipts = listOf(Receipt(sKey))
            //insert into receipt table
            receipts.forEach { receiptDao.insertReceipt(it)}
            //get auto generated primary key from receipt table
            val cKey = receiptDao.getReceiptWithID(sKey)
            cKey.forEach {
                rKey = it.receiptId
            }
            //
            rets = cKey
            //create product lists of receipt products
            for (i in priceList.indices){
                //products = listOf(Product(productNameList[i],priceList[i],rKey))
                products.add(Product(productNameList[i],priceList[i],rKey))
            }
            //Insert into products table
            products.forEach { receiptDao.insertProduct(it)}
        }*/
    }

    fun tescoReceipt(){
        var firstEurIndex = 0
        var firstIndexCount = 0
        var secondIndexCount: Int
        var plusTrigger = false
        for (i in productList) {
            //Take every line that starts with an "EUR" or "FUR"and assume this to be the price
            if (i.contains("EUR") || i.contains("FUR")) {
                //Check if the first and second EUR occur concurrently
                if(firstIndexCount == 1){
                    //Log.i("Receipt1ImageView","${firstEurIndex}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2")
                    secondIndexCount = productList.indexOf(i)
                    if((firstEurIndex+1) != secondIndexCount){
                        firstEurIndex = secondIndexCount
                        plusTrigger = true
                    }
                    firstIndexCount ++
                }
                //Check for the first price index
                if (firstIndexCount == 0 ){
                    firstEurIndex = productList.indexOf(i)
                    //Log.i("Receipt1ImageView","${firstEurIndex}!!!!!!!!!!!!!!!!!!!!!${productList[firstEurIndex]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
                    firstIndexCount ++
                }
                //remove the "EUR" from the price
                var sansEUR = String()
                for (c in i.indices) {
                    if (c > 2) {
                        sansEUR += i[c].toString()
                    }
                }
                //if any char is an 'O' or 'o', substitute with a "0"
                var finalPrice = String()
                for (i in sansEUR.indices) {
                    finalPrice += if (sansEUR[i] == 'O' || sansEUR[i] == 'o') {
                        "0"
                    } else {
                        sansEUR[i].toString()
                    }
                }
                //add this to the priceList
                priceList.add(finalPrice)
            }
        }
        //
        for (i in productList) {
            //Conditions for items that are not price items on the receipt
            if (!i.contains("EUR") && !i.contains("FUR")) {
                when {
                    //When "total" or "sub total" is encountered, end the loop
                    i.toUpperCase(Locale.ROOT) == "TOTAL" || i.toUpperCase(Locale.ROOT) == "SUB TOTAL" ->{
                        break
                    }
                    //ignore prices without an EUR prefix
                    i.contains('.') && i.length < 7 ->{
                        //do nothing
                    }
                    //ignore the first two items int he list,
                    // which is usually the "Tesco Ireland" logo
                    productList.indexOf(i) == 0 || productList.indexOf(i) == 1 ->{
                        //do nothing
                    }else -> {
                    //add everything else to the product list, these are the purchased
                    // product names on the receipt
                        productNameList.add(i)
                    }
                }
            }
        }

        /*for (i in productNameList) {
            Log.i("Receipt2ImageView","Produce${productNameList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.5")
        }

        for (i in priceList) {
            Log.i("Receipt2ImageView","Price${priceList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.6")
        }*/

        val numOfEur = priceList.size
        val numOfProductEur = numOfEur - 2
        val lastEurIndex = numOfEur  - 3
        var firstProductIndex = 2 //firstEurIndex - numOfProductEur
        val lastProductIndex = firstEurIndex - 1
        //val totalIndex = lastEurIndex + 1
        val totalPriceIndex = numOfEur - 2
        //
        if(plusTrigger){
            firstProductIndex --
        }

        /*priceList.forEach { price ->


        }*/
        /*for (i in productList){
            when{
                i == "REDUCED PRICE" -> {firstProductIndex -2 }
                i.contains("kg") -> {firstProductIndex -- }
                i.length < 3 -> {firstProductIndex -- }
            }
            Log.i("Receipt2ImageView","$firstProductIndex!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.0")
        }*/
        //


        /*var tests = 0
        val testProductList = mutableListOf<String>()
        for (i in firstProductIndex .. lastProductIndex){
            testProductList.add(productList[i])
            Log.i("Receipt2ImageView","${testProductList[tests]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.3")
            tests ++
        }
        Log.i("Receipt2ImageView","${priceList.size}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.4")
        Log.i("Receipt2ImageView","${testProductList.size}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.5")*/




        var priceIndex = 0
        for (i in productNameList){
            //
            when {
                //When string detected, add spaces to the filtered price list
                i.contains("REDUCE") -> {
                    //filteredPriceList[testProductList.indexOf(i) - 1] = ""
                    //filteredPriceList[testProductList.indexOf(i)] = ""//testProductList[testProductList.indexOf(i)]
                    filteredPriceList.add(index = productNameList.indexOf(i) - 1,element = "")
                    filteredPriceList.add(index = productNameList.indexOf(i) ,element = "") //productNameList[productNameList.indexOf(i)])
                    priceIndex --
                    Log.i("Receipt2ImageView","$priceIndex!!!!!!!!!!!!!!!!!!!REDUCED PRICE!!!!!!!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!${priceList[priceIndex]}!!!!!2.6")
                }
                //
                i.contains("kg") -> {
                    //filteredPriceList[testProductList.indexOf(i) - 1] = ""
                    filteredPriceList.add(index = productNameList.indexOf(i) - 1,element = "")
                    priceIndex --
                    //filteredPriceList[testProductList.indexOf(i)] = priceList[priceIndex]
                    filteredPriceList.add(index = productNameList.indexOf(i),element = priceList[priceIndex])
                    priceIndex ++
                    Log.i("Receipt2ImageView","$priceIndex!!!!!!!!!!!!!!!!!!!@!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.6")
                }
                //
                i.length < 3 -> {
                    //filteredPriceList[testProductList.indexOf(i) - 1] = ""
                    filteredPriceList.add(index = productNameList.indexOf(i) - 1,element = "")
                    //filteredPriceList[testProductList.indexOf(i)] = priceList[priceIndex]
                    filteredPriceList.add(index = productNameList.indexOf(i),element = priceList[priceIndex])
                    priceIndex ++
                    Log.i("Receipt2ImageView","$priceIndex!!!!!!!!!!!!!!!!!!!length!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.6")
                }
                else -> {
                    //filteredPriceList[testProductList.indexOf(i)] = priceList[priceIndex]
                    filteredPriceList.add(index = productNameList.indexOf(i),element = priceList[priceIndex])
                    Log.i("Receipt2ImageView","$priceIndex!!!!!!!!!!!!!!!!!!!else!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!${priceList[priceIndex]}!!!!!!!!!!!2.6")
                    priceIndex ++

                }
            }
        }


        //tesiting
        for (i in productNameList) {
            Log.i("Receipt2ImageView","Produce${productNameList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.5")
        }

        for (i in filteredPriceList) {
            Log.i("Receipt2ImageView","Price${filteredPriceList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.6")
        }



        //priceList[testProductList.indexOf(i)]

       /* for (i in firstProductIndex .. lastProductIndex){
                products.add(Product(productList[i],priceList[i - firstProductIndex],2))
        }
        products.add(Product("Total",priceList[totalPriceIndex],2))

        for (i in products){
            Log.i("ImageViewFragments","${i.product}!!!!!!!!${i.price}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!5")
        }*/

        //"Clean" the created product names and price list , in order to avoid any errors


       /* applicationScope.launch {
            var sKey = 0
            var rKey = 0
            //create shop list
            val shops = listOf(Shop("Tesco"))
            /** Check if the shop is already in the database before adding, if there, make that the bKey*/
            //Insert into shops table
            shops.forEach { receiptDao.insertShop(it)}
            //get auto generated primary key from Shops table
            val bKey = receiptDao.getShopsId()
            bKey.forEach {
                sKey = it.shopId
            }
            //create receipt list
            val receipts = listOf(Receipt(sKey))
            //insert into receipt table
            receipts.forEach { receiptDao.insertReceipt(it)}
            //get auto generated primary key from receipt table
            val cKey = receiptDao.getReceiptWithID(sKey)
            cKey.forEach {
                rKey = it.receiptId
            }
            //Key to navigate to the detail receipt after using receipt
            rets = cKey
            //create product lists of receipt products
            for (i in firstProductIndex .. lastProductIndex){
                products.add(Product(productList[i],priceList[i - firstProductIndex],rKey))
            }
            products.add(Product("Total",priceList[totalPriceIndex],rKey))
            *//*for (i in priceList.indices){
                //products = listOf(Product(productNameList[i],priceList[i],rKey))
                products.add(Product(productNameList[i],priceList[i],rKey))
            }*//*
            //Insert into products table
            products.forEach { receiptDao.insertProduct(it)}
        }*/


        /*Log.i("ImageViewFragments","$firstIndexCount!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.0")
        Log.i("ImageViewFragments","${productList[firstEurIndex]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.1")
        Log.i("ImageViewFragments","$numOfEur!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.2")
        Log.i("ImageViewFragments","$numOfProductEur!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.3")
        Log.i("ImageViewFragments","${priceList[lastEurIndex]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.4")
        Log.i("ImageViewFragments","${productList[firstProductIndex]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.5")
        Log.i("ImageViewFragments","${productList[lastProductIndex]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.6")
        Log.i("ImageViewFragments","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.7")
        Log.i("ImageViewFragments","${priceList[totalPriceIndex]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!4.8")*/
    }



    fun aldiReceipt(){
        //
        for(i in productList){
            when{
                //Check for the end of the receipt
                  i.toUpperCase(Locale.ROOT).contains("GOODS")  -> {
                      //remove the first 5 characters from the string
                      var sans5 = String()
                      for (c in i.indices) {
                          if (c > 5) {
                              sans5 += i[c].toString()
                          }
                      }
                      //if any char is an 'O' or 'o' or 'S', substitute with a "0" or "5"
                      var totalPrice = String()
                      for (i in sans5.indices) {
                          totalPrice += when{
                              sans5[i] == 'O' || sans5[i] == 'o'->{
                                  "0"
                              }
                              sans5[i] == 'S'->{
                                  "5"
                              }else->{
                                  sans5[i].toString()
                              }
                          }
                      }
                      priceList.add(totalPrice)
                      break
                  }
                //if the first 4 char are digits and the string is longer than 7, then assume this to
                    //be a product item
                i.length > 3 && i[0].isDigit() && i[1].isDigit() && i[2].isDigit() && i[3].isDigit()
                        && i.length > 7 -> {
                    productNameList.add(i)
                }
                //If starts with a digit and ends with a character and also contains "."
                // or if the length is less and 7 and contains "." then assume
                // this to be a price item
                i[0].isDigit()  && i.contains('.') && i.length < 7
                        || i.length < 7 && i.contains('.') -> {
                    priceList.add(i)
                }
            }
        }
        //testing
        for (i in productNameList) {
            Log.i("Receipt2ImageView","Produce${productNameList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.5")
        }

        for (i in priceList) {
            Log.i("Receipt2ImageView","Price${priceList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.6")
        }
    }

    fun lidlReceipt(){
        var totalPrice = String()
        for (i in productList){
            //
            when{
                //If "Total" or "Debit" is encounter store appropriate item as final price
                    // and end loop
                i.toUpperCase(Locale.ROOT).contains("TOTAL") || i.toUpperCase(Locale.ROOT)
                    .contains("DEBIT")  -> {
                    if (i.toUpperCase(Locale.ROOT).contains("TOTAL")){
                    val ind = productList.indexOf(i) + 1
                    priceList.add( productList[ind])
                    break
                    }else{
                        val ind = productList.indexOf(i) - 1
                        priceList.add( productList[ind])
                        break
                    }
                }
                //if any of the below words and characters are encountered, ignore them
                i.contains("Scratch") || i.contains("Coupon") ||
                        i.contains("Multibuy") || i.contains("-") ||
                        i.contains("Item") ||i.contains("EUR") ||
                        i.contains("x") && i.contains(".") -> {}
                //if item has "." and is less than 7 in length, assume it to be an item price
                i.contains(".") && i.length < 7 -> {
                    priceList.add(i)
                }
                //otherwise, if this is not the first two item, then this is a product item
                productList.indexOf(i) != 0 && productList.indexOf(i) != 1 ->{
                    productNameList.add(i)
                }
            }
        }
        //testing
        for (i in productNameList) {
            Log.i("Receipt2ImageView","Produce${productNameList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.5")
        }

        for (i in priceList) {
            Log.i("Receipt2ImageView","Price${priceList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.6")
        }
    }

    //Navigate to the detailed receipt screen and pass the current receipt
    fun onGoToDetailViewClick() = viewModelScope.launch{
        Log.i("Receipt2ImageView","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!3.0")
        rets.forEach { receipt ->
            cameraEventChannel.send(CameraEvent.NavigateToReceiptDetailScreen(receipt))
        }
    }
    //Trigger change in icon when image processing is completed
    fun onImageProcessingCompleted() = viewModelScope.launch {
        cameraEventChannel.send(CameraEvent.imageProcessingCompleted)
    }

    //
    sealed class CameraEvent{
        //
        data class NavigateToReceiptDetailScreen(val receipt: Receipt) : CameraEvent()
        //
        //data class imageProcessingCompleted(val trigger: Boolean) : CameraEvent()
        object  imageProcessingCompleted : CameraEvent()
    }
}