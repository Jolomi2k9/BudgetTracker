package com.example.budgettracker.ui.landing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.data.Receipt
import com.example.budgettracker.data.ReceiptsWithProducts
import com.example.budgettracker.data.ShopsWithReceipts
import com.example.budgettracker.databinding.ItemReceiptBinding

/*class ReceiptAdapter : ListAdapter<Receipt,ReceiptAdapter.ReceiptViewHolder>(DiffCallback()) {

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
        // Create the viewholder and binding for the item receipt view


    class ReceiptViewHolder(private val binding: ItemReceiptBinding) : RecyclerView.ViewHolder(binding.root){

        //function to put data into the views in layout
        fun bind(receipt: Receipt){
            //specifies which data to put into the view of the item receipt
            *//*binding.apply {
                storeName.text = receipt.shopName
                totalPrice.text = receipt.price.toString()
                receiptDate.text = receipt.createdDateFormatted



            }*//*
        }
    }
    //to enable the ListAdapter to compare list items
    class DiffCallback : DiffUtil.ItemCallback<Receipt>(){
        //uniquely compare items
        override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt) =
            oldItem.receiptId == newItem.receiptId

        override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt)  =
             oldItem == newItem
    }

}*/
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
                //val swr = shopsWithReceipts.receipt[0]

                storeName.text = shopsWithReceipts.shop.shopName
                //shopsWithReceipts.receipt.size
                //receiptDate.text = shopsWithReceipts.receipt[1].receipt.createdDateFormatted
                //totalPrice.text = shopsWithReceipts.receipt[0].product[0].price.toString()
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
