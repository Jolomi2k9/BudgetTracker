package com.example.budgettracker.data

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgettracker.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Flow
import javax.inject.Inject
import javax.inject.Provider

/**
 * Trigger Room code generation by annotating class.
 * Pass in all our tables.
 * We use version to update the database if we change the
 * schema.
 */
@Database(entities = [Receipt::class,
                        Shop::class,
                        Product::class,
                     ],version = 1)
/**
 * Make class abstract because Room will generate all the necessary code
 * and implementation for the class
 */
abstract class ReceiptDatabase : RoomDatabase(){

    /**Use this abstract function to
     * get to out Receipt Dao which can then be used to perform
     * database operations
     */
    abstract fun receiptDao(): ReceiptDao

   /* * @Inject enables dagger how to create an instance of the class below
    * We make dagger to not instantiate the database when
    * the below callback is created to avoid a circular dependency, but will do so
    * only when the onCreate() method below is executed, which happens AFTER
    * the build() method in AppModule.kt has finished.*/
    class Callback @Inject constructor(
        //use provider to avoid a circular dependency
        private val database: Provider<ReceiptDatabase>,
        //
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback(){
        //executed the first time the database is created
        override fun onCreate(db: SupportSQLiteDatabase){
            super.onCreate(db)
            //
            val dao = database.get().receiptDao()


            // we use this coroutine to run suspends functions,
            //which can only be run by coroutines
            applicationScope.launch {
                //Log.i("Receipt","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
                var sKey = 0
                var rKey = 0
                //create shop list
                val shops = listOf(Shop("Tesco18"))
                //Insert into shops table
                shops.forEach { dao.insertShop(it)}
                //get auto generated primary key from Shops table
                val bKey = dao.getShopsId()
                bKey.forEach {
                    sKey = it.shopId
                }
                //create receipt list
                val receipts = listOf(Receipt(sKey))
                //insert into receipt table
                receipts.forEach { dao.insertReceipt(it)}
                //get auto generated primary key from receipt table
                val cKey = dao.getReceiptId()
                cKey.forEach {
                    rKey = it.receiptId
                }
                //create product lists of receipt products
                val products = listOf(
                    Product("CKD CHICKEN","3.50",rKey),
                    Product("COCOA POWDER","1.79",rKey),
                    Product("MILK CHOCOLATE","1.09",rKey),
                    Product("FRESH PROTEIN","1.89",rKey),
                    Product("TESCO TEABAGS","2.50",rKey),
                    Product("PEELER","1.99",rKey)
                )
                //Insert into products table
                products.forEach { dao.insertProduct(it)}
            }

            applicationScope.launch {
                var sKey = 0
                var rKey = 0
                //create shop list
                val shops = listOf(Shop("Dunnes"))
                //Insert into shops table
                shops.forEach { dao.insertShop(it)}
                //get auto generated primary key from Shops table
                val bKey = dao.getShopsId()
                bKey.forEach {
                    sKey = it.shopId
                }
                //create receipt list
                val receipts = listOf(Receipt(sKey))
                //insert into receipt table
                receipts.forEach { dao.insertReceipt(it)}
                //get auto generated primary key from receipt table
                val cKey = dao.getReceiptId()
                cKey.forEach {
                    rKey = it.receiptId
                }
                //create product lists of receipt products
                val products = listOf(
                    Product("Soda Bread","0.59",rKey),
                    Product("HB ICEBERGER","2.50",rKey),
                    Product("PEELER","1.99",rKey),
                    Product("MOUTHWASH","3.62",rKey),
                    Product("FANTA","1.50",rKey),
                    Product("FROZEN PIZZA","3.50",rKey),
                    Product("FROZEN PIZZA","3.50",rKey),
                    Product("CEREAL","1.99",rKey)
                )
                //Insert into products table
                products.forEach { dao.insertProduct(it)}
            }
        }
    }
}