# BookForge 1.1.0 – mobilní editor knih

Česká Android aplikace na psaní románů, povídek a knih, připravená pro **XG Applications**.

### Dvojstrana uprostřed
- Vždy **právě dvě stránky** vedle sebe, zarovnané na střed.
- Předchozí/další dvojstrana se zobrazí až po stisknutí šipky.
- Samostatný režim jedné stránky a tlačítko „Dvě na střed“ pro přizpůsobení mobilu.
- Přiblížení 15–200 %, automatické stránkování po kapitolách.

### Formáty a rozvržení
- A3, A4, A5, A6, B5, US Letter, Legal, čtverec nebo vlastní rozměr 50–500 mm.
- Orientace na výšku i na šířku, vlastní okraje.
- Čísla stránek mohou začínat vlastním číslem a být arabská nebo římská, vlevo/uprostřed/vpravo, lze je vypnout.

### Editace
- Formátování textu, nadpisy, barvy, zarovnání, seznamy, odsazení, hledání a nahrazování.
- Vkládání obrázků (JPEG/PNG/WebP, převedení na úsporné JPEG), tabulek a odkazů.
- Titul knihy, autor, ISBN, anotace a samostatná obálka.
- Více knih a kapitol, automatické lokální ukládání.

### Export
- **EPUB 3** s obsahem, kapitolami, obálkou a obrázky.
- **Word DOCX** včetně vložených obrázků a rozměrů stránek.
- **PDF** přes tiskový dialog Androidu, HTML, TXT a záloha JSON.

### Android
- Verze: `1.1.0`, code `2`
- Application ID: `com.xg.bookforge`
- minSdk 26 (Android 8.0), targetSdk 36
- Java 17, Android Gradle Plugin 8.12.3, Gradle 8.13
- GitHub Actions: [APK a AAB](../../actions/workflows/android.yml)
- APK je **debug podepsané testovacím klíčem**.
- AAB je **nepodepsané** a před nahráním na Google Play musí být podepsané tvým release/upload klíčem.

**Limity:** Editor zatím není 100% náhradou desktopového Microsoft Wordu: nemá komentáře a sledování změn, import DOCX, složité styly/tabulky nebo profesionální sazbu. EPUB je přetékající text podle čtečky, ne pevně rozdělená dvojstrana. PDF export používá tiskový papír nastavený podle první stránky, při smíšených formátech není zaručeno jejich zachování. Při důležité práci pravidelně exportuj JSON zálohu.

Vytvořeno pro XG Applications.
