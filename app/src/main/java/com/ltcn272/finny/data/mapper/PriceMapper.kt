package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.FeatureDto
import com.ltcn272.finny.data.remote.dto.PriceDataDto
import com.ltcn272.finny.data.remote.dto.PriceResponseDto
import com.ltcn272.finny.data.remote.dto.PriceOptionDto
import com.ltcn272.finny.domain.model.PricePlan
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Convert the API response (new shape) into a flat list of PricePlan domain objects.
 * - Each entry under `data.plan` (e.g. "Pro", "Premium") contains multiple plan options.
 * - Features under `data.Feature` are converted into human-readable strings and attached
 *   to the matching plan tier (by tier name).
 */
fun PriceResponseDto.toDomain(): List<PricePlan> {
    val now = ZonedDateTime.now(ZoneId.systemDefault())
    val plans = mutableListOf<PricePlan>()

    // Build a feature map keyed by tier name (e.g., "Free", "Pro", "Premium")
    val featureMap: Map<String, List<FeatureDto>> = this.data.feature

    // Iterate each tier in the plan map (e.g., "Pro", "Premium")
    this.data.plan.forEach { (tierName, options) ->
        val tierFeatures = featureMap[tierName] ?: emptyList()

        // Convert FeatureDto list to readable strings once per tier
        val readableFeatures: List<String> = tierFeatures.mapNotNull { f ->
            featureDtoToReadable(f)
        }

        options.forEach { option ->
            plans.add(option.toDomainPlan(tierName, readableFeatures, now))
        }
    }

    return plans
}

private fun PriceOptionDto.toDomainPlan(tierName: String, tierFeatures: List<String>, now: ZonedDateTime): PricePlan {
    // Try to coerce price JsonElement to Double. If impossible (e.g., "unlimited"), use -1.0 as sentinel.
    val priceDouble: Double = try {
        when {
            price.isJsonPrimitive && price.asJsonPrimitive.isNumber -> price.asDouble
            price.isJsonPrimitive && price.asJsonPrimitive.isString -> price.asString.toDoubleOrNull() ?: -1.0
            else -> -1.0
        }
    } catch (t: Throwable) {
        -1.0
    }

    val id = "${tierName}_$code"
    val planName = tierName
    val price = priceDouble
    val currency = "usd" // API doesn't return currency per option in provided samples; default to usd
    val period = "${duration} ${unit}" // e.g., "1 month" or "1 year"
    val features = tierFeatures
    val isDefault = false

    return PricePlan(
        id = id,
        planName = planName,
        price = price,
        currency = currency,
        period = period,
        features = features,
        isDefault = isDefault,
        createdAt = now,
        updatedAt = now
    )
}

private fun featureDtoToReadable(f: FeatureDto): String? {
    return try {
        val label = when (f.code.lowercase()) {
            "prompt" -> "Prompt"
            "ai-advance", "ai_advance", "aiadvance" -> "AI Advance"
            else -> f.code.replace('-', ' ').replace('_', ' ').replaceFirstChar { it.uppercase() }
        }

        val value = when {
            f.value.isJsonPrimitive && f.value.asJsonPrimitive.isNumber -> f.value.asNumber.toString()
            f.value.isJsonPrimitive && f.value.asJsonPrimitive.isBoolean -> if (f.value.asBoolean) "✔" else "—"
            f.value.isJsonPrimitive && f.value.asJsonPrimitive.isString -> f.value.asString
            else -> f.value.toString()
        }

        "$label: $value"
    } catch (t: Throwable) {
        null
    }
}
