package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PausaDao {
    @Query("SELECT * FROM app_limits")
    fun getAllAppLimits(): Flow<List<AppLimitEntity>>

    @Query("SELECT COUNT(*) FROM app_limits")
    suspend fun getAppLimitsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppLimits(limits: List<AppLimitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppLimit(limit: AppLimitEntity)

    @Update
    suspend fun updateAppLimit(limit: AppLimitEntity)

    @Query("SELECT * FROM pause_logs ORDER BY timestamp DESC")
    fun getAllPauseLogs(): Flow<List<PauseLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPauseLog(log: PauseLogEntity)

    @Query("SELECT * FROM plants ORDER BY id ASC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants ORDER BY id ASC")
    suspend fun getAllPlantsSync(): List<PlantEntity>

    @Query("SELECT * FROM plants WHERE isCurrent = 1 LIMIT 1")
    suspend fun getActivePlantSync(): PlantEntity?

    @Query("SELECT * FROM app_limits")
    suspend fun getAllAppLimitsSync(): List<AppLimitEntity>

    @Query("SELECT COUNT(*) FROM plants")
    suspend fun getPlantsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlants(plants: List<PlantEntity>)

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Query("UPDATE plants SET isCurrent = CASE WHEN id = :plantId THEN 1 ELSE 0 END")
    suspend fun setActivePlant(plantId: Int)

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC, id DESC")
    fun getAllWalletTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC, id DESC")
    suspend fun getAllWalletTransactionsSync(): List<WalletTransactionEntity>

    @Query("SELECT * FROM wallet_transactions WHERE status = :status ORDER BY timestamp DESC, id DESC")
    fun getWalletTransactionsByStatus(status: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE orderReference = :orderReference LIMIT 1")
    suspend fun getWalletTransactionByReference(orderReference: String): WalletTransactionEntity?

    @Query("SELECT * FROM wallet_transactions WHERE id = :id LIMIT 1")
    suspend fun getWalletTransactionById(id: Int): WalletTransactionEntity?

    @Query("SELECT COUNT(*) FROM wallet_transactions")
    suspend fun getWalletTransactionsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTransaction(tx: WalletTransactionEntity): Long

    @Update
    suspend fun updateWalletTransaction(tx: WalletTransactionEntity)

    @Query(
        """
        UPDATE wallet_transactions 
        SET status = :status, 
            subtitle = :subtitle, 
            balanceAfter = :balanceAfter, 
            updatedAt = :updatedAt 
        WHERE orderReference = :orderReference
        """
    )
    suspend fun updateWalletTransactionStatus(
        orderReference: String,
        status: String,
        subtitle: String,
        balanceAfter: Double,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE wallet_transactions 
        SET status = :newStatus, 
            type = 'PENALTY_PAYPAL_BALANCE',
            updatedAt = :updatedAt,
            balanceAfter = :balanceAfter
        WHERE type = 'PENALTY_PAYPAL_PENDING' AND status = 'PENDING'
        """
    )
    suspend fun settleAllPendingPenaltyTransactions(
        newStatus: String = TransactionStatus.COMPLETED.name,
        updatedAt: Long = System.currentTimeMillis(),
        balanceAfter: Double = 0.0
    ): Int

    @Transaction
    suspend fun completeTransactionAndSyncAccount(
        orderReference: String,
        completedSubtitle: String,
        balanceAfter: Double,
        updatedAt: Long,
        settledPenaltyDebt: Boolean = false,
        updatedAccount: UserAccountEntity? = null
    ) {
        updateWalletTransactionStatus(
            orderReference = orderReference,
            status = TransactionStatus.COMPLETED.name,
            subtitle = completedSubtitle,
            balanceAfter = balanceAfter,
            updatedAt = updatedAt
        )
        if (settledPenaltyDebt) {
            settleAllPendingPenaltyTransactions(
                newStatus = TransactionStatus.COMPLETED.name,
                updatedAt = updatedAt,
                balanceAfter = balanceAfter
            )
        }
        if (updatedAccount != null) {
            insertUserAccount(updatedAccount)
        }
    }

    @Query("DELETE FROM pause_logs")
    suspend fun clearPauseLogs()

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearWalletTransactions()

    @Query("DELETE FROM plants")
    suspend fun clearPlants()

    @Query("DELETE FROM app_limits")
    suspend fun clearAppLimits()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccountEntity)

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserAccountByEmail(email: String): UserAccountEntity?
}
