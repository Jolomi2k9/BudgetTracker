package com.example.budgettracker.ui.receipts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.data.Product
import com.example.budgettracker.data.ShopsWithReceipts
import com.example.budgettracker.databinding.DetailedItemsBinding
import com.example.budgettracker.databinding.ItemReceiptBinding
import com.example.budgettracker.ui.landing.ReceiptAdapter

class ReceiptDetailAdapter  : ListAdapter<Product, ReceiptDetailAdapter.ReceiptDetailViewHolder>(DiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptDetailViewHolder {
        //create the needed binding object for the layout inflater
        val binding = DetailedItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiptDetailViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ReceiptDetailViewHolder, position: Int) {
        //Reference to the current position item
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }
    class ReceiptDetailViewHolder(private val binding: DetailedItemsBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(product: Product){
            binding.apply {
                itemName.text = product.product
                itemPrice.text = product.price
            }
        }
    }
    //to enable the ListAdapter to compare list items
    class DiffCallback : DiffUtil.ItemCallback<Product>(){
        //uniquely compare items
        override fun areItemsTheSame(oldItem: Product, newItem: Product) =
            oldItem.productID == newItem.productID

        override fun areContentsTheSame(oldItem: Product, newItem: Product)  =
            oldItem == newItem
    }
}