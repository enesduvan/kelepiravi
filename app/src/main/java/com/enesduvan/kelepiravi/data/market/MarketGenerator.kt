package com.enesduvan.kelepiravi.data.market

import com.enesduvan.kelepiravi.data.GameConstants
import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Prosedürel pazar üretim motoru.
 * imageName değerleri projedeki gerçek drawable isimleriyle birebir eşleşir.
 */
object MarketGenerator {

    // ─── Mevcut Drawable Havuzu ───────────────────────────────────────────────
    private data class ImagePool(
        val clean: List<String>,
        val damaged: List<String>
    ) {
        fun pickFor(condition: String): String {
            val rng = Random.Default
            return when {
                condition.contains("Kusursuz") || condition.contains("Temiz") || condition.contains("Hafif") ->
                    if (clean.isNotEmpty()) clean.random(rng) else (damaged + clean).random(rng)
                else ->
                    if (damaged.isNotEmpty()) damaged.random(rng) else clean.random(rng)
            }
        }
    }

    private val IMAGE_POOLS = mapOf(
        "bez_canta" to ImagePool(clean = listOf("bez_canta"), damaged = listOf("bez_canta")),
        "bogaz_koprusu" to ImagePool(clean = listOf("bogaz_koprusu"), damaged = listOf("bogaz_koprusu")),
        "f_16" to ImagePool(clean = listOf("f_16"), damaged = listOf("f_16")),
        "kamera_resim" to ImagePool(clean = listOf("kamera_resim"), damaged = listOf("kamera_resim")),
        "koy_kahvesi" to ImagePool(clean = listOf("koy_kahvesi"), damaged = listOf("koy_kahvesi")),
        "mini_firin" to ImagePool(clean = listOf("mini_firin"), damaged = listOf("mini_firin")),
        "nasa_bilgisayari" to ImagePool(clean = listOf("nasa_bilgisayari"), damaged = listOf("nasa_bilgisayari")),
        "oyun_kulakligi" to ImagePool(clean = listOf("oyun_kulakligi"), damaged = listOf("oyun_kulakligi")),
        "satilik_kaynana" to ImagePool(clean = listOf("satilik_kaynana"), damaged = listOf("satilik_kaynana")),
        "rubber_duck" to ImagePool(
            clean = listOf("rubber_duck_1", "rubber_duck_2", "rubber_duck_3", "rubber_duck_4", "rubber_duck_5", "rubber_duck_6", "rubber_duck_7"),
            damaged = listOf("rubber_duck_1", "rubber_duck_2", "rubber_duck_3", "rubber_duck_4", "rubber_duck_5", "rubber_duck_6", "rubber_duck_7")
        ),
        "rubber_duck_egg" to ImagePool(
            clean = listOf("rubber_duck_egg_1", "rubber_duck_egg_2", "rubber_duck_egg_3", "rubber_duck_egg_4", "rubber_duck_egg_5"),
            damaged = listOf("rubber_duck_egg_1", "rubber_duck_egg_2", "rubber_duck_egg_3", "rubber_duck_egg_4", "rubber_duck_egg_5")
        ),
        "smartphone_clean" to ImagePool(
            clean = listOf("smartphone_clean_1", "smartphone_clean_2", "smartphone_clean_3", "smartphone_clean_4", "smartphone_clean_5", "smartphone_clean_6", "smartphone_clean_7", "smartphone_clean_8", "smartphone_clean_9", "smartphone_clean_10"),
            damaged = listOf("smartphone_clean_1", "smartphone_clean_2", "smartphone_clean_3", "smartphone_clean_4", "smartphone_clean_5", "smartphone_clean_6", "smartphone_clean_7", "smartphone_clean_8", "smartphone_clean_9", "smartphone_clean_10")
        ),
        "smartphone_cracked" to ImagePool(
            clean = listOf("smartphone_cracked_1", "smartphone_cracked_2", "smartphone_cracked_3", "smartphone_cracked_4", "smartphone_cracked_5", "smartphone_cracked_6", "smartphone_cracked_7", "smartphone_cracked_8", "smartphone_cracked_9", "smartphone_cracked_10"),
            damaged = listOf("smartphone_cracked_1", "smartphone_cracked_2", "smartphone_cracked_3", "smartphone_cracked_4", "smartphone_cracked_5", "smartphone_cracked_6", "smartphone_cracked_7", "smartphone_cracked_8", "smartphone_cracked_9", "smartphone_cracked_10")
        ),
        "smartphone_scratched" to ImagePool(
            clean = listOf("smartphone_scratched_1", "smartphone_scratched_2", "smartphone_scratched_3"),
            damaged = listOf("smartphone_scratched_1", "smartphone_scratched_2", "smartphone_scratched_3")
        ),
        "smartphone_dirty" to ImagePool(
            clean = listOf("smartphone_dirty_1", "smartphone_dirty_2", "smartphone_dirty_3", "smartphone_dirty_4"),
            damaged = listOf("smartphone_dirty_1", "smartphone_dirty_2", "smartphone_dirty_3", "smartphone_dirty_4")
        ),
        "smartphone_melted_back" to ImagePool(
            clean = listOf("smartphone_melted_back"),
            damaged = listOf("smartphone_melted_back")
        ),
        "tablet_galaxy" to ImagePool(
            clean = listOf("tablet_galaxy"),
            damaged = listOf("tablet_galaxy")
        ),
        "tablet_scratched" to ImagePool(
            clean = listOf("tablet_scratched_1", "tablet_scratched_2"),
            damaged = listOf("tablet_scratched_1", "tablet_scratched_2")
        ),
        "blender" to ImagePool(
            clean = listOf("blender_1", "blender_2", "blender_3", "blender_4", "blender_5"),
            damaged = listOf("blender_1", "blender_2", "blender_3", "blender_4", "blender_5")
        ),
        "coffee_maker" to ImagePool(
            clean = listOf("coffee_maker_1", "coffee_maker_2"),
            damaged = listOf("coffee_maker_1", "coffee_maker_2")
        ),
        "mini_fridge" to ImagePool(
            clean = listOf("mini_fridge_1", "mini_fridge_2", "mini_fridge_3"),
            damaged = listOf("mini_fridge_1", "mini_fridge_2", "mini_fridge_3")
        ),
        "headphones_duct_tape" to ImagePool(
            clean = listOf("headphones_duct_tape_1", "headphones_duct_tape_2", "headphones_duct_tape_3"),
            damaged = listOf("headphones_duct_tape_1", "headphones_duct_tape_2", "headphones_duct_tape_3")
        ),
        "classic_watch" to ImagePool(
            clean = listOf("classic_watch_1", "classic_watch_2", "classic_watch_3"),
            damaged = listOf("classic_watch_1", "classic_watch_2", "classic_watch_3")
        ),
        "classic_bicycle" to ImagePool(
            clean = listOf("classic_bicycle_1"),
            damaged = listOf("classic_bicycle_1")
        ),
        "canvas_backpack" to ImagePool(
            clean = listOf("canvas_backpack_1", "canvas_backpack_2"),
            damaged = listOf("canvas_backpack_1", "canvas_backpack_2")
        ),
        "dirty_jersey" to ImagePool(
            clean = listOf("dirty_jersey_19"),
            damaged = listOf("dirty_jersey_19")
        ),
        "gamepad_broken" to ImagePool(
            clean = listOf("gamepad_broken_1"),
            damaged = listOf("gamepad_broken_1")
        ),
        "monitor_cracked" to ImagePool(
            clean = listOf("monitor_cracked_1", "monitor_cracked_2", "monitor_cracked_3"),
            damaged = listOf("monitor_cracked_1", "monitor_cracked_2", "monitor_cracked_3")
        ),
        "vr_headset_clean" to ImagePool(
            clean = listOf("vr_headset_clean_1"),
            damaged = listOf("vr_headset_clean_1")
        ),
        "vr_headset_scratched" to ImagePool(
            clean = listOf("vr_headset_scratched_1"),
            damaged = listOf("vr_headset_scratched_1")
        ),
        "mech_keyboard_clean" to ImagePool(
            clean = listOf("mech_keyboard_clean_1"),
            damaged = listOf("mech_keyboard_clean_1")
        ),
        "mech_keyboard_missing_key" to ImagePool(
            clean = listOf("mech_keyboard_missing_key_1"),
            damaged = listOf("mech_keyboard_missing_key_1")
        ),
        "acoustic_guitar_clean" to ImagePool(
            clean = listOf("acoustic_guitar_clean_1"),
            damaged = listOf("acoustic_guitar_clean_1")
        ),
        "acoustic_guitar_broken_string" to ImagePool(
            clean = listOf("acoustic_guitar_broken_string_1"),
            damaged = listOf("acoustic_guitar_broken_string_1")
        ),
        "leather_jacket_clean" to ImagePool(
            clean = listOf("leather_jacket_clean_1"),
            damaged = listOf("leather_jacket_clean_1")
        ),
        "leather_jacket_torn" to ImagePool(
            clean = listOf("leather_jacket_torn_1"),
            damaged = listOf("leather_jacket_torn_1")
        ),
        "city_bike" to ImagePool(
            clean = listOf("blackcitybike", "bluecitybike", "greencitybike", "lightbluecitybike"),
            damaged = listOf("blackcitybike", "bluecitybike", "greencitybike", "lightbluecitybike")
        ),
        "mountain_bike" to ImagePool(
            clean = listOf("darkgreymountainbike", "matteblackmountainbike", "tealmountainbike", "tealwhitemountainbike"),
            damaged = listOf("darkgreymountainbike", "matteblackmountainbike", "tealmountainbike", "tealwhitemountainbike")
        ),
        "retro_console" to ImagePool(
            clean = listOf("classicgreyretroconsole", "retroconsolewithscreena", "retroconsolewithscreenb", "modernsilverconsole", "classicretroconsole", "turkokonsoleretro"),
            damaged = listOf("classicgreyretroconsole", "retroconsolewithscreena", "retroconsolewithscreenb", "modernsilverconsole", "classicretroconsole", "turkokonsoleretro")
        ),
        "vr_headset_extra" to ImagePool(
            clean = listOf("chunkyvrheadset", "genericvrheadseta", "sleekvrheadset", "vrheadsetwithaudio"),
            damaged = listOf("chunkyvrheadset", "genericvrheadseta", "sleekvrheadset", "vrheadsetwithaudio")
        ),
        "rc_car" to ImagePool(
            clean = listOf("redrcbuggy", "redblackrcbuggy", "redwhitercbuggy"),
            damaged = listOf("redrcbuggy", "redblackrcbuggy", "redwhitercbuggy")
        ),
        "rc_truck_muddy" to ImagePool(
            clean = listOf("muddyredrctruck"),
            damaged = listOf("muddyredrctruck")
        ),
        "electric_scooter" to ImagePool(
            clean = listOf("scooter", "scooter2", "scooter3"),
            damaged = listOf("scooter", "scooter2", "scooter3")
        ),
        "jigsaw_puzzle" to ImagePool(
            clean = listOf("boxedbluejigsawpuzzle", "openbluejigsawpuzzle", "squarebluepuzzle", "bluepuzzlebox"),
            damaged = listOf("boxedbluejigsawpuzzle", "openbluejigsawpuzzle", "squarebluepuzzle", "bluepuzzlebox")
        ),
        "toy_blocks" to ImagePool(
            clean = listOf("boxofcolorfultoyblocks", "boxofgreytoyblocks", "boxoftantoyblocks", "cardboardboxgreyblocks"),
            damaged = listOf("boxofcolorfultoyblocks", "boxofgreytoyblocks", "boxoftantoyblocks", "cardboardboxgreyblocks")
        ),
        "chess_set" to ImagePool(
            clean = listOf("woodenchessseta", "woodenchesssetb", "woodenchesssetc", "woodenchesssetd"),
            damaged = listOf("woodenchessseta", "woodenchesssetb", "woodenchesssetc", "woodenchesssetd")
        ),
        "car_tire" to ImagePool(
            clean = listOf("flatcartire", "standingcartirea", "standingcartireb", "standingcartirec"),
            damaged = listOf("flatcartire", "standingcartirea", "standingcartireb", "standingcartirec")
        ),
        "luxury_watch" to ImagePool(
            clean = listOf("classicbluewatch", "luxuryreginowatch", "leathersmartwatch"),
            damaged = listOf("classicbluewatch", "luxuryreginowatch", "leathersmartwatch")
        ),
        "smashed_watch" to ImagePool(
            clean = listOf("smashedclassicwatch"),
            damaged = listOf("smashedclassicwatch")
        ),
        "gaming_laptop_v2" to ImagePool(
            clean = listOf("gaminglaptop", "heavydutygaminglaptop"),
            damaged = listOf("gaminglaptop", "heavydutygaminglaptop")
        ),
        "laptop_damaged" to ImagePool(
            clean = listOf("genericsilverlaptop"),
            damaged = listOf("damagedsilverlaptop")
        ),
        "retro_mobile" to ImagePool(
            clean = listOf("retromobilephone"),
            damaged = listOf("retromobilephone")
        ),
        "smartphone_scratched_v2" to ImagePool(
            clean = listOf("scratchedsmartphone"),
            damaged = listOf("scratchedsmartphone")
        ),
        "smartphone_missing" to ImagePool(
            clean = listOf("smartphone_05_missing_parts_varyasyon1", "smartphone_05_missing_parts_varyasyon2", "smartphone_05_missing_parts_varyasyon3", "smartphone_05_missing_parts_varyasyon4"),
            damaged = listOf("smartphone_05_missing_parts_varyasyon1", "smartphone_05_missing_parts_varyasyon2", "smartphone_05_missing_parts_varyasyon3", "smartphone_05_missing_parts_varyasyon4")
        ),
        "smartphone_scrap" to ImagePool(
            clean = listOf("smartphone_10_scrap_varyasyon1", "smartphone_10_scrap_varyasyon2", "smartphone_10_scrap_varyasyon3", "smartphone_10_scrap_varyasyon4"),
            damaged = listOf("smartphone_10_scrap_varyasyon1", "smartphone_10_scrap_varyasyon2", "smartphone_10_scrap_varyasyon3", "smartphone_10_scrap_varyasyon4")
        ),
        "smartphone_custom" to ImagePool(
            clean = listOf("smartphone_09_custom_painted_varyasyon1"),
            damaged = listOf("smartphone_09_custom_painted_varyasyon1")
        ),
        "gaming_headset_new" to ImagePool(
            clean = listOf("gaming_headset_01_flawless_varyasyon1", "gaming_headset_01_flawless_varyasyon2", "gaming_headset_01_flawless_varyasyon3", "gaming_headset_01_flawless_varyasyon4", "gaming_headset_02_lightly_used_varyasyon1", "gaming_headset_02_lightly_used_varyasyon2", "gaming_headset_02_lightly_used_varyasyon3", "gaming_headset_02_lightly_used_varyasyon4", "gaming_headset_03_worn_out_varyasyon1", "gaming_headset_03_worn_out_varyasyon2", "gaming_headset_03_worn_out_varyasyon3", "gaming_headset_03_worn_out_varyasyon4", "gaming_headset_05_missing_parts_varyasyon1", "gaming_headset_05_missing_parts_varyasyon2", "gaming_headset_05_missing_parts_varyasyon3", "gaming_headset_05_missing_parts_varyasyon4", "gaming_headset_06_repaired_duct_tape_varyasyon1", "gaming_headset_06_repaired_duct_tape_varyasyon2", "gaming_headset_06_repaired_duct_tape_varyasyon3", "gaming_headset_06_repaired_duct_tape_varyasyon4", "gaming_headset_08_burned_varyasyon1", "gaming_headset_08_burned_varyasyon2", "gaming_headset_08_burned_varyasyon3", "gaming_headset_08_burned_varyasyon4", "gaming_headset_09_custom_painted_varyasyon1", "gaming_headset_09_custom_painted_varyasyon2", "gaming_headset_09_custom_painted_varyasyon3", "gaming_headset_09_custom_painted_varyasyon4", "gaming_headset_10_scrap_varyasyon1", "gaming_headset_10_scrap_varyasyon2", "gaming_headset_10_scrap_varyasyon3", "gaming_headset_10_scrap_varyasyon4"),
            damaged = listOf("gaming_headset_04_heavily_damaged_varyasyon1", "gaming_headset_04_heavily_damaged_varyasyon3", "gaming_headset_04_heavily_damaged_varyasyon4", "gaming_headset_07_dirty_rusty_varyasyon1", "gaming_headset_07_dirty_rusty_varyasyon2", "gaming_headset_07_dirty_rusty_varyasyon3", "gaming_headset_07_dirty_rusty_varyasyon4")
        ),
        "bluetooth_speaker_new" to ImagePool(
            clean = listOf("bluetooth_speaker_01_flawless_varyasyon1", "bluetooth_speaker_01_flawless_varyasyon2", "bluetooth_speaker_01_flawless_varyasyon3", "bluetooth_speaker_01_flawless_varyasyon4", "bluetooth_speaker_02_lightly_used_varyasyon1", "bluetooth_speaker_02_lightly_used_varyasyon2", "bluetooth_speaker_02_lightly_used_varyasyon3", "bluetooth_speaker_02_lightly_used_varyasyon4", "bluetooth_speaker_03_worn_out_varyasyon2", "bluetooth_speaker_03_worn_out_varyasyon3", "bluetooth_speaker_05_missing_parts_varyasyon3", "bluetooth_speaker_06_repaired_duct_tape_varyasyon2", "bluetooth_speaker_06_repaired_duct_tape_varyasyon3", "bluetooth_speaker_06_repaired_duct_tape_varyasyon4", "bluetooth_speaker_08_burned_varyasyon1", "bluetooth_speaker_08_burned_varyasyon2", "bluetooth_speaker_08_burned_varyasyon3", "bluetooth_speaker_08_burned_varyasyon4", "bluetooth_speaker_09_custom_painted_varyasyon1", "bluetooth_speaker_09_custom_painted_varyasyon2", "bluetooth_speaker_09_custom_painted_varyasyon3", "bluetooth_speaker_09_custom_painted_varyasyon4", "bluetooth_speaker_10_scrap_varyasyon2", "bluetooth_speaker_10_scrap_varyasyon3", "bluetooth_speaker_10_scrap_varyasyon4"),
            damaged = listOf("bluetooth_speaker_07_dirty_rusty_varyasyon1", "bluetooth_speaker_07_dirty_rusty_varyasyon2", "bluetooth_speaker_07_dirty_rusty_varyasyon3", "bluetooth_speaker_07_dirty_rusty_varyasyon4")
        ),
        "game_console_new" to ImagePool(
            clean = listOf("game_console_01_flawless_varyasyon1", "game_console_01_flawless_varyasyon2", "game_console_01_flawless_varyasyon3", "game_console_01_flawless_varyasyon4", "game_console_02_lightly_used_varyasyon1", "game_console_02_lightly_used_varyasyon2", "game_console_02_lightly_used_varyasyon3", "game_console_02_lightly_used_varyasyon4", "game_console_03_worn_out_varyasyon1", "game_console_03_worn_out_varyasyon2", "game_console_03_worn_out_varyasyon3", "game_console_03_worn_out_varyasyon4", "game_console_05_missing_parts_varyasyon1", "game_console_05_missing_parts_varyasyon2", "game_console_05_missing_parts_varyasyon3", "game_console_05_missing_parts_varyasyon4", "game_console_06_repaired_duct_tape_varyasyon1", "game_console_06_repaired_duct_tape_varyasyon2", "game_console_06_repaired_duct_tape_varyasyon4", "game_console_08_burned_varyasyon1", "game_console_08_burned_varyasyon2", "game_console_08_burned_varyasyon3", "game_console_08_burned_varyasyon4", "game_console_09_custom_painted_varyasyon1", "game_console_09_custom_painted_varyasyon2", "game_console_09_custom_painted_varyasyon4"),
            damaged = listOf("game_console_04_heavily_damaged_varyasyon1", "game_console_04_heavily_damaged_varyasyon2", "game_console_04_heavily_damaged_varyasyon3", "game_console_04_heavily_damaged_varyasyon4", "game_console_07_dirty_rusty_varyasyon1", "game_console_07_dirty_rusty_varyasyon2", "game_console_07_dirty_rusty_varyasyon3", "game_console_07_dirty_rusty_varyasyon4")
        ),
        "gaming_laptop_new" to ImagePool(
            clean = listOf("gaming_laptop_01_flawless_varyasyon2", "gaming_laptop_01_flawless_varyasyon3", "gaming_laptop_01_flawless_varyasyon4", "gaming_laptop_02_lightly_used_varyasyon2", "gaming_laptop_03_worn_out_varyasyon1", "gaming_laptop_03_worn_out_varyasyon2", "gaming_laptop_03_worn_out_varyasyon3", "gaming_laptop_03_worn_out_varyasyon4", "gaming_laptop_05_missing_parts_varyasyon1", "gaming_laptop_05_missing_parts_varyasyon2", "gaming_laptop_05_missing_parts_varyasyon3", "gaming_laptop_05_missing_parts_varyasyon4", "gaming_laptop_06_repaired_duct_tape_varyasyon1", "gaming_laptop_06_repaired_duct_tape_varyasyon2", "gaming_laptop_06_repaired_duct_tape_varyasyon3", "gaming_laptop_06_repaired_duct_tape_varyasyon4", "gaming_laptop_08_burned_varyasyon1", "gaming_laptop_08_burned_varyasyon2", "gaming_laptop_08_burned_varyasyon3", "gaming_laptop_08_burned_varyasyon4", "gaming_laptop_09_custom_painted_varyasyon1", "gaming_laptop_09_custom_painted_varyasyon2", "gaming_laptop_09_custom_painted_varyasyon3", "gaming_laptop_09_custom_painted_varyasyon4", "gaming_laptop_10_scrap_varyasyon1", "gaming_laptop_10_scrap_varyasyon2", "gaming_laptop_10_scrap_varyasyon3", "gaming_laptop_10_scrap_varyasyon4"),
            damaged = listOf("gaming_laptop_04_heavily_damaged_varyasyon1", "gaming_laptop_04_heavily_damaged_varyasyon2", "gaming_laptop_04_heavily_damaged_varyasyon3", "gaming_laptop_07_dirty_rusty_varyasyon1", "gaming_laptop_07_dirty_rusty_varyasyon2", "gaming_laptop_07_dirty_rusty_varyasyon3", "gaming_laptop_07_dirty_rusty_varyasyon4")
        ),
        "office_laptop" to ImagePool(
            clean = listOf("office_laptop_01_flawless_varyasyon1", "office_laptop_01_flawless_varyasyon2", "office_laptop_01_flawless_varyasyon3", "office_laptop_01_flawless_varyasyon4", "office_laptop_02_lightly_used_varyasyon1", "office_laptop_02_lightly_used_varyasyon2", "office_laptop_02_lightly_used_varyasyon3", "office_laptop_02_lightly_used_varyasyon4", "office_laptop_05_missing_parts_varyasyon1", "office_laptop_05_missing_parts_varyasyon3", "office_laptop_06_repaired_duct_tape_varyasyon1", "office_laptop_06_repaired_duct_tape_varyasyon2", "office_laptop_06_repaired_duct_tape_varyasyon3", "office_laptop_06_repaired_duct_tape_varyasyon4", "office_laptop_08_burned_varyasyon1", "office_laptop_08_burned_varyasyon2", "office_laptop_08_burned_varyasyon3", "office_laptop_08_burned_varyasyon4", "office_laptop_09_custom_painted_varyasyon1", "office_laptop_09_custom_painted_varyasyon2", "office_laptop_09_custom_painted_varyasyon3", "office_laptop_09_custom_painted_varyasyon4"),
            damaged = listOf("office_laptop_07_dirty_rusty_varyasyon1", "office_laptop_07_dirty_rusty_varyasyon2", "office_laptop_07_dirty_rusty_varyasyon3", "office_laptop_07_dirty_rusty_varyasyon4")
        ),
        "keypad_phone" to ImagePool(
            clean = listOf("keypad_phone_01_flawless_varyasyon1", "keypad_phone_01_flawless_varyasyon2", "keypad_phone_01_flawless_varyasyon3", "keypad_phone_01_flawless_varyasyon4", "keypad_phone_02_lightly_used_varyasyon1", "keypad_phone_02_lightly_used_varyasyon2", "keypad_phone_02_lightly_used_varyasyon3", "keypad_phone_02_lightly_used_varyasyon4", "keypad_phone_03_worn_out_varyasyon1", "keypad_phone_03_worn_out_varyasyon2", "keypad_phone_03_worn_out_varyasyon3", "keypad_phone_03_worn_out_varyasyon4", "keypad_phone_05_missing_parts_varyasyon1", "keypad_phone_05_missing_parts_varyasyon2", "keypad_phone_05_missing_parts_varyasyon3", "keypad_phone_05_missing_parts_varyasyon4", "keypad_phone_06_repaired_duct_tape_varyasyon1", "keypad_phone_06_repaired_duct_tape_varyasyon2", "keypad_phone_06_repaired_duct_tape_varyasyon3", "keypad_phone_06_repaired_duct_tape_varyasyon4", "keypad_phone_08_burned_varyasyon1", "keypad_phone_08_burned_varyasyon2", "keypad_phone_08_burned_varyasyon3", "keypad_phone_08_burned_varyasyon4", "keypad_phone_09_custom_painted_varyasyon1", "keypad_phone_09_custom_painted_varyasyon2", "keypad_phone_09_custom_painted_varyasyon3", "keypad_phone_09_custom_painted_varyasyon4"),
            damaged = listOf("keypad_phone_04_heavily_damaged_varyasyon1", "keypad_phone_04_heavily_damaged_varyasyon2", "keypad_phone_04_heavily_damaged_varyasyon3", "keypad_phone_04_heavily_damaged_varyasyon4", "keypad_phone_07_dirty_rusty_varyasyon1", "keypad_phone_07_dirty_rusty_varyasyon2", "keypad_phone_07_dirty_rusty_varyasyon3", "keypad_phone_07_dirty_rusty_varyasyon4")
        ),
        "mini_projector" to ImagePool(
            clean = listOf("mini_projector_01_flawless_varyasyon1", "mini_projector_01_flawless_varyasyon2", "mini_projector_01_flawless_varyasyon3", "mini_projector_01_flawless_varyasyon4", "mini_projector_02_lightly_used_varyasyon1", "mini_projector_02_lightly_used_varyasyon2", "mini_projector_02_lightly_used_varyasyon3", "mini_projector_02_lightly_used_varyasyon4", "mini_projector_03_worn_out_varyasyon1", "mini_projector_03_worn_out_varyasyon2", "mini_projector_03_worn_out_varyasyon3", "mini_projector_03_worn_out_varyasyon4", "mini_projector_05_missing_parts_varyasyon1", "mini_projector_05_missing_parts_varyasyon2", "mini_projector_05_missing_parts_varyasyon3", "mini_projector_05_missing_parts_varyasyon4", "mini_projector_06_repaired_duct_tape_varyasyon1", "mini_projector_06_repaired_duct_tape_varyasyon3", "mini_projector_06_repaired_duct_tape_varyasyon4", "mini_projector_08_burned_varyasyon1", "mini_projector_08_burned_varyasyon2", "mini_projector_08_burned_varyasyon3", "mini_projector_08_burned_varyasyon4", "mini_projector_09_custom_painted_varyasyon1", "mini_projector_09_custom_painted_varyasyon2", "mini_projector_09_custom_painted_varyasyon3", "mini_projector_09_custom_painted_varyasyon4"),
            damaged = listOf("mini_projector_04_heavily_damaged_varyasyon1", "mini_projector_04_heavily_damaged_varyasyon2", "mini_projector_04_heavily_damaged_varyasyon3", "mini_projector_04_heavily_damaged_varyasyon4", "mini_projector_07_dirty_rusty_varyasyon1", "mini_projector_07_dirty_rusty_varyasyon2", "mini_projector_07_dirty_rusty_varyasyon3", "mini_projector_07_dirty_rusty_varyasyon4")
        ),
        "pc_monitor" to ImagePool(
            clean = listOf("pc_monitor_01_flawless_varyasyon1", "pc_monitor_01_flawless_varyasyon2", "pc_monitor_01_flawless_varyasyon3", "pc_monitor_01_flawless_varyasyon4", "pc_monitor_02_lightly_used_varyasyon1", "pc_monitor_02_lightly_used_varyasyon2", "pc_monitor_02_lightly_used_varyasyon3", "pc_monitor_02_lightly_used_varyasyon4", "pc_monitor_03_worn_out_varyasyon1", "pc_monitor_03_worn_out_varyasyon2", "pc_monitor_03_worn_out_varyasyon3", "pc_monitor_03_worn_out_varyasyon4", "pc_monitor_05_missing_parts_varyasyon2", "pc_monitor_05_missing_parts_varyasyon3", "pc_monitor_06_repaired_duct_tape_varyasyon1", "pc_monitor_06_repaired_duct_tape_varyasyon4", "pc_monitor_09_custom_painted_varyasyon1", "pc_monitor_09_custom_painted_varyasyon2", "pc_monitor_09_custom_painted_varyasyon3", "pc_monitor_09_custom_painted_varyasyon4", "pc_monitor_10_scrap_varyasyon4"),
            damaged = listOf("pc_monitor_04_heavily_damaged_varyasyon1", "pc_monitor_04_heavily_damaged_varyasyon2", "pc_monitor_04_heavily_damaged_varyasyon3", "pc_monitor_04_heavily_damaged_varyasyon4", "pc_monitor_07_dirty_rusty_varyasyon1", "pc_monitor_07_dirty_rusty_varyasyon2", "pc_monitor_07_dirty_rusty_varyasyon3", "pc_monitor_07_dirty_rusty_varyasyon4")
        ),
        "smartwatch_new" to ImagePool(
            clean = listOf("smartwatch_01_flawless_varyasyon1", "smartwatch_01_flawless_varyasyon2", "smartwatch_01_flawless_varyasyon3", "smartwatch_01_flawless_varyasyon4", "smartwatch_02_lightly_used_varyasyon1", "smartwatch_02_lightly_used_varyasyon2", "smartwatch_02_lightly_used_varyasyon3", "smartwatch_02_lightly_used_varyasyon4", "smartwatch_03_worn_out_varyasyon1", "smartwatch_03_worn_out_varyasyon2", "smartwatch_03_worn_out_varyasyon3", "smartwatch_03_worn_out_varyasyon4", "smartwatch_06_repaired_duct_tape_varyasyon1", "smartwatch_06_repaired_duct_tape_varyasyon2", "smartwatch_06_repaired_duct_tape_varyasyon3", "smartwatch_06_repaired_duct_tape_varyasyon4", "smartwatch_08_burned_varyasyon1", "smartwatch_08_burned_varyasyon2", "smartwatch_08_burned_varyasyon3", "smartwatch_08_burned_varyasyon4", "smartwatch_09_custom_painted_varyasyon1", "smartwatch_09_custom_painted_varyasyon2", "smartwatch_09_custom_painted_varyasyon3", "smartwatch_09_custom_painted_varyasyon4", "smartwatch_10_scrap_varyasyon4"),
            damaged = listOf("smartwatch_04_heavily_damaged_varyasyon1", "smartwatch_04_heavily_damaged_varyasyon2", "smartwatch_04_heavily_damaged_varyasyon3", "smartwatch_04_heavily_damaged_varyasyon4", "smartwatch_07_dirty_rusty_varyasyon1", "smartwatch_07_dirty_rusty_varyasyon2", "smartwatch_07_dirty_rusty_varyasyon3", "smartwatch_07_dirty_rusty_varyasyon4")
        ),
        "tablet_new" to ImagePool(
            clean = listOf("tablet_01_flawless_varyasyon1", "tablet_01_flawless_varyasyon2", "tablet_01_flawless_varyasyon3", "tablet_01_flawless_varyasyon4", "tablet_02_lightly_used_varyasyon1", "tablet_02_lightly_used_varyasyon2", "tablet_02_lightly_used_varyasyon3", "tablet_02_lightly_used_varyasyon4", "tablet_03_worn_out_varyasyon1", "tablet_03_worn_out_varyasyon2", "tablet_03_worn_out_varyasyon3", "tablet_03_worn_out_varyasyon4", "tablet_05_missing_parts_varyasyon3", "tablet_06_repaired_duct_tape_varyasyon2", "tablet_06_repaired_duct_tape_varyasyon3", "tablet_06_repaired_duct_tape_varyasyon4", "tablet_08_burned_varyasyon1", "tablet_08_burned_varyasyon2", "tablet_08_burned_varyasyon3", "tablet_09_custom_painted_varyasyon1", "tablet_09_custom_painted_varyasyon2", "tablet_09_custom_painted_varyasyon3", "tablet_09_custom_painted_varyasyon4"),
            damaged = listOf("tablet_04_heavily_damaged_varyasyon3", "tablet_07_dirty_rusty_varyasyon2", "tablet_07_dirty_rusty_varyasyon3", "tablet_07_dirty_rusty_varyasyon4")
        ),
        "wireless_earbuds_new" to ImagePool(
            clean = listOf("wireless_earbuds_01_flawless_varyasyon1", "wireless_earbuds_01_flawless_varyasyon2", "wireless_earbuds_01_flawless_varyasyon3", "wireless_earbuds_01_flawless_varyasyon4", "wireless_earbuds_02_lightly_used_varyasyon2", "wireless_earbuds_02_lightly_used_varyasyon3", "wireless_earbuds_02_lightly_used_varyasyon4", "wireless_earbuds_03_worn_out_varyasyon2", "wireless_earbuds_03_worn_out_varyasyon4", "wireless_earbuds_05_missing_parts_varyasyon2", "wireless_earbuds_05_missing_parts_varyasyon4", "wireless_earbuds_06_repaired_duct_tape_varyasyon1", "wireless_earbuds_06_repaired_duct_tape_varyasyon2", "wireless_earbuds_06_repaired_duct_tape_varyasyon3", "wireless_earbuds_08_burned_varyasyon1", "wireless_earbuds_08_burned_varyasyon2", "wireless_earbuds_09_custom_painted_varyasyon2", "wireless_earbuds_09_custom_painted_varyasyon3", "wireless_earbuds_09_custom_painted_varyasyon4"),
            damaged = listOf("wireless_earbuds_01_flawless_varyasyon1", "wireless_earbuds_01_flawless_varyasyon2", "wireless_earbuds_01_flawless_varyasyon3", "wireless_earbuds_01_flawless_varyasyon4", "wireless_earbuds_02_lightly_used_varyasyon2", "wireless_earbuds_02_lightly_used_varyasyon3", "wireless_earbuds_02_lightly_used_varyasyon4", "wireless_earbuds_03_worn_out_varyasyon2", "wireless_earbuds_03_worn_out_varyasyon4", "wireless_earbuds_05_missing_parts_varyasyon2", "wireless_earbuds_05_missing_parts_varyasyon4", "wireless_earbuds_06_repaired_duct_tape_varyasyon1", "wireless_earbuds_06_repaired_duct_tape_varyasyon2", "wireless_earbuds_06_repaired_duct_tape_varyasyon3", "wireless_earbuds_08_burned_varyasyon1", "wireless_earbuds_08_burned_varyasyon2", "wireless_earbuds_09_custom_painted_varyasyon2", "wireless_earbuds_09_custom_painted_varyasyon3", "wireless_earbuds_09_custom_painted_varyasyon4")
        ),
        "dslr_camera" to ImagePool(
            clean = listOf("dslr_camera_01_flawless_varyasyon1", "dslr_camera_01_flawless_varyasyon2", "dslr_camera_01_flawless_varyasyon3", "dslr_camera_01_flawless_varyasyon4", "dslr_camera_02_lightly_used_varyasyon1", "dslr_camera_02_lightly_used_varyasyon2", "dslr_camera_02_lightly_used_varyasyon3", "dslr_camera_02_lightly_used_varyasyon4"),
            damaged = listOf("dslr_camera_01_flawless_varyasyon1", "dslr_camera_01_flawless_varyasyon2", "dslr_camera_01_flawless_varyasyon3", "dslr_camera_01_flawless_varyasyon4", "dslr_camera_02_lightly_used_varyasyon1", "dslr_camera_02_lightly_used_varyasyon2", "dslr_camera_02_lightly_used_varyasyon3", "dslr_camera_02_lightly_used_varyasyon4")
        ),
        "baseball_cap" to ImagePool(
            clean = listOf("blankbaseballcap", "blankwhitecap", "cleanbeigecap", "cleanwhitecap", "dustybeigecap", "fadedbluecap", "heavilymuddycap", "muddybaseballcap", "muddybrowncap", "pristinebeigecap", "pristinewhitecap", "ruinedmuddycap", "sunbleachedcap", "wornbeigecap", "worngreybaseballcap"),
            damaged = listOf("blankbaseballcap", "blankwhitecap", "cleanbeigecap", "cleanwhitecap", "dustybeigecap", "fadedbluecap", "heavilymuddycap", "muddybaseballcap", "muddybrowncap", "pristinebeigecap", "pristinewhitecap", "ruinedmuddycap", "sunbleachedcap", "wornbeigecap", "worngreybaseballcap")
        ),
        "camping_tent" to ImagePool(
            clean = listOf("browncampingtent", "orangecampingtent", "ruinedmuddytent", "yellowcampingtent"),
            damaged = listOf("browncampingtent", "orangecampingtent", "ruinedmuddytent", "yellowcampingtent")
        ),
        "toolbox" to ImagePool(
            clean = listOf("bluetoolboxwithyellowwrench", "greytoolboxwithwrench", "orangetoolboxwithwrench", "smashedbrowntoolbox"),
            damaged = listOf("bluetoolboxwithyellowwrench", "greytoolboxwithwrench", "orangetoolboxwithwrench", "smashedbrowntoolbox")
        ),
        "fishing_rod" to ImagePool(
            clean = listOf("classicfishingrod", "fishingrodwithbox", "standardfishingrod"),
            damaged = listOf("brokenfishingrod")
        ),
        "folding_table" to ImagePool(
            clean = listOf("basicfoldingtable", "cleanfoldingtable", "scratchedfoldingtable"),
            damaged = listOf("brokenfoldingtable")
        ),
        "aviator_sunglasses" to ImagePool(
            clean = listOf("classicaviatorsunglasses", "greenaviatorsunglasses"),
            damaged = listOf("brokenaviratorsunglasses")
        ),
        "sneakers" to ImagePool(
            clean = listOf("chunkywhitesneakers", "pristinewhitesneakers", "ruinedmuddysneakers", "wornwhitesneakers"),
            damaged = listOf("slightlydirtysneakers")
        ),
        "car_radio" to ImagePool(
            clean = listOf("blackscreencarradio", "greenscreencarradio", "hollowcarradio", "smashedcarradio"),
            damaged = listOf("blackscreencarradio", "greenscreencarradio", "hollowcarradio", "smashedcarradio")
        ),
        "mystery_box" to ImagePool(
            clean = listOf("cardboardmysterybox", "mysterybox", "squaremysterybox"),
            damaged = listOf("cardboardmysterybox", "mysterybox", "squaremysterybox")
        ),
        "ufo_lamp" to ImagePool(
            clean = listOf("neonufolamp", "retroufolamp", "ufodesklampp2"),
            damaged = listOf("damagedufolamp")
        ),
        "treasure_map" to ImagePool(
            clean = listOf("pristinetreasuremap", "vintagetreasuremap"),
            damaged = listOf("pristinetreasuremap", "vintagetreasuremap")
        ),
        "trex_skull" to ImagePool(
            clean = listOf("floatingtrexskull", "rexskullreplica", "trexskulllonstand"),
            damaged = listOf("floatingtrexskull", "rexskullreplica", "trexskulllonstand")
        ),
        "polo_shirt" to ImagePool(
            clean = listOf("foldedpoloshirts", "foldedstripedjersey", "foldedvnecksweater"),
            damaged = listOf("foldedpoloshirts", "foldedstripedjersey", "foldedvnecksweater")
        ),
        "leather_jacket_folded" to ImagePool(
            clean = listOf("foldedleatherjackets"),
            damaged = listOf("foldedleatherjackets")
        ),
        "silver_watch" to ImagePool(
            clean = listOf("silvermetalwatch", "smashedmetalwatch"),
            damaged = listOf("silvermetalwatch", "smashedmetalwatch")
        ),
        "piknik_tupu" to ImagePool(
            clean = listOf("piknik_tupu"),
            damaged = listOf("piknik_tupu")
        ),
        "iphone" to ImagePool(
            clean = listOf("iphone"),
            damaged = listOf("iphone")
        )
    )

