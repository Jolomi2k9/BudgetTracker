package com.example.budgettracker.ui.landing

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.budgettracker.data.ReceiptDao

class LandingFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao
) : ViewModel() {
}