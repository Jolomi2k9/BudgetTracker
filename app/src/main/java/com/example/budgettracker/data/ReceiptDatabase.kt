package com.example.budgettracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgettracker.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.collections.ArrayList

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
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            //
            val dao = database.get().receiptDao()

            //insert into shops
            val shops = listOf(Shop("Tesco"))
            //get shops primary key
            val hey = shops[0]
            val sKey = hey.shopId
            //insert into receipts
            val receipts = listOf(Receipt(sKey))
            //get receipt primary key
            val hey1 = receipts[0]
            val rKey = hey1.receiptId
            //insert into products
            val products = listOf(
                            Product("CKD CHICKEN",3.50,rKey),
                            Product("COCOA POWDER",1.79,rKey),
                            Product("MILK CHOCOLATE",1.09,rKey),
                            Product("FRESH PROTEIN",1.89,rKey),
                            Product("TESCO TEABAGS",2.50,rKey),
                            Product("PEELER",1.99,rKey)
            )
            // we use this coroutine to run suspends functions,
            //which can only be run by coroutines
            applicationScope.launch{
                shops.forEach { dao.insertShop(it)}
                receipts.forEach { dao.insertReceipt(it)}
                products.forEach { dao.insertProduct(it)}

            }

        }
    }

    /*companion object {


        *//**
         * The value of a volatile variable will never be cached, and all
         * writes and reads will be done to and from the main memory.
         * *//*
        @Volatile
        private var INSTANCE: ReceiptDatabase? = null

        fun getInstance(context: Context): ReceiptDatabase {

            synchronized(this) {}

            var instance = INSTANCE

            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReceiptDatabase::class.java,
                    "sleep_history_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

            }
            return instance
        }

    }
*/



}