package com.example.budgettracker.ui.landing

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.budgettracker.data.ReceiptDao

class LandingFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao
) : ViewModel() {

    //get receipt data from the database
    val receipt = receiptDao.getReceipt().asLiveData()

}