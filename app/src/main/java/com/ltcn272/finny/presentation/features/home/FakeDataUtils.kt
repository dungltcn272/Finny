package com.ltcn272.finny.presentation.features.home

import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionCategory
import com.ltcn272.finny.domain.model.TransactionType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

object FakeDataUtils {

    private const val USER_ID = "test_user"

    /**
     * Fake budgets:
     *  - id để rỗng, khi lưu qua Room thì entity sẽ được Room tự gen id.
     *  - createdAt các budget cách baseDate vài tháng để nhìn “lâu lâu”.
     *  - TẤT CẢ date < 08/12/2025 (baseDate = 07/12/2025).
     */
    fun getFakeBudgets(): List<Budget> {
        // Cố định baseDate = 2025-12-07T09:00 trước ngày 08/12/2025
        val baseDate = ZonedDateTime.of(
            2025, 12, 7,
            9, 0, 0, 0,
            ZoneId.systemDefault()
        )

        val startOfCurrentMonth = baseDate.withDayOfMonth(1)       // 2025-12-01
        val startOfYear = baseDate.withMonth(1).withDayOfMonth(1)  // 2025-01-01

        val foodCreatedAt = baseDate.minusMonths(4)        // 2025-08-07
        val transportCreatedAt = baseDate.minusMonths(3)   // 2025-09-07
        val entertainmentCreatedAt = baseDate.minusMonths(2).minusDays(5) // 2025-10-02
        val shoppingCreatedAt = baseDate.minusMonths(2)    // 2025-10-07
        val healthCreatedAt = baseDate.minusMonths(5)      // 2025-07-07
        val housingCreatedAt = baseDate.minusMonths(6)     // 2025-06-07
        val utilitiesCreatedAt = baseDate.minusMonths(3).minusDays(10) // 2025-08-28
        val educationCreatedAt = baseDate.minusMonths(8)   // 2025-04-07
        val incomeCreatedAt = baseDate.minusMonths(6)      // 2025-06-07

        return listOf(
            Budget(
                id = "",
                name = "Ăn uống hàng ngày",
                userId = USER_ID,
                limit = 4_000_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = foodCreatedAt,
                updatedAt = baseDate.minusDays(2)
            ),
            Budget(
                id = "",
                name = "Đi lại",
                userId = USER_ID,
                limit = 1_200_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = transportCreatedAt,
                updatedAt = baseDate.minusDays(3)
            ),
            Budget(
                id = "",
                name = "Giải trí",
                userId = USER_ID,
                limit = 1_500_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = entertainmentCreatedAt,
                updatedAt = baseDate.minusDays(4)
            ),
            Budget(
                id = "",
                name = "Mua sắm cá nhân",
                userId = USER_ID,
                limit = 3_000_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = shoppingCreatedAt,
                updatedAt = baseDate.minusDays(1)
            ),
            Budget(
                id = "",
                name = "Sức khỏe",
                userId = USER_ID,
                limit = 1_500_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = healthCreatedAt,
                updatedAt = baseDate.minusDays(5)
            ),
            Budget(
                id = "",
                name = "Nhà ở",
                userId = USER_ID,
                limit = 5_000_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = housingCreatedAt,
                updatedAt = baseDate.minusDays(6)
            ),
            Budget(
                id = "",
                name = "Hóa đơn tiện ích",
                userId = USER_ID,
                limit = 2_000_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = utilitiesCreatedAt,
                updatedAt = baseDate.minusDays(3)
            ),
            Budget(
                id = "",
                name = "Giáo dục / học tập",
                userId = USER_ID,
                limit = 10_000_000.0,
                period = BudgetPeriod.ONE_YEAR,
                startDate = startOfYear,
                createdAt = educationCreatedAt,
                updatedAt = baseDate.minusDays(10)
            ),
            Budget(
                id = "",
                name = "Thu nhập cố định",
                userId = USER_ID,
                limit = 30_000_000.0,
                period = BudgetPeriod.ONE_MONTH,
                startDate = startOfCurrentMonth,
                createdAt = incomeCreatedAt,
                updatedAt = baseDate.minusDays(1)
            )
        )
    }

