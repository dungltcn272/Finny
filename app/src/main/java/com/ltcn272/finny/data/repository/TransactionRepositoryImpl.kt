package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.local.dao.TransactionDao
import com.ltcn272.finny.data.mapper.* // Cần import tất cả các mapper
import com.ltcn272.finny.data.remote.api.TransactionApi
import com.ltcn272.finny.data.remote.dto.FilterBodyDto
import com.ltcn272.finny.data.remote.dto.TransactionFilterRequestDto
import com.ltcn272.finny.data.remote.dto.TransactionDto
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.AppResult.Error
import com.ltcn272.finny.domain.util.AppResult.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionApi: TransactionApi,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    // 1. Lấy dữ liệu cho UI (từ Room, Fast UX)
    override fun getLocalTransactions(filter: TransactionFilter): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
                // Lọc thêm theo logic trong TransactionFilter (ví dụ: filter theo budgetId)
                .filter { txn -> filter.budgetId == null || txn.budgetId == filter.budgetId }
        }
    }

    // 2. Sync Ban Đầu/Refresh: API -> ROOM
    override suspend fun syncTransactionsFromApi(filter: TransactionFilter): AppResult<Unit> {
        return try {
            // 1. Chuẩn bị Filter Body
            val filterBody = filter.budgetId?.let { FilterBodyDto(it) }

            var currentPage = 1
            var lastPage = 1 // Khởi tạo để đảm bảo vòng lặp chạy ít nhất 1 lần

            while (currentPage <= lastPage) {
                // 2. Xây dựng Request DTO
                val requestDto = TransactionFilterRequestDto(
                    page = currentPage,
                    filter = filterBody
                )

                // 3. Gọi API
                val response = transactionApi.getTransactions(requestDto)

                if (response.status == 200 && response.data != null) {

                    // 4. Cập nhật thông tin phân trang cho lần lặp tiếp theo
                    lastPage = response.data.pagination.lastPage

                    // 5. Map DTOs sang Entities và chèn vào Room (REPLACE strategy)
                    // isSynced = true vì dữ liệu đến từ server
                    val entities = response.data.data.map { it.toEntity(isSynced = true) }
                    transactionDao.insertAll(entities)

                    // 6. Tăng counter và tiếp tục
                    currentPage++
                } else {
                    // Nếu API thất bại trên bất kỳ trang nào, dừng sync và báo lỗi
                    return AppResult.Error("API Sync failed on page $currentPage: ${response.message}")
                }
            }

            return AppResult.Success(Unit)
        } catch (e: Exception) {
            return AppResult.Error("Network Sync Error: ${e.localizedMessage}", e)
        }
    }

    // 3. Tạo/Cập nhật Cục bộ: DOMAIN -> ROOM (Offline-First)
    override suspend fun saveTransactionLocally(transaction: Transaction, isNew: Boolean): AppResult<Unit> {
        // Luôn sử dụng ID hiện tại hoặc tạo ID tạm thời mới
        val finalId = if (isNew) UUID.randomUUID().toString() else transaction.id

        // Tạo Entity với trạng thái chưa sync
        val entity = transaction.copy(id = finalId).toEntity(
            isSynced = false,
            isDeleted = false // Chắc chắn không phải xóa
        )

        transactionDao.insert(entity) // Insert hoặc Replace (dùng onConflict = REPLACE)
        return Success(Unit)
    }

    // 4. Xóa cục bộ (Soft Delete cho mục đích sync)
    override suspend fun deleteTransactionLocally(transactionId: String): AppResult<Unit> {
        // Logic: Lấy Entity ra, đánh dấu isDeleted = true, và update lại vào Room
        // RoomDB không có hàm getById trực tiếp cho Flow, giả sử chúng ta có thể làm như sau:
        // Cần phải có hàm getById trong DAO. Tạm thời giả định có:
        // val existingEntity = transactionDao.getById(transactionId)

        val fakeEntityToDelete = transactionDao.getTransactionsToSync().firstOrNull { it.id == transactionId }
            ?.copy(isDeleted = true, isSynced = false) // Đánh dấu cần sync và xóa
            ?: return Error("Transaction not found locally for ID: $transactionId") // Xử lý nếu không tìm thấy

        transactionDao.update(fakeEntityToDelete)
        return Success(Unit)
    }

    // 5. Đồng bộ hóa các thay đổi cục bộ lên API (Background Task)
    override suspend fun pushLocalChangesToApi(): AppResult<Unit> {
        val pendingEntities = transactionDao.getTransactionsToSync()

        pendingEntities.forEach { entity ->
            // Bỏ qua các Entity đã sync hoặc bị xóa cứng
            if (entity.isSynced && !entity.isDeleted) return@forEach

            val transactionDomain = entity.toDomain()

            try {
                // 1. Xử lý DELETE
                if (entity.isDeleted) {
                    transactionApi.deleteTransaction(entity.id)
                    // Thành công: Xóa vĩnh viễn khỏi Room
                    transactionDao.deleteById(entity.id)
                    return@forEach
                }

                // 2. Xử lý CREATE/UPDATE
                val requestDto = transactionDomain.toCreateRequestDto()

                // Dùng logic ID tạm thời để xác định POST hay PUT
                val isPost = !entity.id.contains("-") // Giả định ID tạm thời chứa dấu '-'

                val response = if (isPost) {
                    // Clean Code: Truyền DTO trực tiếp, Retrofit lo việc JSON hóa
                    transactionApi.createTransaction(requestDto)
                } else {
                    // Clean Code: Truyền DTO trực tiếp
                    transactionApi.updateTransaction(entity.id, requestDto)
                }

                if (response.status == 200) {
                    // Cập nhật trạng thái sync thành công
                    // TODO: Nếu là POST, cần kiểm tra response.data để lấy ID chính thức
                    // nếu API trả về ID mới, sau đó xóa Entity cũ và chèn Entity mới.
                    transactionDao.update(entity.copy(isSynced = true, isDeleted = false))
                } else {
                    // Lỗi nghiệp vụ từ API (ví dụ: 400 Bad Request)
                    return Error("API response error for ${entity.id}: ${response.message}")
                }

            } catch (e: Exception) {
                // Lỗi mạng, Timeout, hoặc Serialization: Dừng lại và báo hiệu để retry sau
                return Error("Sync failed (Network/IO) for ${entity.id}: ${e.localizedMessage}", e)
            }
        }

        // Tất cả các bản ghi pending đều đã được xử lý thành công
        return Success(Unit)
    }

    // 6. Upload ảnh (Logic đã có, giữ lại để tiện theo dõi)
    override suspend fun uploadImage(imagePath: String): AppResult<String> {
        return try {
            val file = File(imagePath)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, requestBody)

            val response = transactionApi.uploadImage(part)

            if (response.status == 200 && response.data != null) {
                Success(response.data.imageUrl)
            } else {
                Error(response.message)
            }
        } catch (e: Exception) {
            Error("Image upload failed: ${e.localizedMessage}", e)
        }
    }
}
