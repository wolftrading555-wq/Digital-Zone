package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DealerDao
import com.example.data.dao.GpsVisitDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.OilDao
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.data.model.Product
import com.example.data.model.RecoveryPayment
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import com.example.data.model.SalesOfficer
import com.example.data.model.StockTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Dealer::class,
        SaleOrder::class,
        SaleOrderItem::class,
        RecoveryPayment::class,
        SalesOfficer::class,
        GpsVisitLog::class,
        StockTransaction::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun oilDao(): OilDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun dealerDao(): DealerDao
    abstract fun gpsVisitDao(): GpsVisitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "engine_oil_business.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        InitialData.populateDatabase(database.oilDao())
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        InitialData.populateDatabase(database.oilDao())
                    }
                }
            }
        }
    }
}
