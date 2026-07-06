import os
from PIL import Image

# Ayarlar
GIRIS_KLASORU = "Duvance_Gorselleri"
CIKIS_KLASORU = "Duvance_Webp_Hazir"
HEDEF_BOYUT = (512, 512) 
KALITE = 80 

os.makedirs(CIKIS_KLASORU, exist_ok=True)

print("🚀 Sıkıştırma ve Temizlik Modu Aktif!")
print("İpucu: 'Duvance_Gorselleri' klasörünü aç ve gözüne çarpan bozuk resimleri sil, kod sadece kalanları dönüştürecek.\n")

islenen = 0
toplam_boyut_tasarrufu = 0

# Önce klasördeki tüm dosyaları listele
dosyalar = [f for f in os.listdir(GIRIS_KLASORU) if f.endswith(".jpg")]

for dosya in dosyalar:
    giris_yolu = os.path.join(GIRIS_KLASORU, dosya)
    dosya_adi_saf = os.path.splitext(dosya)[0]
    cikis_yolu = os.path.join(CIKIS_KLASORU, f"{dosya_adi_saf}.webp")
    
    # Zaten dönüştürüldü mü?
    if os.path.exists(cikis_yolu):
        continue
        
    try:
        with Image.open(giris_yolu) as img:
            eski_boyut = os.path.getsize(giris_yolu)
            
            # Kalite odaklı yeniden boyutlandırma
            img_resized = img.resize(HEDEF_BOYUT, Image.Resampling.LANCZOS)
            img_resized.save(cikis_yolu, "webp", quality=KALITE)
            
            yeni_boyut = os.path.getsize(cikis_yolu)
            toplam_boyut_tasarrufu += (eski_boyut - yeni_boyut)
            islenen += 1
            
            print(f"✅ {dosya_adi_saf} dönüştürüldü. ({(yeni_boyut/1024):.1f} KB)")
            
    except Exception as e:
        print(f"❌ {dosya} işlenirken hata: {e}")

print(f"\n🎉 İşlem bitti! {islenen} resim WebP formatına çevrildi.")
print(f"📉 Toplam Tasarruf: {(toplam_boyut_tasarrufu / (1024 * 1024)):.2f} MB")