package com.example.budgettracker.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentLandingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing.*

@AndroidEntryPoint
class LandingFragment : Fragment(R.layout.fragment_landing) {

    private val viewModel: LandingFragmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding to the view
        val binding = FragmentLandingBinding.bind(view)
        //instance of the receipt adapter
        val receiptAdapter = ReceiptAdapter()
        //binding our receipts
        binding.apply {
            recyclerViewLanding.apply {
                //set the adapter of the recyclerviewlanding
                adapter = receiptAdapter
                //
                layoutManager = LinearLayoutManager(requireContext())

            }
        }

        //button to navigate to the camera fragment and add new receipt data
        binding.fabAddReceipt.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_landingFragment_to_cameraFragment)
        }

        //observes the receipt database
        viewModel.receipt.observe(viewLifecycleOwner) {
            //
            receiptAdapter.submitList(it)
        }

    }


}