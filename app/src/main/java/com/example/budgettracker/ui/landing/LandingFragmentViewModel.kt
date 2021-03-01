package com.example.budgettracker.ui.landing

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.budgettracker.data.ReceiptDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class LandingFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao
) : ViewModel() {

    //get receipt data from the database
    //val receipt = receiptDao.getReceipt().asLiveData()
    //get data from our database
    val shopsWithReceipt = receiptDao.getShopsWithReceipts().asLiveData()

    //Create a state flow for the sort order
    //By default the receipt will be sorted by date
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)


    //
   /* private val receiptFlow = combine(
        sortOrder
    ){ sortOrder ->
     (sortOrder)
    }.flatMapLatest { (sortOrder)
        receiptDao.getShopsWithReceipts(it)
    }*/



}

//
enum class SortOrder{ BY_STORE, BY_DATE}
