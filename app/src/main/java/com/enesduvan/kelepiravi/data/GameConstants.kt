package com.enesduvan.kelepiravi.data

object GameConstants {
    const val DEFAULT_USER_ID = 1
    const val INITIAL_BALANCE = "25000.0"
    const val INITIAL_DAY = 1

    const val MARKET_BATCH_SIZE = 12

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
    const val MARKET_MIN_SALES_RATIO = 0.50
    const val MARKET_SALES_RATIO_RANGE = 0.40

    const val DAILY_EVENT_CHANCE = 0.25
    const val DEFAULT_CONDITION_BIAS = -0.02
    const val MIN_DAILY_CHANGE = -0.35
    const val MAX_DAILY_CHANGE = 0.50
    const val PURCHASE_VALUE_FLOOR_RATIO = 0.10
}

object BargainConstants {
    const val STARTING_PATIENCE = 100
    const val MOOD_HAPPY_MIN = 80
    const val MOOD_UNSURE_MIN = 50
    const val MOOD_TENSE_MIN = 20

    const val BUY_SUGGESTED_RATIO = 0.90
    const val BUY_ACCEPT_RATIO = 0.95
    const val BUY_MAYBE_RATIO = 0.85
    const val BUY_LOW_RATIO = 0.70
    const val BUY_COUNTER_ACCEPT_CHANCE = 0.40
    const val BUY_COUNTER_RATIO = 0.90

    const val SELL_INITIAL_MIN_RATIO = 0.80
    const val SELL_INITIAL_RANGE = 0.10
    const val SELL_ACCEPT_RATIO = 1.05
    const val SELL_COUNTER_RATIO = 1.15
    const val SELL_HIGH_RATIO = 1.30
    const val SELL_COUNTER_DISCOUNT = 0.95

    const val PATIENCE_REWARD = 10
    const val PATIENCE_SMALL_PENALTY = 5
    const val PATIENCE_MEDIUM_PENALTY = 15
    const val PATIENCE_LARGE_PENALTY = 30
    const val PATIENCE_SELL_SMALL_PENALTY = 10
    const val PATIENCE_SELL_MEDIUM_PENALTY = 25
    const val PATIENCE_SELL_LARGE_PENALTY = 40
}
