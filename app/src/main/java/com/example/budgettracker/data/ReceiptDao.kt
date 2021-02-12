package com.example.budgettracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface ReceiptDao {


    /**Retrieve data from the database using SQLLite queries.
     * Also define a flow of List of Receipt
     */
    @Query("SELECT * FROM receipt_table")
    fun getReceipt(): Flow<List<Receipt>>


    /**Insert into the database and define action to take if we
     * try to insert a receipt that has the same id an already
     * existing receipt.
     * In this case we will replace the receipt.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: Receipt)

    /**Update the database*/
    @Update
    suspend fun update(receipt: Receipt)

    /**Delete from the database*/
    @Delete
    suspend fun delete(receipt: Receipt)

}