    // ─── Ürün Şablonları ──────────────────────────────────────────────────────

    data class ProductTemplate(
        val name: String,
        val category: String,
        val imageKey: String,
        val baseMinValue: Int,
        val baseMaxValue: Int
    )

        val NORMAL_PRODUCTS = listOf(ProductTemplate("Klasik Plastik Ördek", "Toys", "rubber_duck", 56, 120),
        ProductTemplate("Yumurtadan Çıkan Ördek", "Toys", "rubber_duck_egg", 84, 180),
        ProductTemplate("Akıllı Telefon (Temiz)", "Electronics", "smartphone_clean", 8400, 18000),
        ProductTemplate("Akıllı Telefon (Ekranı Kırık)", "Electronics", "smartphone_cracked", 3150, 6750),
        ProductTemplate("Akıllı Telefon (Çizik Kasa)", "Electronics", "smartphone_scratched", 5950, 12750),
        ProductTemplate("Akıllı Telefon (Bakımsız)", "Electronics", "smartphone_dirty", 4900, 10500),
        ProductTemplate("Akıllı Telefon (Arkası Yanık)", "Electronics", "smartphone_melted_back", 1750, 3750),
        ProductTemplate("10 inç Android Tablet", "Electronics", "tablet_galaxy", 3500, 7500),
        ProductTemplate("Tablet (Kılcal Çizikli)", "Electronics", "tablet_scratched", 2240, 4800),
        ProductTemplate("Mutfak Blender", "Home_appliances", "blender", 665, 1425),
        ProductTemplate("Filtre Kahve Makinesi", "Home_appliances", "coffee_maker", 1540, 3300),
        ProductTemplate("Mini Buzdolabı", "Home_appliances", "mini_fridge", 2660, 5700),
        ProductTemplate("Oyuncu Kulaklığı (Bantlı)", "Accessories", "headphones_duct_tape", 315, 675),
        ProductTemplate("Deri Kordonlu Klasik Saat", "Accessories", "classic_watch", 1050, 2250),
        ProductTemplate("Şehir Bisikleti", "Sports", "classic_bicycle", 3360, 7200),
        ProductTemplate("Kanvas Sırt Çantası", "Accessories", "canvas_backpack", 454, 975),
        ProductTemplate("İkinci El Forma", "Sports", "dirty_jersey", 210, 450),
        ProductTemplate("Ortadan Kırık Gamepad", "Electronics", "gamepad_broken", 105, 225),
        ProductTemplate("Oyuncu Monitörü (Paneli Kırık)", "Electronics", "monitor_cracked", 840, 1800),
        ProductTemplate("VR Gözlük (Temiz)", "Electronics", "vr_headset_clean", 4200, 9000),
        ProductTemplate("VR Gözlük (Çizik)", "Electronics", "vr_headset_scratched", 2800, 6000),
        ProductTemplate("Mekanik Klavye (Temiz)", "Electronics", "mech_keyboard_clean", 1050, 2250),
        ProductTemplate("Mekanik Klavye (Tuşsuz)", "Electronics", "mech_keyboard_missing_key", 420, 900),
        ProductTemplate("Akustik Gitar (Temiz)", "Hobby", "acoustic_guitar_clean", 1400, 3000),
        ProductTemplate("Akustik Gitar (Teli Kopuk)", "Hobby", "acoustic_guitar_broken_string", 630, 1350),
        ProductTemplate("Deri Ceket (Temiz)", "Clothing", "leather_jacket_clean", 2450, 5250),
        ProductTemplate("Deri Ceket (Yırtık)", "Clothing", "leather_jacket_torn", 560, 1200),
        ProductTemplate("Şehir Bisikleti", "Sports", "city_bike", 2450, 5250),
        ProductTemplate("Dağ Bisikleti", "Sports", "mountain_bike", 3639, 7800),
        ProductTemplate("Retro Oyun Konsolu", "Electronics", "retro_console", 1959, 4200),
        ProductTemplate("VR Gözlük (Çeşitli Model)", "Electronics", "vr_headset_extra", 3150, 6750),
        ProductTemplate("RC Araba", "Toys", "rc_car", 1260, 2700),
        ProductTemplate("RC Kamyon (Çamurlu)", "Toys", "rc_truck_muddy", 630, 1350),
        ProductTemplate("Elektrikli Scooter", "Sports", "electric_scooter", 4550, 9750),
        ProductTemplate("Yapboz Bulmaca", "Toys", "jigsaw_puzzle", 244, 525),
        ProductTemplate("Renkli Lego Blok", "Toys", "toy_blocks", 175, 375),
        ProductTemplate("Ahşap Satranç Takımı", "Toys", "chess_set", 840, 1800),
        ProductTemplate("Araba Lastiği", "Spare_parts", "car_tire", 560, 1200),
        ProductTemplate("Lüks Kol Saati", "Accessories", "luxury_watch", 10500, 22500),
        ProductTemplate("Ezilmiş Kol Saati", "Accessories", "smashed_watch", 1400, 3000),
        ProductTemplate("Oyuncu Laptopu (Ağır Seri)", "Electronics", "gaming_laptop_v2", 24500, 52500),
        ProductTemplate("Laptop (Hasarlı)", "Electronics", "laptop_damaged", 5600, 12000),
        ProductTemplate("Retro Cep Telefonu", "Electronics", "retro_mobile", 1750, 3750),
        ProductTemplate("Akıllı Telefon (Çizik)", "Electronics", "smartphone_scratched_v2", 5250, 11250),
        ProductTemplate("Akıllı Telefon (Parçalı)", "Electronics", "smartphone_missing", 2100, 4500),
        ProductTemplate("Akıllı Telefon (Hurda)", "Electronics", "smartphone_scrap", 560, 1200),
        ProductTemplate("Akıllı Telefon (Özel Boyalı)", "Electronics", "smartphone_custom", 6650, 14250),
        ProductTemplate("Oyuncu Kulaklığı", "Electronics", "gaming_headset_new", 2450, 5250),
        ProductTemplate("Bluetooth Hoparlör", "Electronics", "bluetooth_speaker_new", 1959, 4200),
        ProductTemplate("Oyun Konsolu (Modern)", "Electronics", "game_console_new", 5600, 12000),
        ProductTemplate("Oyuncu Laptopu (Varyantlı)", "Electronics", "gaming_laptop_new", 21000, 45000),
        ProductTemplate("Ofis Laptopu", "Electronics", "office_laptop", 8400, 18000),
        ProductTemplate("Tuş Takımlı Telefon", "Electronics", "keypad_phone", 1050, 2250),
        ProductTemplate("Mini Projektör", "Electronics", "mini_projector", 3150, 6750),
        ProductTemplate("PC Monitör", "Electronics", "pc_monitor", 3849, 8250),
        ProductTemplate("Akıllı Saat (Varyantlı)", "Accessories", "smartwatch_new", 4200, 9000),
        ProductTemplate("Tablet (Varyantlı)", "Electronics", "tablet_new", 4900, 10500),
        ProductTemplate("Kablosuz Kulaklık", "Electronics", "wireless_earbuds_new", 1750, 3750),
        ProductTemplate("DSLR Fotoğraf Makinesi", "Electronics", "dslr_camera", 10500, 22500),
        ProductTemplate("Beyzbol Şapkası", "Clothing", "baseball_cap", 175, 375),
        ProductTemplate("Kamp Çadırı", "Sports", "camping_tent", 840, 1800),
        ProductTemplate("Alet Kutusu", "Spare_parts", "toolbox", 420, 900),
        ProductTemplate("Balık Oltası", "Sports", "fishing_rod", 280, 600),
        ProductTemplate("Katlanır Masa", "Home_appliances", "folding_table", 315, 675),
        ProductTemplate("Aviator Güneş Gözlüğü", "Accessories", "aviator_sunglasses", 210, 450),
        ProductTemplate("Spor Ayakkabı", "Clothing", "sneakers", 560, 1200),
        ProductTemplate("Araba Radyosu", "Electronics", "car_radio", 525, 1125),
        ProductTemplate("Gizemli Kutu", "Toys", "mystery_box", 350, 750),
        ProductTemplate("UFO Masa Lambası", "Home_appliances", "ufo_lamp", 630, 1350),
        ProductTemplate("Hazine Haritası", "Toys", "treasure_map", 1050, 2250),
        ProductTemplate("T-Rex Kafatası Replikası", "Toys", "trex_skull", 2100, 4500),
        ProductTemplate("Polo Tişört", "Clothing", "polo_shirt", 140, 300),
        ProductTemplate("Deri Ceket (Katlanmış)", "Clothing", "leather_jacket_folded", 1750, 3750),
        ProductTemplate("Gümüş Metal Saat", "Accessories", "silver_watch", 1400, 3000),
        ProductTemplate("Bez Çanta", "Clothing", "bez_canta", 35, 75),
        ProductTemplate("Mini Fırın", "Home_appliances", "mini_firin", 1050, 2250),
        ProductTemplate("Kamera", "Electronics", "kamera_resim", 5950, 12750),
        ProductTemplate("Oyun Kulaklığı", "Electronics", "oyun_kulakligi", 1750, 3750),
        ProductTemplate("Paslı Piknik Tüpü", "Home_appliances", "piknik_tupu", 50, 200),
        ProductTemplate("Elma Telefon 15", "Electronics", "iphone", 45000, 75000),
        ProductTemplate("Antika Köstekli Saat", "Accessories", "classic_watch", 2000, 5000),
        ProductTemplate("Gaming Laptop", "Electronics", "gaming_laptop_new", 15000, 30000),
        ProductTemplate("Oyun Konsolu", "Electronics", "game_console_new", 4000, 8000),
        ProductTemplate("Tarihi Roma Sikkesi", "Hobby", "silver_watch", 5000, 15000),
        ProductTemplate("Bereket Muskası", "Accessories", "bez_canta", 100, 500),
        ProductTemplate("Bozuk Gramofon", "Home_appliances", "car_radio", 1000, 3000),
        ProductTemplate("Oltu Taşı Tesbih", "Accessories", "silver_watch", 500, 1500),
        ProductTemplate("Gözyaşı Pırlanta Yüzük", "Accessories", "silver_watch", 10000, 50000),
        ProductTemplate("Gümüş İşlemeli Pusula", "Accessories", "silver_watch", 800, 2500),
        ProductTemplate("10 Kilo Kaçak Çay", "Home_appliances", "bez_canta", 500, 1000),
        ProductTemplate("Ahşap Tofaş Direksiyonu", "Spare_parts", "car_tire", 200, 800),
        ProductTemplate("Gözyaşı Kolyesi", "Accessories", "silver_watch", 2000, 8000),
        ProductTemplate("Gizemli Çelik Kasa", "Toys", "mystery_box", 5000, 20000),
        ProductTemplate("Bozuk Duvar Saati", "Home_appliances", "smashed_watch", 100, 300),
        ProductTemplate("Kafeste Baykuş", "Hobby", "mystery_box", 500, 2000),
        ProductTemplate("Sağlam El Radyosu", "Electronics", "car_radio", 300, 800),
        ProductTemplate("Çerçeveli Yapboz", "Toys", "jigsaw_puzzle", 100, 400),
        ProductTemplate("Çelik Termos", "Sports", "ufo_lamp", 200, 600),
        ProductTemplate("Ahşap Boy Aynası", "Home_appliances", "folding_table", 400, 1000),
        ProductTemplate("Kamp Feneri", "Sports", "ufo_lamp", 150, 500),
        ProductTemplate("Folyolu Altın Sikke", "Hobby", "silver_watch", 50, 200),
        ProductTemplate("Eski Parşömen Harita", "Toys", "treasure_map", 1000, 4000)
    )

