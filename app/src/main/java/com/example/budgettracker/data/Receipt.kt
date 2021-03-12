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
    val shopName: String,
    //Primary key to uniquely identify all of the items in this table
    @PrimaryKey(autoGenerate = true) val shopId: Int = 0
)


//Table for the receipts
@Entity(tableName = "receipt_table")
/**Make parcelable to be able to send this object between different fragments*/
@Parcelize
data class Receipt(
    val shopReceiptId: Int,//fk
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
@Parcelize
data class Product(
    val product: String,
    val price: String,
    val productReceiptId: Int,//fk
    @PrimaryKey(autoGenerate = true) val productID: Int = 0
) : Parcelable

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
        parentColumn = "shopId",
        entityColumn = "shopReceiptId"
    )
    val receipt: List<ReceiptsWithProducts>
)
