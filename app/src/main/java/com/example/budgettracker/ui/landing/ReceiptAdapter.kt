package com.example.budgettracker.ui.landing

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.R
import com.example.budgettracker.data.ShopsWithReceipts
import com.example.budgettracker.databinding.ItemReceiptBinding


//Adapter to handle a list of receipts
class ReceiptAdapter(private val listener: onItemClickListener) : ListAdapter<ShopsWithReceipts, ReceiptAdapter.ReceiptViewHolder>(DiffCallback()){


    //Where recyclerview can get new items in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        //create the needed binding object for the layout inflater
        val binding = ItemReceiptBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ReceiptViewHolder(binding)
    }

    //Define how we bind the data
    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        //Reference to the current position item
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class ReceiptViewHolder(private val binding: ItemReceiptBinding) : RecyclerView.ViewHolder(binding.root){
        //forward to onItemClickListener
        init {
            //reference to views
            binding.apply {
                root.setOnClickListener {
                    //get position of item that was clicked
                    val position = adapterPosition
                    //check that the item  clicked is still valid and not -1
                    if(position != RecyclerView.NO_POSITION){
                        val shopsWithReceipts = getItem(position)
                        listener.onItemClick(shopsWithReceipts)
                    }
                }
            }
        }
        //
        fun bind(shopsWithReceipts: ShopsWithReceipts){
            binding.apply {
                //display the current store name
                storeName.text = shopsWithReceipts.shop.shopName
                //display the date the receipt was created
                shopsWithReceipts.receipt.forEach {
                    receiptDate.text = it.receipt.createdDateFormatted
                }
                //products total
                var test = String()
                shopsWithReceipts.receipt.forEach { it ->
                    //add all the product prices in the price list
                    it.product.forEach {
                        test = it.price
                    }
                }
                //
                totalPrice.text = test

            }
        }
    }
    //Click listener for items on the landing page
    interface onItemClickListener{
        fun onItemClick(shopsWithReceipts: ShopsWithReceipts)
    }

    //to enable the ListAdapter to compare list items
    class DiffCallback : DiffUtil.ItemCallback<ShopsWithReceipts>(){
        //uniquely identify an item
        override fun areItemsTheSame(oldItem: ShopsWithReceipts, newItem: ShopsWithReceipts) =
            oldItem.receipt == newItem.receipt

        //inform callback when item in our list has changed
        override fun areContentsTheSame(oldItem: ShopsWithReceipts, newItem: ShopsWithReceipts)  =
            oldItem == newItem
    }

}