    val ABSURD_PRODUCTS = listOf(
        // Absürt İlanlar (Nadir)
        ProductTemplate("Boğaz Köprüsü (Hissedar)", "Emlak", "bogaz_koprusu", 5000000, 15000000),
        ProductTemplate("NASA Bilgisayarı", "Elektronik", "nasa_bilgisayari", 1000000, 5000000),
        ProductTemplate("F-16 (Anahtarı Kayıp)", "Araç", "f_16", 20000000, 50000000),
        ProductTemplate("Satılık Kaynana", "Diğer", "satilik_kaynana", 10, 100),
        ProductTemplate("Köy Kahvesi", "Emlak", "koy_kahvesi", 100000, 500000)
    )

    val PRODUCTS: List<ProductTemplate> get() = NORMAL_PRODUCTS + ABSURD_PRODUCTS

    // ─── Kondisyon Havuzu (Ağırlıklı) ─────────────────────────────────────────

    private data class Condition(val label: String, val valueMultiplier: Double)

    private val CONDITIONS_GENERAL = buildList {
        repeat(20) { add(Condition("Kusursuz Temiz",         1.00)) }
        repeat(30) { add(Condition("Hafif Çizik",            0.82)) }
        repeat(25) { add(Condition("Orta Hasar",             0.65)) }
        repeat(15) { add(Condition("Kırık / Arızalı",        0.40)) }
        repeat(10) { add(Condition("Bantlı / Tamir Gerekli", 0.25)) }
    }