    /**
     * Fake transactions:
     *  - Dùng budgets đã có id thật (do Room gen) và map theo name.
     *  - Mọi transaction.dateTime và createdAt đều SAU budget.createdAt tương ứng.
     *  - TẤT CẢ dateTime <= baseDate (2025-12-07).
     */
    fun getFakeTransactions(budgets: List<Budget>): List<Transaction> {
        val baseDate = ZonedDateTime.of(
            2025, 12, 7,
            9, 0, 0, 0,
            ZoneId.systemDefault()
        )

        // Map nhanh theo name → lấy đúng id đã được Room gán
        val budgetByName = budgets.associateBy { it.name }

        val foodBudget          = budgetByName.getValue("Ăn uống hàng ngày")
        val transportBudget     = budgetByName.getValue("Đi lại")
        val entertainmentBudget = budgetByName.getValue("Giải trí")
        val shoppingBudget      = budgetByName.getValue("Mua sắm cá nhân")
        val healthBudget        = budgetByName.getValue("Sức khỏe")
        val housingBudget       = budgetByName.getValue("Nhà ở")
        val utilitiesBudget     = budgetByName.getValue("Hóa đơn tiện ích")
        val educationBudget     = budgetByName.getValue("Giáo dục / học tập")
        val incomeBudget        = budgetByName.getValue("Thu nhập cố định")

        val transactions = mutableListOf<Transaction>()

        // =============== ĂN UỐNG: ăn sáng + ăn trưa + cà phê trong ~25 ngày gần đây ===============
        (1..25).forEach { dayOffset ->
            val date = baseDate.minusDays(dayOffset.toLong()) // luôn < baseDate, > createdAt budgets

            // Bữa sáng
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Bữa sáng ngày $dayOffset",
                budgetId = foodBudget.id,
                type = TransactionType.OUTCOME,
                description = "Bánh mì / bún / phở",
                userId = USER_ID,
                category = TransactionCategory.FOOD,
                amount = Random.nextDouble(20_000.0, 40_000.0),
                dateTime = date,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date,
                updatedAt = date.plusHours(1)
            )

            // Ăn trưa
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Ăn trưa văn phòng $dayOffset",
                budgetId = foodBudget.id,
                type = TransactionType.OUTCOME,
                description = "Cơm văn phòng",
                userId = USER_ID,
                category = TransactionCategory.LUNCH,
                amount = Random.nextDouble(45_000.0, 70_000.0),
                dateTime = date.plusHours(4),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date.plusHours(4),
                updatedAt = date.plusHours(5)
            )

