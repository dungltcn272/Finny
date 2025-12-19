//// /home/dung/Finny_Deloy/app/src/main/java/com/ltcn272/finny/presentation/features/transation/create_transaction/component/AmountDisplay.kt
//
//package com.ltcn272.finny.presentation.features.transation.create_transaction.component
//
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.width
//import androidx.compose.material3.LocalTextStyle
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.TextUnit
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.isUnspecified
//import androidx.compose.ui.unit.sp
//import com.ltcn272.finny.presentation.common.util.CurrencyUtils.formatAmountWithSeparators
//import kotlin.math.pow
//import kotlin.ranges.coerceIn
//
//@Composable
//fun AmountDisplay(
//    currencySymbol: String = "$",
//    amount: String = "5.99",
//    modifier: Modifier = Modifier
//) {
//    val scale = remember(amount) {
//        calculateScaleByDigits(amount)
//    }
//
//    val symbolFontSize = applyScaleToFontSize(
//        maxFontSize = 32.sp,
//        minFontSize = 16.sp,
//        scale = scale
//    )
//    val amountFontSize = applyScaleToFontSize(
//        maxFontSize = 54.sp,
//        minFontSize = 24.sp,
//        scale = scale
//    )
//
//    Row(
//        modifier = modifier,
//        verticalAlignment = Alignment.Top
//    ) {
//        AutoResizingTextSimple(
//            text = currencySymbol,
//            color = Color.Gray.copy(alpha = 0.8f),
//            fontSize = symbolFontSize,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.offset(y = 8.dp)
//        )
//
//        Spacer(modifier = Modifier.width(2.dp))
//        AutoResizingTextSimple(
//            text = formatAmountWithSeparators(amount),
//            color = Color.Black,
//            fontWeight = FontWeight.Bold,
//            maxLines = 1,
//            fontSize = amountFontSize
//        )
//    }
//}
//
//@Composable
//private fun AutoResizingTextSimple(
//    text: String,
//    modifier: Modifier = Modifier,
//    color: Color = Color.Unspecified,
//    fontWeight: FontWeight? = null,
//    maxLines: Int = Int.MAX_VALUE,
//    fontSize: TextUnit
//) {
//    val textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = fontSize)
//
//    Text(
//        text = text,
//        color = color,
//        fontWeight = fontWeight,
//        maxLines = maxLines,
//        overflow = TextOverflow.Ellipsis,
//        style = textStyle,
//        modifier = modifier
//    )
//}
//
//private fun calculateScaleByDigits(rawAmount: String): Float {
//    val digitsCount = rawAmount.count { it.isDigit() }
//
//    // Tới 999.999 (6 chữ số) giữ nguyên
//    if (digitsCount <= 6) return 1f
//
//    // Bắt đầu giảm từ 1.000.000 (7 chữ số)
//    val extraDigits = digitsCount - 6
//    // Mỗi thêm 3 chữ số → giảm 1 bậc (dùng ceil(extraDigits / 3.0))
//    val steps = (extraDigits + 2) / 3
//
//    return 0.8f.pow(steps)
//}
//
//private fun applyScaleToFontSize(
//    maxFontSize: TextUnit,
//    minFontSize: TextUnit,
//    scale: Float
//): TextUnit {
//    if (maxFontSize.isUnspecified) return maxFontSize
//
//    val scaled = maxFontSize.value * scale
//
//    return scaled
//        .coerceIn(minFontSize.value, maxFontSize.value)
//        .sp
//}
