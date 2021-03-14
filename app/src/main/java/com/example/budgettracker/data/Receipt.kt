package com.example.budgettracker.data

import android.os.Parcelable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat




//Table for the list of shops
@Entity(tableName = "shop_table")
data class Shop(
    @ColumnInfo(name = "shopName")val shopName: String,
    //Primary key to uniquely identify all of the items in this table
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name ="shopId")val shopId: Int = 0
)


//Table for the receipts
@Entity(tableName = "receipt_table",
    foreignKeys = arrayOf(
        ForeignKey(entity = Shop::class,
            parentColumns = arrayOf("shopId"),
            childColumns = arrayOf("shopReceiptId"),
            onDelete = CASCADE))
        )
/**Make parcelable to be able to send this object between different fragments*/
@Parcelize
data class Receipt(
    @ColumnInfo(name = "shopReceiptId")val shopReceiptId: Int,//fk
    @ColumnInfo(name = "created")val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "receiptId")val receiptId: Int = 0
)  : Parcelable {
    /**
     * Format the default date-time stamp from the "created"
     * variable into a more readable format
     */
    val createdDateFormatted: String
        get() = DateFormat.getDateInstance().format(created)
}

//Table for purchased products
@Entity(tableName = "product_table",
        foreignKeys = arrayOf(
            ForeignKey(entity = Receipt::class,
                    parentColumns = arrayOf("receiptId"),
                childColumns = arrayOf("productReceiptId"),
                onDelete = CASCADE))
        )
@Parcelize
data class Product(
    @ColumnInfo(name = "product")val product: String,
    @ColumnInfo(name = "price")val price: String,
    @ColumnInfo(name = "productReceiptId")val productReceiptId: Int,//fk
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "productID")val productID: Int = 0
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
