package com.enesduvan.kelepiravi.data

object GameConstants {
    const val DEFAULT_USER_ID = 1
    const val INITIAL_BALANCE = 25000L
    const val INITIAL_DAY = 1

    const val MARKET_BATCH_SIZE = 50

    const val XP_LEVEL_FACTOR = 50
    const val BUY_XP = 10
    const val SELL_BASE_XP = 10
    const val REPAIR_XP = 15
    const val DAILY_LOGIN_XP = 20
    const val DAILY_LOGIN_BONUS = 50.0
    const val PROFIT_PER_XP = 100.0

    const val PERFECT_CONDITION_MULTIPLIER = 1.0
    const val REPAIR_COST_GAIN_RATE = 0.60
    const val SELL_PRICE_ROUNDING_SCALE = 100.0

    const val MARKET_VALUE_VARIANCE_RATE = 0.15
    const val MARKET_MIN_ITEM_VALUE = 50
    const val MARKET_MIN_SALES_VALUE = 30

    // Ch8: Ekonomi Dengesi — kâr marjları daha da daraltıldı
    const val MARKET_MIN_SALES_RATIO = 0.75   // eskiden 0.62
    const val MARKET_SALES_RATIO_RANGE = 0.15  // eskiden 0.22

    // Ch6: Dolandırıcı üretim şansı
    const val SCAMMER_CHANCE = 0.05            // %5 ihtimalle dolandırıcı satıcı

    const val DAILY_EVENT_CHANCE = 0.25
    const val DEFAULT_CONDITION_BIAS = -0.02
    const val MIN_DAILY_CHANGE = -0.35
    const val MAX_DAILY_CHANGE = 0.50
    const val PURCHASE_VALUE_FLOOR_RATIO = 0.10

    // Ch6: Günlük tamir limiti
    const val DAILY_REPAIR_LIMIT = 2
    // Ch10: Tamir başarısızlık olasılığı (Usta seviyesi 1 için temel risk)
    const val REPAIR_FAILURE_CHANCE = 0.40

    // Ch8: Ekonomi Dengesi
    const val DAILY_RENT_COST = 500.0
    const val DAILY_TAX_RATE = 0.05
}

object BargainConstants {
    const val STARTING_PATIENCE = 100
    const val MOOD_HAPPY_MIN = 80
    const val MOOD_UNSURE_MIN = 50
    const val MOOD_TENSE_MIN = 20

    // Ch6: Daha sıkı eşikler — satıcı kolay tamam demiyor
    const val BUY_SUGGESTED_RATIO = 0.88       // eskiden 0.90
    const val BUY_ACCEPT_RATIO = 0.92          // eskiden 0.95
    const val BUY_MAYBE_RATIO = 0.80           // eskiden 0.85
    const val BUY_LOW_RATIO = 0.68             // eskiden 0.70
    const val BUY_COUNTER_ACCEPT_CHANCE = 0.35 // eskiden 0.40 (daha az kolay kabul)
    const val BUY_COUNTER_RATIO = 0.92         // eskiden 0.90

    const val SELL_INITIAL_MIN_RATIO = 0.80
    const val SELL_INITIAL_RANGE = 0.10
    // Ch8: Satıcılar (biz) malı satarken alıcılar daha cimri olacak
    const val SELL_ACCEPT_RATIO = 1.04         // eskiden 1.08
    const val SELL_COUNTER_RATIO = 1.12        // eskiden 1.18
    const val SELL_HIGH_RATIO = 1.22           // eskiden 1.35
    const val SELL_COUNTER_DISCOUNT = 0.95

    const val PATIENCE_REWARD = 10
    const val PATIENCE_SMALL_PENALTY = 5
    const val PATIENCE_MEDIUM_PENALTY = 15
    const val PATIENCE_LARGE_PENALTY = 30
    const val PATIENCE_SELL_SMALL_PENALTY = 10
    const val PATIENCE_SELL_MEDIUM_PENALTY = 25
    const val PATIENCE_SELL_LARGE_PENALTY = 40

    // Ch6: Aynı teklifi tekrar gönderince ekstra sabır cezası
    const val PATIENCE_REPEAT_OFFER_PENALTY = 12
}
