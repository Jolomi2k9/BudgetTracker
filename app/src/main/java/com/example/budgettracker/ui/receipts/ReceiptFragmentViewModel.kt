package com.example.budgettracker.ui.receipts

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.camera.CameraFragmentViewModel
import com.example.budgettracker.data.Receipt
import com.example.budgettracker.data.ReceiptDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ReceiptFragmentViewModel @ViewModelInject constructor(
    private val receiptDao: ReceiptDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel(){

    //Use channels to send data between two coroutines
    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvent = homeEventChannel.receiveAsFlow()


    //retrieve argument
    val receipt = state.get<Receipt>("receipt")


    //retrieve the receipt Id from the saved instance state
    var receiptID = state.get<Int>("receiptID") ?: receipt?.receiptId ?: 0

    //using the retrieved receipt id get all the products associated with the receipt
    //from the product database
    val product = receiptDao.getProductWithID(receiptID).asLiveData()

    //Navigate to the landing fragment
    fun onGoHomeClick() = viewModelScope.launch{
        //send this event to channel
        homeEventChannel.send(HomeEvent.NavigateToHomeScreen)
    }

    sealed class HomeEvent{
        //create a single instance of to navigate to camera fragment
        object NavigateToHomeScreen : HomeEvent()
    }
}