import os
import sys
import time
from playwright.sync_api import sync_playwright

# Windows terminal encoding sorunu icin
sys.stdout.reconfigure(encoding='utf-8')

# === 1. BOLUM: OYUN ESYALARI (EV VE ARABA) ===
house_items = {
    "house_apartment": "modern multi-story city apartment building exterior, urban style",
    "house_villa":     "luxury detached villa with garden and small pool, suburban",
    "house_village":   "classic old traditional village house, stone walls and wood beams",
    "house_mansion":   "massive ultra luxury waterfront mansion, grand architecture",
    "house_prefab":    "simple modular prefab container house, neat and compact",
    "house_penthouse": "modern rooftop penthouse apartment, panoramic glass windows",
    "house_bungalow":  "single story bungalow house with porch and garden",
    "house_farmhouse": "rustic countryside farmhouse with wooden barn nearby",
    "house_studio":    "small modern studio flat apartment, cozy interior visible",
    "house_duplex":    "two-story duplex townhouse, semi-detached style",
}

car_items = {
    "car_sports":  "sleek aggressive sports car, aerodynamic low profile design",
    "car_sedan":   "classic elegant mid-size sedan car, modern clean look",
    "car_suv":     "large rugged SUV off-road 4x4, big knobby tires",
    "car_van":     "commercial panel cargo van, boxy white body",
    "car_toros":   "old classic blocky retro sedan, 1990s Turkish-style car",
    "car_pickup":  "heavy duty pickup truck with open cargo bed",
    "car_muscle":  "classic american muscle car, wide body and chrome accents",
    "car_hatchback": "compact 5-door hatchback city car, small and efficient",
    "car_coupe":   "sleek 2-door fastback coupe, sporty silhouette",
    "car_classic": "vintage 1960s classic car, chrome bumpers and round headlights",
}

# === 2. BOLUM: KONDISYONLAR ===
house_conditions = {
    "01_sifir":          "brand new construction, pristine and immaculate, freshly built",
    "02_az_kullanilmis": "lightly lived-in condition, clean and well-maintained, minor signs of use",
    "03_eski":           "older property, faded paint, weathered walls, visibly aged but structurally sound",
    "04_bakimsiz":       "neglected and run-down, peeling paint, overgrown garden, worn out",
    "05_hasarli":        "structurally damaged, cracked walls, broken windows, partial collapse visible",
    "06_yangin_hasari":  "fire damaged property, scorched walls, burnt roof, smoke marks",
    "07_sel_hasari":     "flood damaged, waterlogged walls, visible water stains and mold",
    "08_harabe":         "completely abandoned ruin, crumbling structure, overtaken by nature",
}

car_conditions = {
    "01_sifir":          "brand new showroom condition, perfectly clean and shiny, immaculate paint",
    "02_az_kullanilmis": "lightly used condition, very minor scratches, well-maintained and clean",
    "03_cizik":          "worn condition, visible scratches and small dents on body panels",
    "04_eski":           "old and faded, oxidized paint, worn interior visible through windows",
    "05_agir_hasarli":   "heavily crash damaged, large dents, bent panels, broken lights",
    "06_yanmis":         "fire damaged car, burnt and charred, melted plastic parts visible",
    "07_pasakli":        "abandoned and rusted, covered in rust spots and dust, flat tires",
    "08_hurdaya_donmus": "total wreck scrap condition, completely crushed and destroyed",
}

# Kayıt klasörünü oluştur
kayit_klasoru = os.path.join(os.getcwd(), "Duvance_Gorselleri_EvAraba")
os.makedirs(kayit_klasoru, exist_ok=True)

# Edge tarayıcı profil yolu
user_data_dir = os.path.join(os.environ["LOCALAPPDATA"], "Microsoft", "Edge", "User Data")

# Promptları hafızada oluştur
envanter_listesi = {}

for item_key, item_desc in house_items.items():
    for cond_key, cond_desc in house_conditions.items():
        dosya_adi = f"{item_key}_{cond_key}"
        prompt = f"3D isometric render of a {item_desc}, {cond_desc}, plain white background, professional video game asset, no people"
        envanter_listesi[dosya_adi] = prompt

