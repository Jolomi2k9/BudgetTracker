package com.example.budgettracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgettracker.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

/**
 *Trigger Room code generation by annotating class.
 *Pass in the Receipt table.
 * We use version to update the database if we change the
 * schema.
 */
@Database(entities = [Receipt::class], version = 1)
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

    /*
    * @Inject enables dagger how to create an instance of the class below
    * We make dagger to not instantiate the database when
    * the below callback is created to avoid a circular dependency, but will do so
    * only when the onCreate() method below is executed, which happens AFTER
    * the build() method in AppModule.kt has finished.
    */
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

            /*
            * we use this coroutine to run suspends functions,
            * which can only run by coroutines
            */
            applicationScope.launch{
                dao.insert(Receipt("Tesco","CKD CHICKEN",3.50))
                dao.insert(Receipt("Tesco","COCOA POWDER",1.79))
                dao.insert(Receipt("Tesco","MILK CHOCOLATE",1.39))
                dao.insert(Receipt("Tesco","FRESH PROTEIN",1.09))
                dao.insert(Receipt("Tesco","TESCO TEABAGS",1.89))
                dao.insert(Receipt("Tesco","HB ICEBERGER",2.50))
                dao.insert(Receipt("Tesco","PEELER",1.99))
                dao.insert(Receipt("Tesco","CKD CHICKEN",3.50))
            }

        }
    }
}