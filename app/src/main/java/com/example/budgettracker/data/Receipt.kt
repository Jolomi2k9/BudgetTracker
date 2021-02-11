package com.example.budgettracker.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

/***/
@Entity(tableName = "receipt_table")
/**Make parcelable to be able to send this object between different fragments*/
@Parcelize
data class Receipt(
    val shopName: String,
    val product: String,
    val price: Double = 0.0,
    val created: Long = System.currentTimeMillis(),
    /**Primary key to uniquely identify all of the items in our table*/
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    /**Format the default date-time stamp from the "created"
     * variable into a more readable format
     */
    val createdDateFormatted: String
        get() = DateFormat.getDateInstance().format(created)
}