package com.example.budgettracker.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.*
import com.example.budgettracker.di.ApplicationScope
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
    //Used to pass receipt id between fragments
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
            }
        }
        //Identify which receipt has been scanned and call the associated function or display
        //an error message
        for (i in productList) {
            when{
                i.toUpperCase(Locale.ROOT) == "TESCO" || i.toUpperCase(Locale.ROOT) == "TESC" -> {
                    onStoreNotSupported()
                    break
                }
                i.toUpperCase(Locale.ROOT).contains("ALDI") -> {
                    aldiReceipt()
                    break
                }
                i.toUpperCase(Locale.ROOT).contains("LODL") ||
                        i.toUpperCase(Locale.ROOT).contains("LDL") ||
                        i.toUpperCase(Locale.ROOT).contains("LIDL")-> {
                    lidlReceipt()
                    break
                }else -> {
                    //If no receipt is detected
                    onNoReceiptDetected()
                }

            }

        }
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

        val numOfEur = priceList.size
        val numOfProductEur = numOfEur - 2
        val lastEurIndex = numOfEur  - 3
        var firstProductIndex = 2 //firstEurIndex - numOfProductEur
        val lastProductIndex = firstEurIndex - 1
        //val totalIndex = lastEurIndex + 1
        val totalPriceIndex = numOfEur - 2
        //
        /*if(plusTrigger){
            firstProductIndex --
        }*/
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

    }



    fun aldiReceipt(){
        var endTag = 0
        //
        for(i in productList){
            when{
                //Check for the end of the receipt
                  i.toUpperCase(Locale.ROOT).contains("GOODS")  -> {
                      //indicate that an end tag was encountered
                      endTag ++
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

        //Check for errors in Detected data and write to the database or display an error message
        errorCheck(endTag,"ALDI")

        //testing
        for (i in productNameList) {
            Log.i("Receipt2ImageView","Produce${productNameList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.5")
        }

        for (i in priceList) {
            Log.i("Receipt2ImageView","Price${priceList.size}!!!!!!!!!!!!!!!!!$i!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.7")
        }
        //Write data to database
        //writeToDatabase("ALDI")
    }

    fun lidlReceipt(){
        var totalPrice = String()
        var endTag = 0
        for (i in productList){
            //
            when{
                //If "Total" or "Debit" is encounter store appropriate item as final price
                    // and end loop
                i.toUpperCase(Locale.ROOT).contains("TOTAL") || i.toUpperCase(Locale.ROOT)
                    .contains("DEBIT")  -> {
                    //indicate that an end tag was encountered
                    endTag ++
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


        //Check for errors in Detected data and write to the database or display an error message
        errorCheck(endTag,"LIDL")

        //Write data to database
        //writeToDatabase("LIDL")
    }

    //Check for errors in the detected data
    fun errorCheck(endTag: Int, storeName: String) {
        //Check that an end tag was encountered and the productName size does not exceed that of
        //the pricelist
        when {
            endTag == 0 -> {
                onIncompleteDataReceived()
            }
            productNameList.size == priceList.size || productNameList.size > priceList.size -> {
                onIncompleteDataReceived()
            }else -> {
            //If there are no errors, write to the database
            writeToDatabase(storeName)
        }
        }
    }

    //write detected data to the database
    fun writeToDatabase(storeName: String){
        //
         applicationScope.launch {
           var sKey = 0
           var rKey = 0
           //create shop list
           val shops = listOf(Shop(storeName))
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
             Log.i("Receipt2ImageView","Price${priceList.size}!!!!!!!!!!!!!!!!!!!!!!!!${priceList[productNameList.size]}!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2.6")
             for(i in productNameList.indices){
               products.add(Product(productNameList[i],priceList[i],rKey))
             }
             products.add(Product("Total",priceList[productNameList.size],rKey))
            //Insert into products table
            products.forEach { receiptDao.insertProduct(it)}
        }

    }



    //Navigate to the detailed receipt screen and pass the current receipt
    fun onGoToDetailViewClick() = viewModelScope.launch{
        rets.forEach { receipt ->
            cameraEventChannel.send(CameraEvent.NavigateToReceiptDetailScreen(receipt))
        }
    }
    //Trigger change in icon when image processing is completed
    fun onImageProcessingCompleted() = viewModelScope.launch {
        cameraEventChannel.send(CameraEvent.ImageProcessingCompleted)
    }
    //Trigger to change button if no supported receipt is detected
    fun onNoReceiptDetected() = viewModelScope.launch {
        cameraEventChannel.send(CameraEvent.NoSupportedReceiptDetected)
    }
    //Trigger to change button if no supported receipt is detected
    fun onIncompleteDataReceived() = viewModelScope.launch {
        cameraEventChannel.send(CameraEvent.IncompleteDataReceived)
    }
    //Trigger to change button if no supported receipt is detected
    fun onStoreNotSupported() = viewModelScope.launch {
        cameraEventChannel.send(CameraEvent.StoreNotSupported)
    }
    //Events
    sealed class CameraEvent{
        //Event to navigate to the detailed receipt screen
        data class NavigateToReceiptDetailScreen(val receipt: Receipt) : CameraEvent()
        //Event triggered when image processing is completed
        object  ImageProcessingCompleted : CameraEvent()
        //Event triggered if no supported receipt is detected
        object NoSupportedReceiptDetected : CameraEvent()
        //Event triggered when captured data is incomplete or defective
        object IncompleteDataReceived : CameraEvent()
        //Event triggered when a particular store is not currently supported
        object StoreNotSupported : CameraEvent()
    }
}