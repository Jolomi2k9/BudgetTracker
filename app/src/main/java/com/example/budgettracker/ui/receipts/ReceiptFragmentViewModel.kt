package com.example.budgettracker.ui.receipts

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.budgettracker.data.Product
import com.example.budgettracker.data.Receipt
import com.example.budgettracker.data.ReceiptDao

class ReceiptFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel(){

    //retrieve argument
    val receipt = state.get<Receipt>("receipt")
    val product = state.get<Product>("product")

    //retrieve receipt id value from the saved instance state
    var receiptId = state.get<Int>("receiptId") ?: receipt?.receiptId ?: 0
    //store the data in the saved instance state
        set(value) {
            field = value
            state.set("receiptId",value)
        }
    //retrieve product id value from the saved instance state
    var productID = state.get<Int>("productID") ?: product?.productID ?: 0
        set(value) {
            field = value
            state.set("productID",value)
        }

}