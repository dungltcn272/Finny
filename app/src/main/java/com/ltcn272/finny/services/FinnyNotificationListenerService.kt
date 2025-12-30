package com.ltcn272.finny.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.ltcn272.finny.data.SettingDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FinnyNotificationListenerService : NotificationListenerService() {
    private val TAG = "FinnyNotiListener"
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    @Inject
    lateinit var settingDataStore: SettingDataStore

    private val financialAppPackages = mapOf(

        // ======= NGÂN HÀNG QUỐC DOANH =======
        "com.mbmobile" to "MB Bank",
        "vn.com.vietcombank.android.vcblite" to "Vietcombank",
        "vn.com.bidv.smartbanking" to "BIDV SmartBanking",
        "vn.com.vietinbank.ipay" to "VietinBank iPay",

        // ======= NGÂN HÀNG TMCP =======
        "vn.com.techcombank.bb.app" to "Techcombank",
        "vn.com.acb.acb_mobile" to "ACB",
        "vn.com.tpb.ebank" to "TPBank",
        "vn.com.vpb.neo" to "VPBank NEO",
        "com.shb.mobile" to "SHB Mobile",
        "com.ocb.mobile" to "OCB OMNI",
        "com.msb.smartBanking" to "MSB Bank",
        "vn.com.hdbank.smartbanking" to "HDBank",
        "com.seabank.mbapp" to "SeABank",
        "com.lpb.lienviet24h" to "LienVietPostBank",
        "com.scb.sacombank" to "Sacombank Pay",
        "com.eximbank.ebanking" to "Eximbank",
        "com.vib.myvib2" to "VIB",
        "com.abbank.mobile" to "ABBank",
        "com.baca.mobilebanking" to "Bac A Bank",
        "com.ncb.mobile" to "NCB",
        "com.pvcombank.smartbank" to "PVcomBank",
        "com.saigonbank.mobile" to "SaigonBank",
        "com.vietabank.mobile" to "VietABank",
        "com.kienlongbank.mobile" to "KienlongBank",
        "com.bvbank.digimi" to "BVBank DigiMi",
        "com.vietbank.mobile" to "VietBank",
        "com.namabank.mobile" to "Nam A Bank",
        "com.oceanbank.mobile" to "OceanBank",

        // ======= NGÂN HÀNG SỐ =======
        "com.timo.plus" to "Timo",
        "com.tyme.digitalbank" to "TymeX",

        // ======= VÍ ĐIỆN TỬ =======
        "vn.com.momo" to "MoMo",
        "com.viettel.vtmoney" to "Viettel Money",
        "vn.zalopay" to "ZaloPay",
        "com.mservice.zalopay" to "ZaloPay (old)",
        "vn.com.vnpay.wallet" to "VNPay",
        "com.shopee.wallet" to "ShopeePay",
        "com.grabtaxi.passenger" to "Grab (Moca)",
        "com.viviet" to "Ví Việt",
        "com.airpay.android" to "AirPay (cũ)",
        "vn.payoo.wallet" to "Payoo",

        // ======= TÀI CHÍNH / CHỨNG KHOÁN =======
        "vn.com.ssi.mobile" to "SSI iBoard",
        "com.vndirect.app" to "VNDIRECT",
        "com.hsc.mobiletrading" to "HSC",
        "com.vpsmart.one" to "VPBank Securities (VPSS)",
    )


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "Notification received from package: ${sbn?.packageName}")

        if (sbn == null || sbn.packageName !in financialAppPackages.keys) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() || text.isBlank() || text.length < 10) {
            return
        }

        Log.d(TAG, "✅ Notification captured from: ${sbn.packageName}. Saving to inbox.")

        val appName = financialAppPackages[sbn.packageName] ?: "Unknown"

        // Format: appName;;;title;;;text
        val rawNotificationData = "$appName;;;$title;;;$text"

        serviceScope.launch {
            // SỬ DỤNG HÀM MỚI
            settingDataStore.addRawBankNotification(rawNotificationData)
            Log.i(TAG, "Successfully added raw notification to inbox queue.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
