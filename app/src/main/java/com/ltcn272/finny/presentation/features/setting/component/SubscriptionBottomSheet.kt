package com.ltcn272.finny.presentation.features.setting.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.common.ui.SegmentedControl
import com.ltcn272.finny.presentation.common.util.CurrencyUtils
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    pricePlans: List<PricePlan> = emptyList(),
    periodOptions: List<String> = emptyList(),
    selectedPeriodIndex: Int = 0,
    onPeriodSelected: (Int) -> Unit = {},
    selectedPlanId: String? = null,
    onPlanSelected: (String) -> Unit = {},
    isSubscribing: Boolean = false,
    onSubscribe: (planId: String, periodIndex: Int) -> Unit = { _, _ -> }
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = true,
    ) {
        BoxWithConstraints {
            val maxHeight = this.maxHeight * 0.95f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                // --- Top part: Image and Plan Selection (Not Scrollable) ---
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.intro_money),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .align(Alignment.TopCenter),
                        contentScale = ContentScale.Crop
                    )

                    CircleIconButton(
                        onClick = onDismiss,
                        icon = R.drawable.ic_close,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(100.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                val tiers: List<String> = periodOptions.ifEmpty {
                                    pricePlans.map { it.planName }.distinct()
                                        .ifEmpty { listOf("Pro", "Premium") }
                                }

                                SegmentedControl(
                                    modifier = Modifier.fillMaxWidth(),
                                    options = tiers,
                                    selected = tiers.getOrNull(selectedPeriodIndex)
                                        ?: tiers.first(),
                                    onOptionClicked = { option ->
                                        val idx = tiers.indexOf(option)
                                        if (idx >= 0) onPeriodSelected(idx)
                                    },
                                    indicatorPadding = 3.dp,
                                    titleForItem = { it },
                                    contentPadding = PaddingValues(vertical = 15.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val selectedTier =
                                    tiers.getOrNull(selectedPeriodIndex) ?: tiers.first()
                                val plansForTier: List<PricePlan> =
                                    pricePlans.filter { it.planName == selectedTier }
                                        .sortedByDescending { it.price }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    if (plansForTier.isEmpty()) {
                                        val now = ZonedDateTime.now()
                                        PlanCard(
                                            plan = PricePlan(
                                                id = "1",
                                                planName = "Pro",
                                                period = "One Year",
                                                price = 49.9,
                                                currency = "USD",
                                                features = listOf(),
                                                isDefault = true,
                                                createdAt = now,
                                                updatedAt = now
                                            ),
                                            isSelected = true,
                                            onClick = {}
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        PlanCard(
                                            plan = PricePlan(
                                                id = "2",
                                                planName = "Pro",
                                                period = "Six Month",
                                                price = 24.9,
                                                currency = "USD",
                                                features = listOf(),
                                                isDefault = false,
                                                createdAt = now,
                                                updatedAt = now
                                            ),
                                            isSelected = false,
                                            onClick = {}
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        PlanCard(
                                            plan = PricePlan(
                                                id = "3",
                                                planName = "Pro",
                                                period = "One Month",
                                                price = 4.9,
                                                currency = "USD",
                                                features = listOf(),
                                                isDefault = false,
                                                createdAt = now,
                                                updatedAt = now
                                            ),
                                            isSelected = false,
                                            onClick = {}
                                        )
                                    } else {
                                        plansForTier.forEach { plan ->
                                            val selected = plan.id == selectedPlanId
                                            PlanCard(
                                                plan = plan,
                                                isSelected = selected,
                                                onClick = { onPlanSelected(plan.id) }
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "What's included?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val tiersForFeatures: List<String> = listOf("Freemium", "Pro", "Premium")
                    val featureMap = getFeatureMap(pricePlans, tiersForFeatures)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "Feature",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        tiersForFeatures.forEach { tier ->
                            Text(
                                text = tier,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    featureMap.forEach { (featureName, tierValues) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                featureName,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            tiersForFeatures.forEach { tier ->
                                val value = tierValues[tier]
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (value) {
                                        "true" -> Icon(
                                            painter = painterResource(id = R.drawable.ic_check),
                                            contentDescription = "Included",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )

                                        "false" -> {}
                                        else -> Text(
                                            value ?: "-",
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                val currentPlan = pricePlans.find { it.id == selectedPlanId }
                Surface(
                    onClick = {
                        val planId = selectedPlanId ?: ""
                        if (!isSubscribing && planId.isNotEmpty()) {
                            onSubscribe(planId, selectedPeriodIndex)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(100),
                    color = Color(0xFFFF9500),
                    enabled = !isSubscribing && selectedPlanId != null
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Subscribe",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val priceText = currentPlan?.let {
                            "Plan auto-renews for ${
                                formatPrice(
                                    it.price,
                                    it.currency
                                )
                            }/${it.period.lowercase()} until canceled"
                        } ?: "Plan auto-renews until canceled"

                        Text(
                            text = priceText,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PlanCard(plan: PricePlan, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.LightGray.copy(alpha = 0.3f),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFFFF9500)) else null,
        interactionSource = remember { MutableInteractionSource() } // Disables ripple
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = plan.period, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPrice(plan.price, plan.currency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Selected",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun getFeatureMap(
    pricePlans: List<PricePlan>,
    tiersForFeatures: List<String>
): Map<String, Map<String, String?>> {
    val featureMap = linkedMapOf<String, MutableMap<String, String?>>()
    val allFeatureNames = listOf("Prompt", "AI Advance")

    allFeatureNames.forEach { featureName ->
        featureMap[featureName] = mutableMapOf()
    }

    tiersForFeatures.forEach { tier ->
        val plan = pricePlans.firstOrNull { it.planName == tier }
        val displayFeatures = plan?.features ?: when (tier) {
            "Pro" -> listOf("Prompt:500", "AI Advance:true")
            "Premium" -> listOf("Prompt:Unlimited", "AI Advance:true")
            "Freemium" -> listOf("Prompt:100", "AI Advance:false")
            else -> emptyList()
        }

        val featuresAsMap = displayFeatures.associate {
            val parts = it.split(":", limit = 2)
            parts[0].trim() to parts.getOrNull(1)?.trim()
        }

        allFeatureNames.forEach { featureName ->
            featureMap[featureName]?.put(tier, featuresAsMap[featureName])
        }
    }
    return featureMap
}

private fun formatPrice(price: Double, currency: String): String {
    if (price < 0.0) return "-"
    val locale = Locale.US
    return try {
        when (currency.lowercase(Locale.getDefault())) {
            "usd" -> String.format(locale, "$%.1f", price)
            else -> {
                val symbol = CurrencyUtils.getCurrencySymbolForCurrentLocale()
                String.format(Locale.getDefault(), "%s%.1f", symbol, price)
            }
        }
    } catch (t: Throwable) {
        price.toString()
    }
}
