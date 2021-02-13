package com.example.budgettracker.ui.landing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.data.Receipt
import com.example.budgettracker.databinding.ItemReceiptBinding

class ReceiptAdapter : ListAdapter<Receipt, ReceiptAdapter.ReceiptViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        //create the needed binding object for the layout inflater
        val binding = ItemReceiptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        //Reference to the current position item
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    /*
        * Create the viewholder and binding for the item receipt view
        */
    class ReceiptViewHolder(private val binding: ItemReceiptBinding) : RecyclerView.ViewHolder(binding.root){

        //function to put data into the views in layout
        fun bind(receipt: Receipt){
            //specifies which data to put into the view of the item receipt
            binding.apply {
                storeName.text = receipt.shopName
                totalPrice.text = receipt.price.toString()
            }
        }

    }

    //to enable the ListAdapter compare list items
    class DiffCallback : DiffUtil.ItemCallback<Receipt>(){
        //uniquely compare items
        override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt)  =
             oldItem == newItem

    }

}