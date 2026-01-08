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

        // Chỉ cần thấy đơn vị tiền
        val hasMoneyUnit = listOf(
            "vnd",
            "vnđ",
            " đ"   // có space để tránh bắt nhầm chữ "đi"
        ).any { content.contains(it) }

        // Hoặc thấy chuyển khoản
        val hasTransferKeyword = listOf(
            "chuyển khoản",
            "chuyen khoan"
        ).any { content.contains(it) }

        return hasMoneyUnit || hasTransferKeyword
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