    private val CONDITIONS_OTOMOTIV = buildList {
        repeat(20) { add(Condition("Hatasız Boyasız",        1.00)) }
        repeat(30) { add(Condition("Lokal Boyalı",           0.85)) }
        repeat(25) { add(Condition("Çizik / Kaporta Hasarlı",0.70)) }
        repeat(15) { add(Condition("Motor Arızalı",          0.50)) }
        repeat(10) { add(Condition("Ağır Hasarlı / Pert",    0.30)) }
    }

    private val CONDITIONS_EMLAK = buildList {
        repeat(20) { add(Condition("Sıfır / Ultra Lüks",     1.00)) }
        repeat(30) { add(Condition("Masrafsız Temiz",        0.90)) }
        repeat(25) { add(Condition("Ufak Tadilatlık",        0.75)) }
        repeat(15) { add(Condition("Masraflı / Rutubetli",   0.55)) }
        repeat(10) { add(Condition("Yıkık / Harabe",         0.35)) }
    }

    private fun getConditionList(category: String): List<Condition> {
        return when (category.lowercase()) {
            "otomotiv", "vehicles", "araba" -> CONDITIONS_OTOMOTIV
            "emlak", "realestate", "ev" -> CONDITIONS_EMLAK
            else -> CONDITIONS_GENERAL
        }
    }

