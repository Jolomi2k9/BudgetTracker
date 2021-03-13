package com.example.budgettracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface ReceiptDao {


    /*
     * Retrieve data from the database using SQLLite queries.
     * Also define a flow of List of Receipt
     */
    @Query("SELECT * FROM receipt_table")
    fun getReceipt(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipt_table")
    fun  getReceiptId() : List<Receipt>


    //Retrieve all data in the product table
    @Query("SELECT * FROM product_table")
    fun getProduct(): Flow<List<Product>>

    //
    @Transaction
    @Query("SELECT * FROM shop_table")
    fun  getShopsWithReceipts() : Flow<List<ShopsWithReceipts>>


    @Query("SELECT * FROM shop_table")
    fun  getShopsId() : List<Shop>


    /*Insert into the database and define action to take if we
     * try to insert a receipt that has the same id an already
     * existing receipt.
     * In this case we will replace the receipt.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: Shop)
    //into the receipt table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt)
    //into the product table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)




    /*Delete from the database*/
    @Delete
    suspend fun delete(receipt: Receipt)




}