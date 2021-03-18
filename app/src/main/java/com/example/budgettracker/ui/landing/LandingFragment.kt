package com.example.budgettracker.ui.landing

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.R
import com.example.budgettracker.data.ShopsWithReceipts
import com.example.budgettracker.databinding.FragmentLandingBinding
import com.example.budgettracker.util.exhaustive
import com.example.budgettracker.util.generateFile
import com.example.budgettracker.util.goToFileIntent
import com.example.budgettracker.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


@AndroidEntryPoint
class LandingFragment : Fragment(R.layout.fragment_landing), ReceiptAdapter.onItemClickListener{

    private lateinit var safeContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    private val viewModel: LandingFragmentViewModel by viewModels()

    //create a csv file name
    private val CSVFileName : String = "ReceiptProducts.csv"
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        //binding to the view
        val binding = FragmentLandingBinding.bind(view)
        //get an instance of the receipt adapter
        val receiptAdapter = ReceiptAdapter(this)
        //binding our receipts
        binding.apply {
            recyclerViewLanding.apply {
                //set the adapter of the recyclerviewlanding
                adapter = receiptAdapter
                //specify how layout manager should layout the items on screen
                layoutManager = LinearLayoutManager(requireContext())
                //Implementing swipe to delete functionality
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                    0,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        //up and down functionality(drag and drop) not supported
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        //swipe to delete
                        val receipt = receiptAdapter.currentList[viewHolder.adapterPosition]
                        viewModel.onReceiptSwiped(receipt)
                    }
                }).attachToRecyclerView(recyclerViewLanding)
                //navigate to cameraFragment and scan new receipt
                fabAddReceipt.setOnClickListener {
                    viewModel.onAddNewReceiptClick()
                }

            }
        }
        //button to navigate to the camera fragment and scan new receipt data
        /*binding.fabAddReceipt.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_landingFragment_to_imageViewFragment)
        }*/



        //observes our livedata in the receipt database
        viewModel.shopsWithReceipt.observe(viewLifecycleOwner){
            //whenever something in the database changes, the adapter is updated
            receiptAdapter.submitList(it)
        }
        //define scope so as to be cancelled when onStop is called and restarted when
        //onStart is called
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.receiptEvent.collect { event ->
                //Turn this into an expression in order to get compile time safety
                when(event){
                    //specify when to display message
                    is LandingFragmentViewModel.ReceiptEvent.ShowDeleteReceiptMessage -> {
                        //display Snack bar message
                        Snackbar.make(requireView(), "Receipt deleted!", Snackbar.LENGTH_LONG)
                            .show()
                    }
                    is LandingFragmentViewModel.ReceiptEvent.NavigateToAddNewReceiptScreen -> {
                        //navigate to the camera with compile time safety
                        val action =
                            LandingFragmentDirections.actionLandingFragmentToImageViewFragment()
                        findNavController().navigate(action)
                    }
                    is LandingFragmentViewModel.ReceiptEvent.NavigateToReceiptDetailScreen -> {
                        //navigate to detailed receipt screen and pass the receipt, also with compile time safety
                        event.shopsWithReceipts.receipt.forEach {
                            val action =
                                LandingFragmentDirections.actionLandingFragmentToReceiptFragment(
                                    it.receipt
                                )
                            findNavController().navigate(action)
                        }
                    }
                }.exhaustive
            }
        }
        //Activate the options menu in fragment
        setHasOptionsMenu(true)
    }
    override fun onItemClick(shopsWithReceipts: ShopsWithReceipts) {
        //delegate to viewmodel
        viewModel.onReceiptSelected(shopsWithReceipts)

    }
    //
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater){
        inflater.inflate(R.menu.menu_fragment_receipt, menu)
        //reference to the search view
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        //call onQueryTextChanged from ViewExt file to handle queries
        searchView.onQueryTextChanged {
            //pass on queried text
            viewModel.searchQuery.value = it
        }
    }
    //Define action to take when a menu item is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean{
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
                Log.i("Receipt","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!0")
                val csvFile = generateFile(requireContext(), CSVFileName)
                //export the receipts
                if (csvFile != null) {
                    //
                    Log.i("Receipt","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
                    viewModel.exportReceiptDBToCSVFile(csvFile)
                    Toast.makeText(safeContext, "CSV file generated!", Toast.LENGTH_LONG).show()
                    val intent = goToFileIntent(requireContext(), csvFile)
                    startActivity(intent)
                } else {
                    Toast.makeText(safeContext, "CSV file was not generated", Toast.LENGTH_LONG).show()
                }
                true
            }
            //return false to indicate the click was not handled
            else -> super.onOptionsItemSelected(item)
        }
    }
}