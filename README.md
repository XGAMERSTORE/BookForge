# BookForge – editor knih pro Android

Verze **1.0.0** (versionCode 1), `com.xg.bookforge`, minSdk 26, targetSdk 36.

## Funkce
- Samostatné papíry A4 a A5, možnost různých formátů ve stejné knize.
- Dvojstrana nebo jednotlivé stránky, vodorovný posun, zoom.
- Kapitoly, textové formátování, ukládání offline.
- Export DOCX, TXT, HTML, JSON, tisk do PDF pomocí Androidu.

## Sestavení
GitHub Actions při push do main automaticky vytvoří:
- **BookForge-v1.0.0-APK**: debug APK podepsané testovacím klíčem pro instalaci do telefonu.
- **BookForge-v1.0.0-AAB-unsigned**: nepodepsané release AAB. Pro Google Play jej musíte podepsat svým dlouhodobým upload key, nepoužívejte testovací klíč.

V Android Studiu (JDK 17, Gradle 8.13, Android SDK 36): `gradle assembleDebug bundleRelease`.

## Upozornění
Editor je první verze; složitě formátovaný text se nemusí dokonale automaticky stránkovat. Neumí import DOCX ani obrázky a tabulky. Výstupy pravidelně zálohujte přes JSON export. Obsah knih neopouští zařízení.

Vývoj XG Applications.
