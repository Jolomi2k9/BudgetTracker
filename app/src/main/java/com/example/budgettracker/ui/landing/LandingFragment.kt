package com.example.budgettracker.ui.landing

import android.os.Bundle
import android.view.*
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

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        //button to navigate to the camera fragment and scan new receipt data
        binding.fabAddReceipt.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_landingFragment_to_cameraFragment)
        }

        //observes the receipt database
        viewModel.receipt.observe(viewLifecycleOwner) {
            //
            receiptAdapter.submitList(it)
        }

    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding to the view
        val binding = FragmentLandingBinding.bind(view)
        //get an instance of the receipt adapter
        val receiptAdapter = ReceiptAdapter()
        //binding our receipts
        binding.apply {
            recyclerViewLanding.apply {
                //set the adapter of the recyclerviewlanding
                adapter = receiptAdapter
                //specify how layout manager should layout the items on screen
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        //button to navigate to the camera fragment and scan new receipt data
        binding.fabAddReceipt.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_landingFragment_to_imageViewFragment)
        }
        //observes our livedata in the receipt database
        viewModel.shopsWithReceipt.observe(viewLifecycleOwner){
            //whenever something in the database changes, the adapter is updated
            receiptAdapter.submitList(it)
        }

        //Activate the options menu in fragment
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_receipt,menu)


    }

    //Define action to take when a menu item is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //identify the item that was clicked
        return when(item.itemId){
            R.id.action_sort_by_store -> {
              //action to take when item clicked
                viewModel.sortOrder.value = SortOrder.BY_STORE
               true
            }
            //
            R.id.action_sort_by_date_created -> {
                //action to take when item clicked
                viewModel.sortOrder.value = SortOrder.BY_DATE
                true
            }
            R.id.action_export_receipts -> {

                true
            }
            //return false to indicate the click was not handled
            else -> super.onOptionsItemSelected(item)
        }

    }


}