    private fun getScammerDisplayedCondition(category: String): String {
        return getConditionList(category).first().label // En iyi kondisyonu gösterir
    }

    // Dolandırıcıdan alındıktan sonra ortaya çıkacak gerçek kondisyon havuzu
    private fun getScammerHiddenConditionList(category: String): List<Condition> {
        val conditions = getConditionList(category)
        return buildList {
            repeat(3)  { add(conditions[2]) } // Orta halli kötü
            repeat(5)  { add(conditions[3]) } // Kötü
            repeat(2)  { add(conditions[4]) } // En kötü
        }
    }

    // Tüm kondisyonların birleşimi (Multiplier aramak için)
    private val ALL_CONDITIONS = (CONDITIONS_GENERAL + CONDITIONS_OTOMOTIV + CONDITIONS_EMLAK).distinctBy { it.label }

    // ─── Satıcı Havuzu ────────────────────────────────────────────────────────

    private val SELLERS = listOf(
        "Sabırsız Murat", "Pazarcı Hüseyin", "Eski Çarşı Ali", "Hesaplı Fatma",
        "Sürekli İndirim Yaşar", "Acele Satan Kemal", "Güvenilir Mehmet",
        "Kapı Kapı Dolaşan Necip", "Fırsatçı Selin", "Temiz Mal Derya",
        "İkinci El Emre", "Stok Eritme Ahmet", "Son Fiyat Leyla",
        "Hızlı Kazan Pınar", "Uygun Fiyat Osman", "Seri Satan Zeynep",
        "Taze Mal Tarık", "Şanslı Gün Ayşe", "Net Fiyat Barış",
        "Temiz Ev Nazan", "Anlık Fiyat Serkan", "Güler Yüzlü Gülden",
        "Eşyacı Tuncay", "Bol Stok Ferhat", "Dürüst Satıcı Sedef",
        "Komşu Pazarı İsmail", "Anlık İndirim Handan", "Kaliteli Mal Cemal",
        "Eski Dost Ufuk", "Fırsat Kaçmaz Dilek", "Takas Yapan Tolga",
        "Elde Kalan Sibel", "Stok Fazlası Bülent", "İkinci Şans Burak",
        "Son Fiyat Esen", "Hızlı İşlem Korhan", "Her Şey Satılık Özge"
    )

