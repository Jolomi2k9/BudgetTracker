package com.example.budgettracker.ui.receipts

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.budgettracker.data.Receipt
import com.example.budgettracker.data.ReceiptDao

class ReceiptFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel(){

    //retrieve argument
    val receipt = state.get<Receipt>("receipt")


    //retrieve the receipt Id from the saved instance state
    var receiptID = state.get<Int>("receiptID") ?: receipt?.receiptId ?: 0

    //using the retrieved receipt id get all the products associated with the receipt
    //from the product database
    val product = receiptDao.getProductWithID(receiptID).asLiveData()
}