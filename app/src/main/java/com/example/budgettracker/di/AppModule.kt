package com.example.budgettracker.di

import android.app.Application
import androidx.room.Room
import com.example.budgettracker.data.ReceiptDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/***/
@Module
/**Use this database thought out our app*/
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    //only creates one instance of the Receipt database
    @Singleton
    fun provideDatabase(
        //get the application context
        app: Application,
        calllback: ReceiptDatabase.Callback
    )     /*Creates a database object from the ReceiptDatabase abstract class*/
        = Room.databaseBuilder(app, ReceiptDatabase::class.java, "receipt_database")
            /*
             * What room should do if we update our database schema but do not specify
             * a proper migration strategy.
             * In this case it will drop the table and create a new one.
             */
            .fallbackToDestructiveMigration()
            //use to have dummy data in the recycler view if no receipt is in database
            .addCallback(calllback)
            /*creates an instance of the Receipt database class*/
            .build()

    /*
     * Creates Receipt Dao object which is needed to perform database operations.
     * Always a singleton
     */
    @Provides
    fun providesReceiptDao(db: ReceiptDatabase) = db.receiptDao()


    /*
    * Created a coroutine scope that lives as long as this application lives.
    * use a supervisorjob to prevent the coroutinescope from terminating all child
    * coroutine if one fails
    */
    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}


//
@Retention(AnnotationRetention.RUNTIME)
//avoids ambiguity with the application scope
@Qualifier
annotation class ApplicationScope