    // Dolandırıcı isimleri — masum görünümlü
    val SCAMMER_SELLERS = listOf(
        "Güvenilir Halit", "Dürüst Semih", "Temiz Adam Kürşat",
        "Açık Kalpli Nuri", "Şeffaf Satıcı Vedat", "Doğru Sözlü Cengiz",
        "Namuslu Hakan", "Helal Süt Emmiş Tarcan", "Nezaketli Faruk"
    )

    fun getRandomName(): String = SELLERS.random(Random.Default)

    // ─── Üretim Motoru ────────────────────────────────────────────────────────

    fun getConditionMultiplier(name: String): Double {
        return ALL_CONDITIONS.find { it.label == name }?.valueMultiplier
            ?: GameConstants.PERFECT_CONDITION_MULTIPLIER
    }

    fun generateItems(
        count: Int = GameConstants.MARKET_BATCH_SIZE,
        marketTrends: Map<String, Double> = emptyMap(),
        activeModifiers: Map<String, Int> = emptyMap()
    ): List<MarketItem> {
        if (activeModifiers.containsKey("NO_SALES")) return emptyList()
        return (1..count).map { generateOne(marketTrends) }
    }

    private fun generateOne(marketTrends: Map<String, Double>): MarketItem {
        val rng = Random.Default
        val isAbsurd = rng.nextDouble() < 0.10 // %10 şansla absürt ilan
        val product = if (isAbsurd) ABSURD_PRODUCTS.random(rng) else NORMAL_PRODUCTS.random(rng)
        val isScammer = rng.nextDouble() < GameConstants.SCAMMER_CHANCE

        return if (isScammer) {
            generateScammerItem(rng, product, marketTrends)
        } else {
            generateNormalItem(rng, product, marketTrends)
        }
    }

