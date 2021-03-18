package com.example.budgettracker.ui.landing

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.RoomMasterTable.TABLE_NAME
import com.example.budgettracker.data.Product
import com.example.budgettracker.data.ReceiptDao
import com.example.budgettracker.data.Shop
import com.example.budgettracker.data.ShopsWithReceipts
import com.example.budgettracker.di.ApplicationScope
import com.example.budgettracker.util.generateFile
import com.example.budgettracker.util.goToFileIntent
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class LandingFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {


    //
    private val receiptEventChannel = Channel<ReceiptEvent>()
    val receiptEvent = receiptEventChannel.receiveAsFlow()

    //Create a state flow for the sort order
    //By default the receipt will be sorted by date
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)

    //create a csv file name
    private val CSVFileName : String = "ReceiptProducts.csv"

    //search query
    val searchQuery = MutableStateFlow("")
    //

    //translate into an sqlLite query.
    //combine and pass all the latest values
    private val receiptFlow = combine(
        searchQuery,
        sortOrder
    ){query, sortOrder ->
        Pair(query,sortOrder)
    }.flatMapLatest {(query,sortOrder) ->
        receiptDao.getShopsWithReceipts(query,sortOrder)
    }

    //get receipt data from the database
    //val receipt = receiptDao.getReceipt().asLiveData()
    //get data from our database
    val shopsWithReceipt = receiptFlow.asLiveData()


    //Navigate to detailed receipt view when item on landing page is clicked
    fun onReceiptSelected(shopsWithReceipts: ShopsWithReceipts) = viewModelScope.launch{
        receiptEventChannel.send(ReceiptEvent.NavigateToReceiptDetailScreen(shopsWithReceipts))
    }

    fun onReceiptSwiped(shopsWithReceipts: ShopsWithReceipts) = viewModelScope.launch {
        //Delete selected item
        receiptDao.delete(shopsWithReceipts.shop)
        //Show "receipt deleted" message
        receiptEventChannel.send(ReceiptEvent.ShowDeleteReceiptMessage(shopsWithReceipts))
    }

    //handle click for the add new receipt button
    fun onAddNewReceiptClick() = viewModelScope.launch {
        //send this event to channel
        //this emits event from the view model so the fragment can listen and take appropriate action
        receiptEventChannel.send(ReceiptEvent.NavigateToAddNewReceiptScreen)
    }

    fun exportReceiptDBToCSVFile(csvFile : File){
        //
        var product = listOf<Product>()
        applicationScope.launch {
             product = receiptDao.getProduct()
        }
        Log.i("Receipt","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2")
        csvWriter().open(csvFile, append = false) {
            // Header
            writeRow(listOf("[id]", "Shops"))
            //
            /*product.forEachIndexed { index, shop ->
                writeRow(listOf(index,shop.shopName))
            }*/
            product.forEachIndexed { index, product ->
                writeRow(listOf(index,product.product))
                Log.i("Receipt","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!3")
            }

        }

    }

    //For event handling
    sealed class ReceiptEvent{
        //create a single instance of to navigate to camera fragment
        object NavigateToAddNewReceiptScreen : ReceiptEvent()
        //Navigate to receipt detail view
        //data class NavigateToReceiptDetailScreen(val shopsWithReceipts: ShopsWithReceipts) : ReceiptEvent()
        data class NavigateToReceiptDetailScreen(val shopsWithReceipts: ShopsWithReceipts) : ReceiptEvent()
        //for displaying delete message
        data class ShowDeleteReceiptMessage(val shopsWithReceipts: ShopsWithReceipts) : ReceiptEvent()
    }




}

//class used in sorting the database
enum class SortOrder{ BY_STORE, BY_DATE}
