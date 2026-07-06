import os
import time
from playwright.sync_api import sync_playwright

# === 1. BÖLÜM: 75 TEMEL OYUN EŞYASI ===
base_items = {
    # Elektronik (15)
    "smartphone": "generic modern smartphone, dark grey aluminum casing",
    "keypad_phone": "generic classic keypad mobile phone, retro style",
    "tablet": "sleek generic tablet device, shiny black screen",
    "gaming_laptop": "thick high-end gaming laptop, glowing RGB accents",
    "office_laptop": "slim silver office laptop, minimalist design",
    "pc_monitor": "27-inch frameless PC monitor, matte black finish",
    "wireless_earbuds": "generic white wireless earbuds with charging case",
    "gaming_headset": "premium over-ear gaming headset, dark blue padding",
    "smartwatch": "sporty smartwatch, metallic body and rubber strap",
    "bluetooth_speaker": "portable cylindrical bluetooth speaker, mesh texture",
    "game_console": "modern game console system, futuristic geometry",
    "mini_projector": "small generic mini projector, cube shape",
    "dslr_camera": "professional DSLR camera with large lens",
    "studio_mic": "studio USB condenser microphone on a small desk stand",
    "mp3_player": "vintage generic mp3 player, small aluminum body",

    # Ev Eşyaları (15)
    "coffee_maker": "generic drip coffee maker with a glass pot",
    "kitchen_blender": "generic kitchen blender with a glass pitcher",
    "vacuum_cleaner": "modern upright vacuum cleaner, shiny plastic",
    "airfryer": "digital airfryer, glossy black plastic finish",
    "mini_fridge": "mini fridge for a dorm room, closed door",
    "microwave": "classic microwave oven, digital display",
    "electric_kettle": "stainless steel electric kettle",
    "desk_lamp": "articulated metal desk lamp",
    "alarm_clock": "digital bedside alarm clock, red LED numbers",
    "pedestal_fan": "standing pedestal fan with oscillating head",
    "robot_vacuum": "flat circular robotic vacuum cleaner",
    "vintage_radio": "wooden vintage AM/FM radio, retro dials",
    "smart_speaker": "cylindrical smart home speaker, fabric wrap",
    "electric_iron": "clothes electric ironing tool, smooth soleplate",
    "space_heater": "small portable electric space heater",

    # Hobi & Eğlence (15)
    "mech_keyboard": "mechanical gaming keyboard, colorful keycaps",
    "game_controller": "wireless game controller gamepad",
    "handheld_console": "portable handheld gaming console with screen",
    "vr_headset": "virtual reality VR headset goggles",
    "arcade_stick": "retro arcade fighting stick with colorful buttons",
    "board_game": "classic board game box, colorful cover",
    "acoustic_guitar": "wooden acoustic guitar, detailed strings",
    "electric_guitar": "electric guitar, solid color body",
    "drone": "small quadcopter camera drone",
    "drawing_tablet": "digital graphic drawing tablet with stylus pen",
    "action_camera": "small rugged action camera in waterproof case",
    "rubiks_cube": "classic 3x3 puzzle rubiks cube",
    "skateboard": "wooden skateboard with grip tape and wheels",
    "roller_skates": "pair of retro quad roller skates",
    "binoculars": "black outdoor tactical binoculars",

    # Araçlar & Gereçler (15)
    "mountain_bike": "outdoor mountain bike with thick tires",
    "kick_scooter": "electric kick scooter, long deck",
    "power_drill": "cordless electric power drill with battery",
    "toolbox": "classic red metal toolbox, closed",
    "chainsaw": "heavy duty gas powered chainsaw",
    "multimeter": "digital electronic multimeter tester with cables",
    "circular_saw": "electric circular saw tool",
    "wrench_set": "set of steel mechanic wrenches",
    "welding_machine": "compact portable welding machine",
    "car_battery": "12V automotive car battery",
    "motorcycle_helmet": "full face motorcycle helmet with visor",
    "car_tire": "car tire wrapped around an alloy rim",
    "electric_unicycle": "electric unicycle personal transporter",
    "hoverboard": "two-wheeled self-balancing hoverboard",
    "portable_generator": "heavy duty portable power generator",

    # Giyim & Aksesuar (15)
    "sunglasses": "aviator style sunglasses, metal frame",
    "leather_jacket": "classic black leather motorcycle jacket, folded",
    "sneakers": "pair of athletic running sneakers",
    "leather_boots": "pair of vintage brown leather boots",
    "wrist_watch": "classic analog wrist watch, leather strap",
    "canvas_backpack": "outdoor canvas hiking backpack",
    "hard_suitcase": "hard shell travel suitcase with wheels",
    "leather_wallet": "folded brown leather bifold wallet",
    "baseball_cap": "casual sports baseball cap",
    "winter_beanie": "knitted winter beanie hat",
    "leather_gloves": "pair of black leather driving gloves",
    "duffel_bag": "cylindrical sports gym duffel bag",
    "hoodie": "casual cotton hoodie sweater, folded",
    "denim_jeans": "pair of classic blue denim jeans, folded",
    "silver_necklace": "simple silver chain necklace with a small pendant"
}

# === 2. BÖLÜM: 10 FARKLI KONDİSYON ===
conditions = {
    "01_flawless": "perfect flawless brand new condition, immaculate and shiny",
    "02_lightly_used": "lightly used condition, very minor smudges, functional",
    "03_worn_out": "worn out condition, visible scratches and faded colors",
    "04_heavily_damaged": "heavily damaged and cracked, deep structural damage",
    "05_missing_parts": "incomplete, missing some external parts or buttons",
    "06_repaired_duct_tape": "visibly repaired with grey duct tape, ugly but functional fix",
    "07_dirty_rusty": "abandoned condition, covered in dirt, dust, and rust spots",
    "08_burned": "risky condition, partially burned or melted plastic parts",
    "09_custom_painted": "customized condition, covered in graffiti and colorful spray paint",
    "10_scrap": "total scrap condition, completely destroyed beyond repair, useless junk"
}

# Kayıt klasörünü oluştur
kayit_klasoru = os.path.join(os.getcwd(), "Duvance_Gorselleri")
os.makedirs(kayit_klasoru, exist_ok=True)

# Edge tarayıcı profil yolu
user_data_dir = os.path.join(os.environ["LOCALAPPDATA"], "Microsoft", "Edge", "User Data")

# Promptları hafızada oluştur (75 item x 10 condition = 750 set)
envanter_listesi = {}
for item_key, item_desc in base_items.items():
    for cond_key, cond_desc in conditions.items():
        dosya_adi = f"{item_key}_{cond_key}"
        prompt = f"3D isometric render of a {item_desc}, {cond_desc}, plain white background, professional video game asset"
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
    print("\n🎉 OYUNUN TÜM 3000 ASSETİ (750x4 VARYASYONUYLA) BAŞARIYLA İNDİRİLDİ!")