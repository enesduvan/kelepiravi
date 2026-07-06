import os
import json
import shutil

# Yollar
WEBP_DIR = "Duvance_Webp_Hazir"
DRAWABLE_DIR = r"..\app\src\main\res\drawable-xxhdpi"
ITEMS_JSON_PATH = r"..\app\src\main\assets\json\items.json"
MOCK_JSON_PATH = r"..\app\src\main\assets\json\mock_market.json"

if not os.path.exists(WEBP_DIR):
    print(f"❌ {WEBP_DIR} bulunamadı! Önce webp_convert.py'yi çalıştırın.")
    exit()

print("🚀 Otomatik Import Başlıyor...")

# Mevcut items.json oku
with open(ITEMS_JSON_PATH, "r", encoding="utf-8") as f:
    items = json.load(f)

# ID ataması için en yüksek id'yi bul
max_id = max([item["id"] for item in items]) if items else 0

# Dosyaları grupla (Eşya Adı -> Varyasyonlar listesi)
# Dosya yapısı: eşya_kondisyon_varyasyonX.webp
grouped_items = {}

for filename in os.listdir(WEBP_DIR):
    if filename.endswith(".webp"):
        # "smartphone_01_flawless_varyasyon1.webp" gibi
        base_name = filename.replace(".webp", "")
        # Varyasyon numarasını atıp eşya+kondisyon kökünü bulalım (örn: smartphone_01_flawless)
        if "_varyasyon" in base_name:
            root_name = base_name.split("_varyasyon")[0]
        else:
            root_name = base_name
            
        if root_name not in grouped_items:
            grouped_items[root_name] = []
        grouped_items[root_name].append(base_name)

# Gruplanan öğeleri items.json ve mock_market.json formatına uygun hale getir
new_count = 0
for root_name, variants in grouped_items.items():
    # Bu öğe zaten items.json içinde var mı?
    exists = False
    for item in items:
        if item["asset"] == root_name:
            # Varyasyonları güncelle
            item["variants"] = list(set(item["variants"] + variants))
            exists = True
            break
            
    if not exists:
        max_id += 1
        new_item = {
            "id": max_id,
            "asset": root_name,
            "name": root_name.replace("_", " ").title(),
            "category": "auto_generated",
            "basePrice": 1000, # Varsayılan fiyat
            "rarity": 1,
            "variants": variants
        }
        items.append(new_item)
        new_count += 1
        
    # Resimleri kopyala
    for var in variants:
        src = os.path.join(WEBP_DIR, f"{var}.webp")
        dst = os.path.join(DRAWABLE_DIR, f"{var}.webp")
        shutil.copy2(src, dst)

# items.json kaydet
with open(ITEMS_JSON_PATH, "w", encoding="utf-8") as f:
    json.dump(items, f, ensure_ascii=False, indent=2)

print(f"✅ Başarılı! {new_count} yeni ürün kategorisi/kondisyonu items.json'a eklendi ve tüm görseller Android projesine kopyalandı.")

# mock_market.json oluştur
sellers = ["Pazarlıkçı Ahmet Abi", "Sabırsız Murat", "Dolandırıcı Şahin", "Temiz Aile Babası Kemal", "Öğrenci Kardeşimiz"]
result = []
seller_idx = 0

for base in items:
    for var in base.get("variants", []):
        sales_val = int(base["basePrice"] * 0.8)
        est_val = base["basePrice"]
        result.append({
            "condition": "Otomatik Eklenen", 
            "sellerName": sellers[seller_idx % len(sellers)],
            "itemName": base["name"],
            "salesValue": str(sales_val),
            "estimatedValue": str(est_val),
            "imageName": var
        })
        seller_idx += 1

with open(MOCK_JSON_PATH, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f"✅ Toplam {len(result)} ilan mock_market.json dosyasına yazıldı! Projeniz artık hazır.")
