package com.ltcn272.finny.domain.util

/**
 * Lớp Sealed Class dùng để đóng gói các kết quả của các hàm Suspend (ví dụ: Repository calls, Use Cases).
 * Nó đảm bảo hàm gọi phải xử lý cả hai trạng thái Success và Error.
 * T: Kiểu dữ liệu trả về khi thành công (ví dụ: User, List<Budget>, Unit).
 */
sealed class AppResult<out T> {
    /**
     * Trạng thái thành công, mang theo dữ liệu.
     * @param data Dữ liệu trả về (Ví dụ: Model, DTO, Unit)
     */
    data class Success<out T>(val data: T) : AppResult<T>()

    /**
     * Trạng thái thất bại (Lỗi nghiệp vụ hoặc lỗi mạng).
     * @param message Thông báo lỗi (ví dụ: từ API response, hoặc lỗi mạng)
     * @param throwable Ngoại lệ gốc (tùy chọn, dùng cho logging)
     */
    data class Error(val message: String, val throwable: Throwable? = null) : AppResult<Nothing>()

    /**
     * Trạng thái tải (Tùy chọn, có thể dùng trong Flow hoặc LiveData nếu không dùng AppResult trực tiếp)
     */
    data object Loading : AppResult<Nothing>()
}

/**
 * Extension function để tiện lợi xử lý AppResult
 * @param onSuccess block code chạy khi thành công
 * @param onError block code chạy khi thất bại
 */
inline fun <T> AppResult<T>.onSuccess(crossinline onSuccess: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) {
        onSuccess(data)
    }
    return this
}

inline fun <T> AppResult<T>.onError(crossinline onError: (String, Throwable?) -> Unit): AppResult<T> {
    if (this is AppResult.Error) {
        onError(message, throwable)
    }
    return this
}