for item_key, item_desc in car_items.items():
    for cond_key, cond_desc in car_conditions.items():
        dosya_adi = f"{item_key}_{cond_key}"
        prompt = f"3D isometric render of a {item_desc}, {cond_desc}, plain white background, professional video game asset, no people"
        envanter_listesi[dosya_adi] = prompt

with sync_playwright() as p:
    print(f"🚀 Tarayıcı senin Edge profilinle ayağa kaldırılıyor... (Toplam {len(envanter_listesi)} Eşya Modeli)")
    browser = p.chromium.launch_persistent_context(
        user_data_dir,
        headless=False,
        channel="msedge",
        args=["--start-maximized"]
    )
    
    page = browser.pages[0] if browser.pages else browser.new_page()
    
    sayac = 1
    for dosya_adi, prompt in envanter_listesi.items():
        print(f"\n⚙️ [{sayac}/{len(envanter_listesi)}] Üretiliyor: {dosya_adi}")
        sayac += 1
        
        # Kaldığımız yerden devam etme kontrolü (1. varyasyon varsa atla)
        if os.path.exists(os.path.join(kayit_klasoru, f"{dosya_adi}_varyasyon1.jpg")):
            print(f"⏩ Zaten mevcut, atlanıyor: {dosya_adi}")
            continue

        page.goto("https://www.bing.com/images/create", wait_until="networkidle")
        
        try:
            # Promptu gir
            textarea = page.wait_for_selector("textarea.b_searchbox", timeout=10000)
            textarea.fill("")
            textarea.fill(prompt)
            
            # Oluştura bas
            create_button = page.wait_for_selector("#create_btn_c", timeout=10000)
            create_button.click()
            
            print("⏳ Arka planda üretim başladı. 35 saniye bekleniyor...")
            time.sleep(35)
            
            print("🔄 F5 Atılıyor... (Önbellekteki görselleri ekrana çekme)")
            page.reload(wait_until="domcontentloaded") 
            
            print("🔍 Ekranda üretilen varyasyonlar aranıyor...")
            # İlk OIG imzalı resmin yüklenmesini bekle
            page.wait_for_selector("img[src*='OIG']", timeout=30000)
            
            # TRİCK: Diğer 3 resmin de yüklenmesi için bota 3 saniye mola (Fren) ver
            time.sleep(3)
            
            # Ekrana düşen tüm resim elementlerini yakala
            gorsel_elementleri = page.query_selector_all("img[src*='OIG']")
            
            # Sadece 4 adet benzersiz ve tam çözünürlüklü linki filtrele
            benzersiz_urller = []
            for gorsel in gorsel_elementleri:
                gorsel_url = gorsel.get_attribute("src")
                if "?" in gorsel_url:
                    gorsel_url = gorsel_url.split("?")[0]
                
                if gorsel_url not in benzersiz_urller:
                    benzersiz_urller.append(gorsel_url)
                
                if len(benzersiz_urller) == 4:
                    break

            # Bulunan 4 linki sırayla indir
            for index, gorsel_url in enumerate(benzersiz_urller):
                image_page = browser.new_page()
                response = image_page.goto(gorsel_url)
                
                varyasyon_adi = f"{dosya_adi}_varyasyon{index + 1}.jpg"
                dosya_yolu = os.path.join(kayit_klasoru, varyasyon_adi)
                
                with open(dosya_yolu, "wb") as f:
                     f.write(response.body())
                
                image_page.close()
                print(f"  └─ İndirildi: {varyasyon_adi}")
                
            print(f"✅ BAŞARILI: {dosya_adi} için {len(benzersiz_urller)} varyasyon kaydedildi!")
            
        except Exception as e:
            print(f"❌ HATA: {dosya_adi} işlenemedi. Atlanıyor... ({e})")
            
        # Ban yememek için iki prompt arası kısa mola
        time.sleep(5)
        
    browser.close()
    print("\n🎉 OYUNUN TÜM EV/ARABA ASSETLERİ BAŞARIYLA İNDİRİLDİ!")
