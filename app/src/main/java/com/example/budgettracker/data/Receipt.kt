package com.example.budgettracker.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat




//Table for the list of shops
@Entity(tableName = "shop_table")
data class Shop(
    //Primary key to uniquely identify all of the items in this table
    @PrimaryKey(autoGenerate = false)
    val shopName: String,
)


//Table for the receipts ID
@Entity(tableName = "receipt_table")
/**Make parcelable to be able to send this object between different fragments*/
@Parcelize
data class Receipt(
    val shopReceiptId: String,//fk
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val receiptId: Int = 0
)  : Parcelable {
    /**
     * Format the default date-time stamp from the "created"
     * variable into a more readable format
     */
    val createdDateFormatted: String
        get() = DateFormat.getDateInstance().format(created)
}


//Table for purchased products
@Entity(tableName = "product_table")
data class Product(
    val product: String,
    val price: Double = 0.0,
    val productReceiptId: Int,//fk
    @PrimaryKey(autoGenerate = true) val PID: Int = 0
)


//Relationship between the Receipt entity and the Product entity
data class ReceiptsWithProducts(
    @Embedded val receipt: Receipt,
    @Relation(
        parentColumn = "receiptId",
        entityColumn = "productReceiptId"
    )
    val product: List<Product>
)


//Relationship between the Shop entity and the ReceiptsWithProducts
data class ShopsWithReceipts(
    @Embedded val shop: Shop,
    @Relation(
        entity = Receipt::class,
        parentColumn = "shopName",
        entityColumn = "shopReceiptId"
    )
    val receipt: List<ReceiptsWithProducts>
)
