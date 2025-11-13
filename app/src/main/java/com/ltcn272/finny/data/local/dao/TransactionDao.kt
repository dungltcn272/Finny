package com.ltcn272.finny.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ltcn272.finny.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    /**
     * Đồng bộ dữ liệu ban đầu từ API vào Room DB.
     * Sử dụng REPLACE để thay thế nếu có ID trùng (đảm bảo dữ liệu server luôn là nguồn tin cậy).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    /**
     * Thêm mới giao dịch cục bộ hoặc cập nhật (khi User thực hiện Add/Edit).
     * Giao dịch này sẽ có 'isSynced = false' (hoặc isDeleted = true)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    /**
     * Cập nhật trạng thái của giao dịch (ví dụ: sau khi POST thành công, cập nhật isSynced = true).
     */
    @Update
    suspend fun update(transaction: TransactionEntity)

    /**
     * Lấy tất cả Transaction (chưa bị đánh dấu xóa) để hiển thị trên UI.
     * Dùng Flow để ViewModel tự động lắng nghe thay đổi.
     * Sắp xếp theo ngày giờ (dateTime) giảm dần.
     */
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY dateTime DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Lấy danh sách các giao dịch cần được đồng bộ lên API (Background Sync).
     * Điều kiện: isSynced = 0 (chưa POST) HOẶC isDeleted = 1 (chưa DELETE trên server).
     */
    @Query("SELECT * FROM transactions WHERE isSynced = 0 OR isDeleted = 1")
    suspend fun getTransactionsToSync(): List<TransactionEntity>

    /**
     * Xóa vĩnh viễn khỏi Room (Chỉ gọi sau khi API DELETE thành công hoặc là giao dịch tạm).
     */
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: String)
}