    fun generateNormalItem(
        rng: Random,
        product: ProductTemplate,
        marketTrends: Map<String, Double>
    ): MarketItem {
        // Eşya kondisyonu (Kategoriye göre)
        val conditionList = getConditionList(product.category)
        val condition = conditionList.random(rng)
        val baseValue = rng.nextInt(product.baseMinValue, product.baseMaxValue)
        val variance = (
            baseValue *
                GameConstants.MARKET_VALUE_VARIANCE_RATE *
                (rng.nextDouble() - 0.5)
            ).roundToInt()
        val trendMultiplier = marketTrends[product.category] ?: 1.0
        
        val extras = mutableListOf<String>()
        var extraMultiplier = 1.0
        
        if (rng.nextDouble() < 0.20) {
            extras.add("Faturalı & Garantili")
            extraMultiplier += 0.10
        }
        if (rng.nextDouble() < 0.15 && product.category == "Elektronik") {
            extras.add("Kutusu Açılmamış")
            extraMultiplier += 0.15
        } else if (rng.nextDouble() < 0.10 && product.category == "Elektronik") {
            extras.add("Şarj Aleti Eksik")
            extraMultiplier -= 0.05
        }

        val estimatedValue = maxOf(GameConstants.MARKET_MIN_ITEM_VALUE, ((baseValue + variance) * trendMultiplier * extraMultiplier).roundToInt())

        val salesRatio = GameConstants.MARKET_MIN_SALES_RATIO +
            rng.nextDouble() * GameConstants.MARKET_SALES_RATIO_RANGE
        val salesValue = (estimatedValue * salesRatio * condition.valueMultiplier)
            .roundToInt().coerceAtLeast(GameConstants.MARKET_MIN_SALES_VALUE)

        val pool = IMAGE_POOLS[product.imageKey]
        val imageName = pool?.pickFor(condition.label) ?: "smartphone_clean_1"

        // Normal açıklama üret
        val normalDescriptions = listOf(
            "İhtiyaçtan satılık, pazarlık payı vardır.",
            "Çok az kullanıldı, sıfır ayarında.",
            "Acil nakit ihtiyacından dolayı bu fiyata.",
            "Yeni modelini aldığım için satıyorum.",
            "Tertemiz ürün, gelip görebilirsiniz."
        )
        val description = if (extras.isNotEmpty()) {
            normalDescriptions.random(rng) + " " + extras.joinToString(", ") + " mevcut."
        } else {
            normalDescriptions.random(rng)
        }

        return MarketItem(
            condition = condition.label,
            sellerName = SELLERS.random(rng),
            itemName = product.name,
            salesValue = salesValue.toString(),
            estimatedValue = estimatedValue.toString(),
            imageName = imageName,
            category = product.category,
            extras = extras,
            description = description
        )
    }