            // Cà phê vài ngày 1 lần
            if (dayOffset % 2 == 0) {
                val coffeeTime = date.plusHours(7)
                transactions += Transaction(
                    id = UUID.randomUUID().toString(),
                    name = "Cà phê tại Highlands",
                    budgetId = foodBudget.id,
                    type = TransactionType.OUTCOME,
                    description = "Cà phê với bạn bè",
                    userId = USER_ID,
                    category = TransactionCategory.COFFEE,
                    amount = Random.nextDouble(45_000.0, 80_000.0),
                    dateTime = coffeeTime,
                    image = null,
                    localImagePath = null,
                    location = null,
                    createdAt = coffeeTime,
                    updatedAt = coffeeTime.plusHours(1)
                )
            }
        }

        // =============== ĐI LẠI: Grab + đổ xăng 20 ngày gần đây ===============
        (1..20).forEach { dayOffset ->
            val date = baseDate.minusDays(dayOffset.toLong())
            val isGrab = dayOffset % 3 != 0

            val amount = if (isGrab) {
                Random.nextDouble(25_000.0, 55_000.0)
            } else {
                Random.nextDouble(80_000.0, 120_000.0)
            }

            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = if (isGrab) "Grab đến công ty" else "Đổ xăng xe",
                budgetId = transportBudget.id,
                type = TransactionType.OUTCOME,
                description = "Đi lại hàng ngày",
                userId = USER_ID,
                category = TransactionCategory.TRANSPORTATION,
                amount = amount,
                dateTime = date.plusHours(8),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date.plusHours(8),
                updatedAt = date.plusHours(9)
            )
        }

        // =============== GIẢI TRÍ: phim, cafe, karaoke, Netflix ===============
        listOf(
            "Xem phim chiếu rạp Lotte" to 250_000.0,
            "Đi cafe sách cuối tuần"   to 80_000.0,
            "Karaoke với bạn bè"       to 400_000.0,
            "Đăng ký Netflix tháng này" to 180_000.0
        ).forEachIndexed { index, (name, amount) ->
            val date = baseDate.minusDays((index * 5 + 2).toLong())

            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = name,
                budgetId = entertainmentBudget.id,
                type = TransactionType.OUTCOME,
                description = "Chi phí giải trí",
                userId = USER_ID,
                category = TransactionCategory.ENTERTAINMENT,
                amount = amount,
                dateTime = date.plusHours(20),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date.plusHours(20),
                updatedAt = date.plusHours(21)
            )
        }

        // =============== MUA SẮM: quần áo, giày, đồ gia dụng, quà tặng ===============
        listOf(
            "Mua áo sơ mi Uniqlo"            to (700_000.0 to TransactionCategory.SHOPPING),
            "Mua giày sneaker"               to (1_200_000.0 to TransactionCategory.SHOPPING),
            "Đặt đồ gia dụng trên Shopee"    to (450_000.0 to TransactionCategory.SHOPPING),
            "Mua quà sinh nhật bạn"          to (350_000.0 to TransactionCategory.GIFT)
        ).forEachIndexed { index, (name, pair) ->
            val (amount, category) = pair
            val date = baseDate.minusDays((index * 4 + 3).toLong())

            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = name,
                budgetId = shoppingBudget.id,
                type = TransactionType.OUTCOME,
                description = "Chi tiêu mua sắm",
                userId = USER_ID,
                category = category,
                amount = amount,
                dateTime = date.plusHours(18),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date.plusHours(18),
                updatedAt = date.plusHours(19)
            )
        }

        // =============== SỨC KHỎE ===============
        run {
            val date1 = baseDate.minusDays(6)
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Mua thuốc cảm",
                budgetId = healthBudget.id,
                type = TransactionType.OUTCOME,
                description = "Hiệu thuốc gần nhà",
                userId = USER_ID,
                category = TransactionCategory.HEALTHCARE,
                amount = 180_000.0,
                dateTime = date1.plusHours(19),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date1.plusHours(19),
                updatedAt = date1.plusHours(20)
            )

            val date2 = baseDate.minusDays(18)
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Khám sức khỏe tổng quát",
                budgetId = healthBudget.id,
                type = TransactionType.OUTCOME,
                description = "Khám tại bệnh viện",
                userId = USER_ID,
                category = TransactionCategory.HEALTHCARE,
                amount = 850_000.0,
                dateTime = date2.plusHours(10),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date2.plusHours(10),
                updatedAt = date2.plusHours(11)
            )
        }

        // =============== NHÀ Ở ===============
        run {
            val date = baseDate.withDayOfMonth(3) // 2025-12-03
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Tiền thuê nhà tháng ${baseDate.monthValue}",
                budgetId = housingBudget.id,
                type = TransactionType.OUTCOME,
                description = "Chuyển khoản cho chủ trọ",
                userId = USER_ID,
                category = TransactionCategory.HOUSING,
                amount = 4_800_000.0,
                dateTime = date,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = date,
                updatedAt = date.plusHours(2)
            )
        }

        // =============== HÓA ĐƠN TIỆN ÍCH: điện, nước, internet ===============
        run {
            val dateElectric = baseDate.withDayOfMonth(10) // 2025-12-10 nhưng ta phải đảm bảo < baseDate
            // để chắc chắn < baseDate (7/12), mình lùi về tháng trước (11)
            val safeElectric = dateElectric.minusMonths(1) // 2025-11-10

            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Tiền điện tháng ${baseDate.monthValue - 1}",
                budgetId = utilitiesBudget.id,
                type = TransactionType.OUTCOME,
                description = "Thanh toán EVN",
                userId = USER_ID,
                category = TransactionCategory.UTILITIES,
                amount = 650_000.0,
                dateTime = safeElectric,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = safeElectric,
                updatedAt = safeElectric.plusHours(1)
            )

            val dateWater = baseDate.withDayOfMonth(11).minusMonths(1) // 2025-11-11
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Tiền nước tháng ${baseDate.monthValue - 1}",
                budgetId = utilitiesBudget.id,
                type = TransactionType.OUTCOME,
                description = "Thanh toán tiền nước",
                userId = USER_ID,
                category = TransactionCategory.UTILITIES,
                amount = 220_000.0,
                dateTime = dateWater,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = dateWater,
                updatedAt = dateWater.plusHours(1)
            )

            val dateInternet = baseDate.withDayOfMonth(12).minusMonths(1) // 2025-11-12
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Tiền Internet & TV",
                budgetId = utilitiesBudget.id,
                type = TransactionType.OUTCOME,
                description = "Cước Viettel",
                userId = USER_ID,
                category = TransactionCategory.UTILITIES,
                amount = 280_000.0,
                dateTime = dateInternet,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = dateInternet,
                updatedAt = dateInternet.plusHours(1)
            )
        }

        // =============== GIÁO DỤC ===============
        run {
            val dateCourse = baseDate.minusDays(20)
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Khóa học Android Compose online",
                budgetId = educationBudget.id,
                type = TransactionType.OUTCOME,
                description = "Khóa học Udemy",
                userId = USER_ID,
                category = TransactionCategory.EDUCATION,
                amount = 799_000.0,
                dateTime = dateCourse.plusHours(21),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = dateCourse.plusHours(21),
                updatedAt = dateCourse.plusHours(22)
            )

            val dateBook = baseDate.minusDays(15)
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Mua sách Clean Architecture",
                budgetId = educationBudget.id,
                type = TransactionType.OUTCOME,
                description = "Mua sách kỹ thuật",
                userId = USER_ID,
                category = TransactionCategory.EDUCATION,
                amount = 350_000.0,
                dateTime = dateBook.plusHours(19),
                image = null,
                localImagePath = null,
                location = null,
                createdAt = dateBook.plusHours(19),
                updatedAt = dateBook.plusHours(20)
            )
        }

        // =============== THU NHẬP: lương + thưởng + freelance ===============
        run {
            // Lương tháng (đặt ngày 05/12/2025, < baseDate 07/12)
            val dateSalary = baseDate.withDayOfMonth(5)
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Lương tháng ${baseDate.monthValue}",
                budgetId = incomeBudget.id,
                type = TransactionType.INCOME,
                description = "Lương công ty chuyển khoản",
                userId = USER_ID,
                category = TransactionCategory.SALARY,
                amount = 22_000_000.0,
                dateTime = dateSalary,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = dateSalary,
                updatedAt = dateSalary.plusHours(1)
            )

            // Thưởng dự án (cuối tháng trước)
            val dateBonus = baseDate.withDayOfMonth(28).minusMonths(1) // 2025-11-28
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Thưởng dự án",
                budgetId = incomeBudget.id,
                type = TransactionType.INCOME,
                description = "Thưởng hoàn thành dự án",
                userId = USER_ID,
                category = TransactionCategory.GIFT,
                amount = 3_000_000.0,
                dateTime = dateBonus,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = dateBonus,
                updatedAt = dateBonus.plusHours(1)
            )

            // Freelance
            val dateFreelance = baseDate.minusDays(8) // 2025-11-29
            transactions += Transaction(
                id = UUID.randomUUID().toString(),
                name = "Freelance làm thêm cuối tuần",
                budgetId = incomeBudget.id,
                type = TransactionType.INCOME,
                description = "Dự án lập trình freelance",
                userId = USER_ID,
                category = TransactionCategory.SALARY,
                amount = 2_500_000.0,
                dateTime = dateFreelance,
                image = null,
                localImagePath = null,
                location = null,
                createdAt = dateFreelance,
                updatedAt = dateFreelance.plusHours(1)
            )
        }

        return transactions
    }
}
