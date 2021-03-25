package com.example.budgettracker.ui.landing

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
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
    @ApplicationScope private val applicationScope: CoroutineScope,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {


    //Use channels to send data between two coroutines
    private val receiptEventChannel = Channel<ReceiptEvent>()
    val receiptEvent = receiptEventChannel.receiveAsFlow()

    //Create a state flow for the sort order
    //By default the receipt will be sorted by date
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)

    //retrieve argument
    val storeCode = state.get<Int>("storeCode")

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

    //write all receipts in the database to a csv file
    fun exportReceiptDBToCSVFile(csvFile : File){
        var shopsWithReceipts: List<ShopsWithReceipts>
        //done on background trend because we call a suspend function
        applicationScope.launch {
            shopsWithReceipts = receiptDao.getShopsWithReceipts()
            csvWriter().open(csvFile, append = false) {
                // Header
                writeRow(listOf("[id]", "[Shops]","[Products]","[Price]"))
                //Body distinguished by receipt
                shopsWithReceipts.forEachIndexed { index, shopsWithReceipts ->
                    val shopName = shopsWithReceipts.shop.shopName
                    shopsWithReceipts.receipt.forEach { receipt ->
                        val date = receipt.receipt.createdDateFormatted
                        writeRow(listOf(index,shopName,date))
                        receipt.product.forEach { product ->
                            writeRow(listOf("","",product.product,product.price))
                        }
                    }
                }
            }
        }
    }
    //Representation of the different events to send to the fragments
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


