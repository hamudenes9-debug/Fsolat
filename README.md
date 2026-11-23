<div align="center">

# Fsolat

**Siyah-beyaz bir namaz vakitleri uygulaması.**<br/>
Sade, hızlı ve reklamsız — Diyanet yöntemiyle dakikası dakikasına hesaplanan vakitler.

`com.hamudenes.fsolat`

</div>

---

## İndir

En son imzalı APK'yı **[Releases](../../releases/latest)** sayfasından indir.

```
Dosya:      Fsolat-v1.0.0.apk
Paket adı:  com.hamudenes.fsolat
Min. sürüm: Android 8.0 (API 26)
İmza:       SHA-384, 4096-bit RSA (development key)
```

## Özellikler

| | |
|---|---|
| **Vakitler** | İmsak, güneş, öğle, ikindi, akşam, yatsı — astronomik hesap (adhan algoritması) |
| **Methodlar** | Diyanet (18°/17° + düzeltmeler), Diyanet 15°, MWL, ISNA, Mısır, Karaçi, Ümmü'l-Kura, Basra Körfezi, Kuveyt, Katar, Singapur, Fransa |
| **Mezhep** | Şafiî / Hanbelî / Mâlikî ve Hanefî ikindi vakti |
| **Tema** | **Siyah**, **Beyaz** ve **Sistem** — Ayarlar'dan anında değişir |
| **Takip** | Vakit kılındı işaretleme, günlük kayıt, seri (streak) ve aylık oran |
| **İstatistik** | Isı haritası takvim, vakit bazında dağılım, 90 güne kadar geçmiş düzenleme |
| **Kıble** | Kâbe'ye bearing ve mesafe, pusula kadranı |
| **Konum** | 150 şehir + cihaz konumu |
| **Dil** | Türkçe / İngilizce / Sistem |
| **Bildirim** | Vakit bildirimi + 0/5/10/15/20/30 dk önce hatırlatma |
| **Saat** | 24 saat / 12 saat biçimi, vakit başına dakika düzeltmesi |

## Ekranlar

| Ana | İstatistik | Ayarlar |
|---|---|---|
| Sıradaki vakit, geri sayım, bugünün vakitleri, kıble | Seri, aylık oran, ısı haritası, vakit dağılımı | Siyah/Beyaz/Sistem tema, dil, konum, yöntem, bildirim |

Alt navigasyon çubuğu üç sekmeli: **Ana · İstatistik · Ayarlar**

## Kurulum (geliştirici)

```bash
git clone https://github.com/hamudenes9-debug/Fsolat.git
cd Fsolat
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease        # imzalı APK (keystore/fsolat-release.jks)
```

APK her push'ta GitHub Actions ile otomatik derlenir:

- `main` veya `v*` tag → **GitHub Release** yayınlanır
- Diğer branch'ler → workflow artifact olarak yüklenir

## Teknik

- Kotlin 2.0 · Jetpack Compose (BOM 2024.10) · Material 3
- minSdk 26 · targetSdk 35 · AGP 8.7.3 · Gradle 8.9
- DataStore Preferences (ayarlar) + Preferences (günlük kayıt)
- WorkManager (vakit hatırlatmaları)
- Tek modül, ~30 Kotlin dosyası, harici servis yok

### Vakit hesabı doğrulaması

`PrayerCalculator`, [adhan](https://github.com/batoulapps/adhan-js) referansıyla karşılaştırıldı:

```
İstanbul 2026-11-23 (Diyanet)   imsak 06:24  güneş 07:54  öğle 12:55  ikindi 15:24  akşam 17:47  yatsı 19:11
Mekke   2026-11-23 (Ümmü'l-Kura) 05:17       06:37       12:07       15:15        17:37        19:07
```

> Vakitler matematiksel olarak hesaplanır; bulunduğunuz caminin vakitleriyle küçük farklar olabilir.

## Lisans

MIT — dilediğiniz gibi kullanın.
