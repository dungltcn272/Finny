package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.FeatureDto
import com.ltcn272.finny.data.remote.dto.PriceApiResponseDto
import com.ltcn272.finny.domain.model.PricePlan

fun PriceApiResponseDto.toPriceDomain(): List<PricePlan> {
    val plans = mutableListOf<PricePlan>()
    val currency = if (this.version == "vi") "VND" else "USD"

    this.data.plan.forEach { (tierName, options) ->
        val featureList = this.data.feature[tierName]?.mapNotNull { featureDto ->
            featureDtoToReadableString(featureDto)
        } ?: emptyList()

        options.forEach { optionDto ->
            plans.add(
                PricePlan(
                    tier = tierName,
                    code = optionDto.code,
                    display = optionDto.display,
                    price = optionDto.price,
                    currency = currency,
                    duration = optionDto.duration,
                    unit = optionDto.unit,
                    features = featureList
                )
            )
        }
    }
    return plans
}

private fun featureDtoToReadableString(feature: FeatureDto): String? {
    return try {
        val featureName = when (feature.code.lowercase()) {
            "ai-advance", "ai_advance" -> "Advanced AI"
            "prompt" -> "Number of prompts"
            else -> feature.code.replaceFirstChar { it.uppercase() }
        }

        val featureValue = when {
            feature.value.isJsonPrimitive && feature.value.asJsonPrimitive.isBoolean -> {
                if (feature.value.asBoolean) "Yes" else "No"
            }
            feature.value.isJsonPrimitive && feature.value.asJsonPrimitive.isString -> {
                if (feature.value.asString.equals("unlimited", ignoreCase = true)) "Unlimited"
                else feature.value.asString
            }
            feature.value.isJsonPrimitive && feature.value.asJsonPrimitive.isNumber -> {
                feature.value.asNumber.toString()
            }
            else -> feature.value.toString()
        }

        "$featureName: $featureValue"
    } catch (e: Exception) {
        null
    }
}