    private fun generateScammerItem(
        rng: Random,
        product: ProductTemplate,
        marketTrends: Map<String, Double>
    ): MarketItem {
        // Dolandırıcı tipi seç
        val scamType = ScamType.entries.random(rng)

        // Dolandırıcılar eşyayı en iyi kondisyonda (Kusursuz/Sıfır/Hatasız) gibi gösterir ama arkasında en kötülerinden biri çıkar
        val hiddenCondition = getScammerHiddenConditionList(product.category).random(rng)
        
        // Sahte "temiz" kondisyon
        val displayedCondition = getScammerDisplayedCondition(product.category)

        // Gerçek değer: hiddenCondition multiplier ile
        val baseValue = rng.nextInt(product.baseMinValue, product.baseMaxValue)
        val trendMultiplier = marketTrends[product.category] ?: 1.0
        val trueEstimatedValue = maxOf(
            GameConstants.MARKET_MIN_ITEM_VALUE,
            (baseValue * trendMultiplier * hiddenCondition.valueMultiplier).roundToInt()
        )

        // Dolandırıcı satış fiyatı: Sahte kelepir → gerçek değerden fazla
        // Diğerleri → kusursuz değermiş gibi satıyor (kazık)
        val fakePerfectValue = maxOf(
            GameConstants.MARKET_MIN_ITEM_VALUE,
            (baseValue * trendMultiplier).roundToInt()
        )

        val salesValue = when (scamType) {
            ScamType.SAHTE_KELEPIR -> {
                // Piyasadan pahalı ama "ucuz" gibi gösterir
                (fakePerfectValue * (1.10 + rng.nextDouble() * 0.20)).roundToInt()
            }
            else -> {
                // Bozuk malı sağlam gibi gösterip tam fiyat istiyor
                val ratio = GameConstants.MARKET_MIN_SALES_RATIO +
                    rng.nextDouble() * GameConstants.MARKET_SALES_RATIO_RANGE * 0.5
                (fakePerfectValue * ratio).roundToInt()
            }
        }

        // Görsel: temiz görsel (dolandırıcı aldatıyor)
        val pool = IMAGE_POOLS[product.imageKey]
        val imageName = pool?.pickFor("Kusursuz") ?: "smartphone_clean_1"

        // Dolandırıcı isim havuzundan seç
        val sellerName = SCAMMER_SELLERS.random(rng)

        // Dolandırıcı açıklaması
        val description = when (scamType) {
            ScamType.KUTU_SATISI -> "Sıfır gibi, jelatinleri bile üstünde. Sadece kutusu satılıktır, cihaz fiyata dahil değildir. İade kabul etmiyorum."
            ScamType.BATARYA -> "Pil sağlığı %100! (Yeni değiştirdim yan sanayi ama olsun)"
            ScamType.KOZMETIK -> "Çizik dahi yok, tertemiz cihaz."
            ScamType.EKSIK_BILGI -> "Sadece ufak bir sorunu var, o da kullanıma engel değil."
            else -> "Acil satılık, ilk gelen alır! Kaçırmayın!"
        }

        return MarketItem(
            condition = displayedCondition,
            sellerName = sellerName,
            itemName = product.name,
            salesValue = salesValue.toString(),
            estimatedValue = fakePerfectValue.toString(), // Sahte tahmini değer
            imageName = imageName,
            category = product.category,
            isScammer = true,
            scamType = scamType.name,
            hiddenCondition = hiddenCondition.label,
            description = description
        )
    }

    // V6.0: Satıcı Profili için özel ilan üretici
    fun generateItemForSeller(rng: kotlin.random.Random, sellerName: String): MarketItem {
        val product = PRODUCTS.random(rng)
        val isScammer = SCAMMER_SELLERS.contains(sellerName)
        
        // Eğer satıcı dolandırıcıysa hep dolandırıcı ilanları üretsin, değilse normal ilan
        val item = if (isScammer) {
            generateScammerItem(rng, product, emptyMap()).copy(sellerName = sellerName)
        } else {
            generateNormalItem(rng, product, emptyMap()).copy(sellerName = sellerName)
        }
        
        // Fiyatlarda ufak varyasyonlar yap ki hepsi aynı olmasın
        val vary = rng.nextDouble(0.9, 1.1)
        val newSalesValue = (item.salesValue.toDouble() * vary).roundToInt().toString()
        
        return item.copy(salesValue = newSalesValue)
    }
}
