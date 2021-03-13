package com.example.budgettracker.ui.landing

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.ReceiptDao
import com.example.budgettracker.data.ShopsWithReceipts
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LandingFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao
) : ViewModel() {

    //
    private val receiptEventChannel = Channel<ReceiptEvent>()
    val receiptEvent = receiptEventChannel.receiveAsFlow()

    //get receipt data from the database
    //val receipt = receiptDao.getReceipt().asLiveData()
    //get data from our database
    val shopsWithReceipt = receiptDao.getShopsWithReceipts().asLiveData()

    //Create a state flow for the sort order
    //By default the receipt will be sorted by date
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)

    //
    fun onReceiptSelected(shopsWithReceipts: ShopsWithReceipts){

    }

    fun onReceiptSwiped(shopsWithReceipts: ShopsWithReceipts) = viewModelScope.launch {
        receiptDao.delete(shopsWithReceipts.receipt[0].receipt)
        //receiptDao.delete(shopsWithReceipts.receipt.forEach {it.receipt})
        //undo the delete
        receiptEventChannel.send(ReceiptEvent.ShowDeleteReceiptMessage(shopsWithReceipts))
    }


    //
    sealed class ReceiptEvent{
        data class ShowDeleteReceiptMessage(val shopsWithReceipts: ShopsWithReceipts) : ReceiptEvent()
    }

}

//
enum class SortOrder{ BY_STORE, BY_DATE}
