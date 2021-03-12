package com.example.budgettracker.ui.receipts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentReceiptBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptFragment : Fragment(R.layout.fragment_receipt){

    private val viewModel: ReceiptFragmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding to the view
        val binding = FragmentReceiptBinding.bind(view)
        //instance of the receipt detail adapter
        val receiptDetailAdapter = ReceiptDetailAdapter()


        binding.apply {
            recyclerViewReceipt.apply {
                //set the adapter of the recycler View Receipt
                adapter = receiptDetailAdapter
                //specify how layout manager should layout the items on screen
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

    }
}