package com.ltcn272.finny.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ltcn272.finny.data.SettingDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FinnyNotificationListenerService : NotificationListenerService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    @Inject
    lateinit var settingDataStore: SettingDataStore

    private val financialAppPackages = mapOf(

        // ======= NGÂN HÀNG QUỐC DOANH =======
        "com.mbmobile" to "MB Bank",
        "com.VCB" to "Vietcombank",
        "com.vnpay.bidv" to "BIDV SmartBanking",
        "com.vietinbank.ipay" to "VietinBank iPay",
        "com.vnpay.Agribank3g" to "Agribank",

        // ======= NGÂN HÀNG TMCP =======
        "vn.com.techcombank.bb.app" to "Techcombank",
        "mobile.acb.com.vn" to "ACB",
        "com.tpb.mb.gprsandroid" to "TPBank",
        "com.VCB" to "Vietcombank",
        "com.vnpay.vpbankonline" to "VPBank NEO",
        "vn.com.seabank.mb1" to "SeABank",
        "om.sacombank.ewallet" to "Sacombank Pay",
        "com.vib.myvib2" to "VIB",
        "com.pvcombank.smartbank" to "PVcomBank",
        "com.vnpay.vietbank" to "VietBank",
        "com.vnpay.bidv" to "BIDV SmartBanking",

        // ======= NGÂN HÀNG SỐ =======
        "io.lifestyle.plus" to "Timo",

        // ======= VÍ ĐIỆN TỬ =======
        "com.mservice.momotransfer" to "MoMo",
        "com.bplus.vtpay" to "Viettel Money",
        "vn.com.vng.zalopay" to "ZaloPay",
        "vnpay.smartacccount" to "VNPay",
        "com.beeasy.toppay" to "ShopeePay",
    )

    private fun isTransactionMessage(text: String): Boolean {
        val content = text.lowercase()

        // 1. Kiểm tra các định dạng số tiền (Regex)
        // Tìm các cụm có số đi kèm VND, VNĐ, đ, hoặc dấu +, - ở trước số
        val amountRegex = Regex("([+-]?\\s?\\d{1,3}([,.]\\d{3})*(\\s?)(vnd|vnđ|đ|vnds))")
        val hasAmount = amountRegex.containsMatchIn(content)

        // 2. Danh sách từ khóa (có dấu và không dấu)
        val keywords = listOf(
            "số dư", "so du", "biến động", "bien dong",
            "tài khoản", "tai khoan", "tk ", "gd ", "giao dịch", "giao dich",
            "thanh toán", "thanh toan", "thành công", "thanh cong",
            "đã nhận", "da nhan", "đã trừ", "da tru", "chuyển khoản", "chuyen khoan"
        )

        val hasKeyword = keywords.any { content.contains(it) }

        // Một tin nhắn giao dịch thường cần cả 2: có số tiền VÀ có từ khóa ngân hàng
        return hasAmount && hasKeyword
    }


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.packageName !in financialAppPackages.keys) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() || text.isBlank() || text.length < 10) {
            return
        }

        if (!isTransactionMessage(text)) {
            return
        }


        val appName = financialAppPackages[sbn.packageName] ?: "Unknown"

        // Format: appName;;;title;;;text
        val rawNotificationData = "$appName;;;$title;;;$text"

        serviceScope.launch {
            settingDataStore.addRawBankNotification(rawNotificationData)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}

