package com.hugo.notificationsilencer.rules

object DefaultKeywords {
    val Conservative: Set<String> = linkedSetOf(
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

    val Enhanced: Set<String> = linkedSetOf(
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
}
