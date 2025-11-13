package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.local.dao.BudgetDao
import com.ltcn272.finny.data.mapper.toCreateRequestDto
import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.mapper.toEntity
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.util.UUID // Dùng để tạo ID tạm thời cho Offline-First

class BudgetRepositoryImpl @Inject constructor(
    private val budgetApi: BudgetApi,
    private val budgetDao: BudgetDao
) : BudgetRepository {

    // 1. Lấy dữ liệu cho UI: ĐỌC TỪ ROOM (Fast UX)
    override fun getLocalBudgets(): Flow<List<Budget>> {
        // Lắng nghe Flow từ DAO và map Entity sang Domain Model
        return budgetDao.getAllBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // 2. Sync Ban Đầu: API -> ROOM
    override suspend fun syncBudgetsFromApi(): AppResult<Unit> {
        return try {
            val response = budgetApi.getBudgets(page = 1) // Giả định chỉ cần trang 1 để sync nhanh

            if (response.status == 200) {
                // Map DTO sang Entity và chèn vào Room
                val entities = response.data?.data?.map { it.toEntity(isSynced = true) }
                if (entities != null) {
                    budgetDao.insertAll(entities)
                }
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error("API Sync Error: ${e.localizedMessage}")
        }
    }

    // 3. Tạo/Cập nhật Cục bộ: DOMAIN -> ROOM (isSynced = false)
    override suspend fun saveBudgetLocally(budget: Budget, isNew: Boolean): AppResult<Unit> {
        val entity = if (isNew) {
            // Tạo ID tạm thời và đánh dấu chưa sync
            budget.copy(id = UUID.randomUUID().toString()) // UUID cho ID tạm thời
                .toEntity(isSynced = false)
        } else {
            // Cập nhật và đánh dấu cần sync lại
            budgetDao.getAllBudgets().map { it.find { b -> b.id == budget.id } } // Cần lấy lại Entity cũ để giữ trạng thái
            // ... Logic chuyển đổi Domain sang Entity và cập nhật isSynced = false
            budget.toEntity(isSynced = false) // Ví dụ đơn giản, bạn cần lấy isDeleted cũ nếu có
        }

        budgetDao.insert(entity) // Insert hoặc Replace
        return AppResult.Success(Unit)
    }

    // 4. Đồng bộ hóa thay đổi cục bộ lên API (Background Task)
    override suspend fun pushLocalChangesToApi(): AppResult<Unit> {
        val pendingEntities = budgetDao.getBudgetsToSync()

        pendingEntities.forEach { entity ->
            val budgetDomain = entity.toDomain()
            val requestDto = budgetDomain.toCreateRequestDto()

            try {
                // Xử lý DELETE trước
                if (entity.isDeleted) {
                    budgetApi.deleteBudget(entity.id)
                    budgetDao.deleteById(entity.id) // Xóa vĩnh viễn khỏi Room sau khi API thành công
                }
                // Xử lý CREATE/UPDATE
                else if (!entity.isSynced) {
                    // Nếu là bản ghi mới (ID tạm thời) -> POST, nếu là ID cũ -> PUT
                    val isPost = !entity.id.contains("-") // Giả định ID tạm thời chứa dấu '-'

                    val response = if (isPost) {
                        budgetApi.createBudget(requestDto)
                    } else {
                        budgetApi.updateBudget(entity.id, requestDto)
                    }

                    if (response.status == 200) {
                        // Cập nhật trạng thái sync thành công
                        budgetDao.update(entity.copy(isSynced = true, isDeleted = false))
                        // Nếu là POST, cần cập nhật ID mới từ API response (tùy thuộc vào cách API trả về)
                    }
                }
            } catch (e: Exception) {
                // Bỏ qua lỗi và tiếp tục, để background worker retry sau
                return AppResult.Error("Sync failed for ${entity.id}: ${e.localizedMessage}")
            }
        }

        return AppResult.Success(Unit)
    }
}