package com.example.budgettracker.data

import android.content.Context
import android.util.Log
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

//log
private const val TAG = "PreferencesManager"
//
enum class SortOrder{ BY_STORE, BY_DATE}

//return multiple if avialable
data class FilterPreferences(val sortOrder: SortOrder)

//abstraction layer to data to  viewmodel
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context){

    private val dataStore = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data
        .catch {exception ->
            if (exception is IOException){
                Log.e(TAG,"Error in preferences",exception)
                //if io exception continue without default preferences
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }
         //transform data coming into flow
        .map { preferences ->
            //turn enum into string and vise versa
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            //return FilterPreferences object
            FilterPreferences(sortOrder)
        }

    //update existing data in data store
    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    //use this to distinguish between values in the data store
    private object PreferencesKeys{
        val SORT_ORDER = preferencesKey<String>("sort_order")
    }
}