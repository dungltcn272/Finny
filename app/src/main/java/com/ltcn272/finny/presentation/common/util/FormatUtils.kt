package com.ltcn272.finny.presentation.common.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Định dạng một chuỗi số thành chuỗi có dấu phân cách hàng nghìn.
 * Ví dụ: "1000000" -> "1.000.000"
 *
 * @param amount Chuỗi số đầu vào.
 * @return Chuỗi đã được định dạng. Nếu đầu vào không phải là số hợp lệ, trả về chuỗi gốc.
 */
fun formatAmountWithSeparators(amount: String): String {
    return try {
        val number = amount.toLong()
        // Sử dụng Locale của Việt Nam để có dấu phân cách hàng nghìn là dấu chấm (.)
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        formatter.format(number)
    } catch (e: NumberFormatException) {
        amount // Trả về chuỗi gốc nếu không thể parse
    }
}
