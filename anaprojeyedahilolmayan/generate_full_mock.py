import json

base_items = [
    {"name": "Klasik Plastik Ördek", "condition": "Temiz", "basePrice": 80, "variants": [f"rubber_duck_{i}" for i in range(1, 8)]},
    {"name": "Yumurtadan Çıkan Ördek", "condition": "Yarım Kırık", "basePrice": 120, "variants": [f"rubber_duck_egg_{i}" for i in range(1, 6)]},
    {"name": "Akıllı Telefon", "condition": "Temiz", "basePrice": 12000, "variants": [f"smartphone_clean_{i}" for i in range(1, 13)]},
    {"name": "Akıllı Telefon", "condition": "Kırık Ekran", "basePrice": 4500, "variants": [f"smartphone_cracked_{i}" for i in range(1, 11)]},
    {"name": "Akıllı Telefon", "condition": "Çizik Kasa", "basePrice": 8500, "variants": [f"smartphone_scratched_{i}" for i in range(1, 4)]},
    {"name": "Akıllı Telefon", "condition": "Bakımsız", "basePrice": 7000, "variants": [f"smartphone_dirty_{i}" for i in range(1, 5)]},
    {"name": "Akıllı Telefon", "condition": "Arkası Yanık", "basePrice": 2500, "variants": ["smartphone_melted_back"]},
    {"name": "10 inç Android Tablet", "condition": "Temiz", "basePrice": 5000, "variants": ["tablet_galaxy"]},
    {"name": "Tablet", "condition": "Kılcal Çizikli", "basePrice": 3200, "variants": [f"tablet_scratched_{i}" for i in range(1, 3)]},
    {"name": "Mutfak Blender", "condition": "Az Kullanılmış", "basePrice": 950, "variants": [f"blender_{i}" for i in range(1, 6)]},
    {"name": "Filtre Kahve Makinesi", "condition": "Kutusu Yok", "basePrice": 2200, "variants": [f"coffee_maker_{i}" for i in range(1, 5)]},
    {"name": "Mini Buzdolabı", "condition": "Temiz", "basePrice": 3800, "variants": [f"mini_fridge_{i}" for i in range(1, 4)]},
    {"name": "Oyuncu Kulaklığı", "condition": "Bantlı (Tamirli)", "basePrice": 450, "variants": [f"headphones_duct_tape_{i}" for i in range(1, 4)]},
    {"name": "Deri Kordonlu Klasik Saat", "condition": "Temiz", "basePrice": 1500, "variants": [f"classic_watch_{i}" for i in range(1, 4)]},
    {"name": "Şehir Bisikleti", "condition": "Bakımsız", "basePrice": 4800, "variants": ["classic_bicycle_1"]},
    {"name": "Kanvas Sırt Çantası", "condition": "Yıpranmış", "basePrice": 650, "variants": [f"canvas_backpack_{i}" for i in range(1, 3)]},
    {"name": "İkinci El Forma", "condition": "Yıkanması Lazım", "basePrice": 300, "variants": ["dirty_jersey_1", "dirty_jersey_2"]},
    {"name": "Oyun Kolu (Gamepad)", "condition": "Ortadan Kırık", "basePrice": 150, "variants": ["gamepad_broken_1", "gamepad_broken_2"]},
    {"name": "Oyuncu Monitörü", "condition": "Paneli Kırık", "basePrice": 1200, "variants": [f"monitor_cracked_{i}" for i in range(1, 4)]},
    {"name": "Elektrikli Scooter", "condition": "Paslı", "basePrice": 3000, "variants": ["electric_scooter_1"]},
    {"name": "UFO Oyuncak", "condition": "Işıklı", "basePrice": 450, "variants": ["ufo_toy_1", "ufo_toy_2"]},
    {"name": "Oyuncu Laptopu", "condition": "Temiz", "basePrice": 25000, "variants": ["laptop_clean_1", "laptop_clean_2"]},
    {"name": "VR Gözlük", "condition": "Temiz", "basePrice": 6000, "variants": ["vr_headset_clean_1"]},
    {"name": "VR Gözlük", "condition": "Çizik", "basePrice": 4000, "variants": ["vr_headset_scratched_1"]},
    {"name": "Mekanik Klavye", "condition": "Temiz", "basePrice": 1500, "variants": ["mech_keyboard_clean_1"]},
    {"name": "Mekanik Klavye", "condition": "Tuşsuz", "basePrice": 600, "variants": ["mech_keyboard_missing_key_1"]},
    {"name": "Akustik Gitar", "condition": "Temiz", "basePrice": 2000, "variants": ["acoustic_guitar_clean_1"]},
    {"name": "Akustik Gitar", "condition": "Teli Kopuk", "basePrice": 900, "variants": ["acoustic_guitar_broken_string_1"]},
    {"name": "Deri Ceket", "condition": "Temiz", "basePrice": 3500, "variants": ["leather_jacket_clean_1"]},
    {"name": "Deri Ceket", "condition": "Yırtık", "basePrice": 800, "variants": ["leather_jacket_torn_1"]}
]

sellers = ["Pazarlıkçı Ahmet Abi", "Sabırsız Murat", "Dolandırıcı Şahin", "Temiz Aile Babası Kemal", "Öğrenci Kardeşimiz"]

result = []
seller_idx = 0
for base in base_items:
    for var in base["variants"]:
        sales_val = int(base["basePrice"] * 0.8) # Rastgele ilan fiyati
        est_val = base["basePrice"] # Piyasa degeri
        result.append({
            "condition": base["condition"],
            "sellerName": sellers[seller_idx % len(sellers)],
            "itemName": base["name"],
            "salesValue": str(sales_val),
            "estimatedValue": str(est_val),
            "imageName": var.replace(".webp", "")
        })
        seller_idx += 1

with open(r"c:\Users\enes\AndroidStudioProjects\kelepiravi\app\src\main\assets\json\mock_market.json", "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f"Toplam {len(result)} oge mock_market.json dosyasina yazildi.")
