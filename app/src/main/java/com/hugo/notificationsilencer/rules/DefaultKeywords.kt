package com.hugo.notificationsilencer.rules

import com.hugo.notificationsilencer.data.AppLanguage
import java.util.Locale

object DefaultKeywords {
    val ChineseConservative: Set<String> = linkedSetOf(
        "优惠券",
        "领券",
        "红包",
        "限时秒杀",
        "秒杀",
        "立减",
        "满减",
        "免费领取",
        "专属福利",
        "新人礼包",
        "会员专享",
        "特价",
        "抢购",
        "大促",
        "清仓",
        "爆款",
        "折扣",
        "返现",
        "0元",
        "低至",
        "到手价",
    )

    val ChineseEnhanced: Set<String> = linkedSetOf(
        "活动",
        "上新",
        "推荐",
        "热卖",
        "福利",
        "惊喜",
        "邀你",
        "错过再等",
        "今日份",
        "为你精选",
        "限量",
        "开抢",
        "回馈",
        "解锁",
        "逛逛",
        "看看",
        "订阅",
        "直播中",
        "任务奖励",
    )

    val EnglishConservative: Set<String> = linkedSetOf(
        "sale",
        "discount",
        "coupon",
        "promo code",
        "voucher",
        "free shipping",
        "limited time",
        "limited-time offer",
        "flash sale",
        "clearance",
        "deal",
        "special offer",
        "exclusive offer",
        "save now",
        "save up to",
        "cashback",
        "buy one get one",
        "bogo",
        "doorbuster",
        "price drop",
        "lowest price",
        "today only",
        "ends tonight",
    )

    val EnglishEnhanced: Set<String> = linkedSetOf(
        "new arrivals",
        "back in stock",
        "low stock",
        "almost gone",
        "selling fast",
        "trending now",
        "recommended for you",
        "picked for you",
        "wishlist",
        "cart",
        "abandoned cart",
        "complete your purchase",
        "checkout",
        "members only",
        "vip",
        "early access",
        "unlock",
        "reward",
        "points",
        "bonus",
        "giveaway",
        "last chance",
        "don't miss",
        "shop now",
        "subscribe",
    )

    val Conservative: Set<String> = ChineseConservative
    val Enhanced: Set<String> = ChineseEnhanced

    fun conservativeFor(language: AppLanguage): Set<String> {
        return when (language.effective()) {
            AppLanguage.English -> EnglishConservative
            AppLanguage.Chinese -> ChineseConservative
            AppLanguage.System -> ChineseConservative
        }
    }

    fun enhancedFor(language: AppLanguage): Set<String> {
        return when (language.effective()) {
            AppLanguage.English -> EnglishEnhanced
            AppLanguage.Chinese -> ChineseEnhanced
            AppLanguage.System -> ChineseEnhanced
        }
    }
}

fun AppLanguage.effective(): AppLanguage {
    if (this != AppLanguage.System) return this
    return if (Locale.getDefault().language.equals("zh", ignoreCase = true)) {
        AppLanguage.Chinese
    } else {
        AppLanguage.English
    }
}
