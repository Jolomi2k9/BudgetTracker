package com.example.budgettracker.ui.receipts

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgettracker.R
import com.example.budgettracker.camera.CameraFragmentViewModel
import com.example.budgettracker.camera.ImageViewFragmentDirections
import com.example.budgettracker.databinding.FragmentReceiptBinding
import com.example.budgettracker.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

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
            fabReturnHome.setOnClickListener {
                viewModel.onGoHomeClick()
            }
        }
        //get the products from the view model and send it to the receiptDetailAdapter
        viewModel.product.observe(viewLifecycleOwner){
            receiptDetailAdapter.submitList(it)
        }

        //define scope so as to be cancelled when onStop is called and restarted when
        //onStart is called
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect { event ->
                Log.i("ReceiptFragment","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!7")
                when (event) {
                    //Navigate to the home screen
                    is ReceiptFragmentViewModel.HomeEvent.NavigateToHomeScreen -> {
                        Log.i("ReceiptFragment","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!8")
                            val action =
                                ReceiptFragmentDirections.actionReceiptFragmentToLandingFragment(
                                )
                        findNavController().navigate(action)
                    }
                    is ReceiptFragmentViewModel.HomeEvent.NavigateHomeWithArgument ->{
                        Log.i("ReceiptFragment","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!9")
                        val action =
                            ReceiptFragmentDirections.actionReceiptFragmentToLandingFragment(event.shopCode)
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

    }
}