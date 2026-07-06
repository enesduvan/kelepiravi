import sys
import types

# --- PYTHON 3.12+ UYUMLULUK YAMASI (MONKEY PATCH) ---
try:
    import pkg_resources
except ImportError:
    mock_pkg = types.ModuleType("pkg_resources")
    mock_pkg.get_distribution = lambda name: types.SimpleNamespace(version="1.0.0")
    sys.modules["pkg_resources"] = mock_pkg
# -----------------------------------------------------

import os
import time
from BingImageCreator import ImageGen

# --- AYARLAR ---
# Tarayıcıdan aldığın iki farklı çerez değerini buraya yapıştır:
BING_U_COOKIE = "1YJa8j3uty06XElWjK0ZiVjgAUst_YW3w_AfQ4RffRq8DhFEkN7ahjUs2VlyN3_D-e5JT-2Uoknb4Nx3gZg8bajEf9jOvIgEBF9bAD3mUaNqi5dttoUHBTexbK9nwduGzq2WJN2Or7Q-jV6aVsTQIeV0rH51lFROJiIM8VuEgsnJvqWcVYAHpuhL5icoj89Q2L9tcMrfvfOlM-22dRosbzw"
BING_SRCHHPGUSR_COOKIE = "BURAYA_SRCHHPGUSR_DEGERINI_YAPISTIRSRCHLANG=tr&PV=19.0.0&B=0&HV=1782109562&HVE=CfDJ8A8rLfEh4ZdMhJ19YNJ4FtToDJCNU7437Hqt42BSjIj07mcHkqnDEd34Nm1FfsFhtgINump7H1jMl5xGRD4DzXQ_xjUbWhm135JFkRQYt8Uo19UhhZbqxdJQZgC2UFt8jNvwBdZrORWYlKew3rd-uK_zIGwOER_7RTBtEd4C24FujuPs9epTAthnN3Jm-pg5qw"

# 10 adetlik test listesi
test_listesi = {
    "test_01": "3D isometric render of a generic modern smartphone, perfect condition, no logos, plain white background, video game asset",
    "test_02": "3D isometric render of a generic classic keypad mobile phone, perfect condition, retro style, no brands, plain white background, video game asset",
    "test_03": "3D isometric render of a generic tablet device, flawless condition, dark grey casing, no logos, plain white background, video game asset",
    "test_04": "3D isometric render of a generic gaming laptop, flawless condition, RGB accents, no logos, plain white background, video game asset",
    "test_05": "3D isometric render of a slim silver office laptop, perfect condition, simple and elegant, no brands, plain white background, video game asset",
    "test_06": "3D isometric render of a generic PC monitor, flawless condition, no logos, plain white background, video game asset",
    "test_07": "3D isometric render of generic white wireless earbuds, perfect condition, no brands, plain white background, video game asset",
    "test_08": "3D isometric render of a generic over-ear gaming headset, perfect condition, no logos, plain white background, video game asset",
    "test_09": "3D isometric render of a generic smartwatch, perfect condition, no logos, plain white background, video game asset",
    "test_10": "3D isometric render of a generic portable bluetooth speaker, perfect condition, no brands, plain white background, video game asset"
}

kayit_klasoru = "Duvance_Test_Gorselleri"
os.makedirs(kayit_klasoru, exist_ok=True)

print("🚀 Çift Çerezli Test Üretimi Başlatılıyor...")

try:
    # İki çerezi de kütüphanenin istediği formatta gönderiyoruz
    generator = ImageGen(auth_cookie=BING_U_COOKIE, auth_cookie_SRCHHPGUSR=BING_SRCHHPGUSR_COOKIE)
except Exception as e:
    print(f"❌ Bağlantı hatası: {e}")
    exit()

for dosya_adi, prompt in test_listesi.items():
    print(f"\n⚙️ Üretiliyor: {dosya_adi}...")
    try:
        gorsel_linkleri = generator.get_images(prompt)
        generator.save_images(gorsel_linkleri, output_dir=kayit_klasoru, file_name=dosya_adi)
        print(f"✅ Başarılı! {dosya_adi}.jpg olarak kaydedildi.")
        
        # İstekler arası güvenli bekleme süresi
        print("⏳ 15 saniye bekleniyor...")
        time.sleep(15)
        
    except Exception as e:
        print(f"❌ HATA ({dosya_adi}): {e}")

print("\n🎉 TEST TAMAMLANDI! Klasörü kontrol edebilirsin ustam.")