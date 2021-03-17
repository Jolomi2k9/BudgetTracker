package com.example.budgettracker.data

import androidx.room.*
import com.example.budgettracker.ui.landing.SortOrder
import kotlinx.coroutines.flow.Flow


@Dao
interface ReceiptDao {


    /*
     * Retrieve data from the database using SQLLite queries.
     * Also define a flow of List of Receipt
     */

    //specify the get queries based on sort order
    fun getShopsWithReceipts(query: String, sortOrder: SortOrder): Flow<List<ShopsWithReceipts>> =
        when (sortOrder){
            SortOrder.BY_DATE -> getShopsWithReceiptsSortedByDateCreated(query)
            SortOrder.BY_STORE -> getShopsWithReceiptsSortedByStoreName(query)
        }
    @Query("SELECT * FROM receipt_table")
    fun getReceipt(): Flow<List<Receipt>>
    //get all receipt in the receipt table
    @Query("SELECT * FROM receipt_table")
    fun  getReceiptId() : List<Receipt>


    //Retrieve all data in the product table
    @Query("SELECT * FROM product_table WHERE productReceiptId = :rKey")
    fun getProduct(rKey: Int): Flow<List<Product>>

    //Get the receipt ordered by shop name
    @Transaction
    @Query("SELECT * FROM shop_table WHERE shopName LIKE '%' || :searchQuery || '%' ORDER BY shopName")
    fun  getShopsWithReceiptsSortedByStoreName(searchQuery: String) : Flow<List<ShopsWithReceipts>>

    //Get the receipt ordered by the date it was created
    @Transaction
    @Query("SELECT * FROM shop_table WHERE shopName LIKE '%' || :searchQuery || '%' ")
    fun  getShopsWithReceiptsSortedByDateCreated(searchQuery: String) : Flow<List<ShopsWithReceipts>>

    //get all shops in the shop table
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
    suspend fun delete(shop: Shop)




}