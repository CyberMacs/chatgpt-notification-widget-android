# ChatGPT Notification Widget

Teljesen helyi, natív Android alkalmazás és Home Screen widget. Kizárólag a hivatalos ChatGPT alkalmazás (`com.openai.chatgpt`) értesítéseit tárolja és jeleníti meg.

## Gyors telepítés kezdőknek

1. Nyisd meg a [legújabb kiadást](https://github.com/CyberMacs/chatgpt-notification-widget-android/releases/latest).
2. Töltsd le a ChatGPT-Notification-Widget-v0.1.3.apk fájlt.
3. Kövesd a rövid, képeszköz nélkül is érthető [telepítési útmutatót](INSTALL.md).

A telepítés nem igényel Android Studiót vagy programozási ismeretet.

## Adatvédelem

Nincs INTERNET engedély, analitika vagy felhőkapcsolat. A Notification Listener már az esemény beérkezésekor eldob minden nem ChatGPT csomagból érkező értesítést. Az előzmények Room adatbázisban, a készüléken maradnak.

## Követelmények és build

Android Studio Ladybug vagy újabb, JDK 17 és Android SDK 35 szükséges.

```powershell
.\gradlew.bat assembleDebug
```

Az APK: `app\build\outputs\apk\debug\app-debug.apk`.

Telepítés csatlakoztatott készülékre:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Használat

1. Telepítsd és indítsd el az alkalmazást.
2. Nyomd meg az **Értesítés-hozzáférés engedélyezése** gombot, majd az Android rendszeroldalán engedélyezd a ChatGPT Notification Widget hozzáférését.
3. A kezdőképernyőn hosszan nyomva válaszd a Widgetek menüt, majd add hozzá a **ChatGPT értesítések** widgetet.
4. A beállításokban állítsd az előzménylimitet, a megjelenített tételeket és az előnézet elrejtését.

## Ismert korlátozások

Az Android rendszer és a ChatGPT alkalmazás dönt arról, milyen notification-szöveg és PendingIntent elérhető. A PendingIntent csak memóriában marad meg: app-újraindítás után, illetve hibás vagy lejárt intent esetén a widget a ChatGPT főalkalmazást nyitja meg. Egyes One UI energiatakarékossági szabályok késleltethetik a widget kirajzolását, de az értesítésfigyelőnek nincs szüksége foreground service-re.

## Ellenőrzött build

2026-09-20: a 	estDebugUnitTest assembleDebug sikeresen lefutott. Az ellenőrzött, v2 Android APK-aláírású telepítő a projekt Final/ChatGPT-Notification-Widget-v0.1.0-debug.apk fájlja.

## Samsung APK-telepítés – ideiglenes engedélyek

1. Másold át az APK-t a telefonra, majd nyisd meg a **Saját fájlok** alkalmazással.
2. Ha a Samsung ezt kéri, csak a Saját fájlok (vagy a használt böngésző) számára engedélyezd: **Beállítások → Biztonság és adatvédelem → További biztonsági beállítások → Ismeretlen alkalmazások telepítése → Engedélyezés ebből a forrásból**.
3. Ha az Auto Blocker akadályozza a telepítést, kapcsold ki ideiglenesen: **Beállítások → Biztonság és adatvédelem → Auto Blocker → Ki**.
4. Ha a Google Play Protect külön letiltja a telepítést, ideiglenesen kapcsold ki a vizsgálatot: **Play Áruház → profilkép → Play Protect → fogaskerék → Alkalmazások vizsgálata a Play Protecttel → Ki**.
5. Telepítés után kapcsold vissza a Play Protect vizsgálatát és az Auto Blockert; az „ismeretlen alkalmazások” engedélyt is vond vissza a Saját fájlok/böngésző számára.

Az APK saját, helyi debug build, ezért nem a Google Playről származik. Csak ebből a projektből származó APK-t telepíts.

## License

This project is licensed under the GNU General Public License v3.0 or later. See [LICENSE](LICENSE).

## Widget frissítés és méret – v0.1.1

- Az értesítésérkezés, az **Olvasott** és a **Frissítés** gomb már a pillanatnyi Android `Context` alapján frissíti az összes hozzáadott widgetet; rendszerfolyamat-újraindítás után is.
- A widget átméretezhető: kis magasságnál 1, közepesnél 2, nagynál 3 értesítés jelenik meg. A kis méretnél a gombsor elrejtőzik, hogy a tartalom olvasható maradjon.
- Az értesítés érintése először az eredeti ChatGPT értesítés műveletét próbálja megnyitni. Ha az Android ezt már nem engedi vagy az alkalmazás újraindult, a ChatGPT főalkalmazás nyílik meg.

## v0.1.1 – Widgetjavítások

- Az új ChatGPT értesítés automatikusan frissíti az összes hozzáadott widgetet.
- A **Frissítés** és az **Olvasott** gomb rendszerfolyamat újraindítása után is működik.
- A widget mérete változtatható: kis méretben 1, közepesben 2, nagy méretben 3 értesítést mutat.
- Az értesítés megérintése elsőként az eredeti ChatGPT értesítéshez tartozó chatet nyitja meg. Ha ezt az Android már nem engedi, a ChatGPT főképernyője nyílik meg.

## v0.1.2 – Telepítés ellenőrzése

Az alkalmazás főképernyőjének címe most kiírja a telepített verziót, például **ChatGPT értesítések v0.1.2**. Telepítés után ezt ellenőrizd először. Ha más verzió jelenik meg, a régi APK fut: töltsd le újra a v0.1.2 fájlt, telepítsd rá a meglévő alkalmazásra, majd a widgetet hosszú nyomással távolítsd el és add hozzá újra.

Az értesítésfigyelő újracsatlakozáskor feldolgozza a még aktív ChatGPT értesítéseket, ezért telepítés vagy rendszerújraindítás után is frissítenie kell a widgetet.

## v0.1.3 – Kézi frissítés és widgetváltozatok

- A főképernyőn látható **Frissítés** gomb újracsatlakoztatja az Android értesítésfigyelőt, majd a még aktív ChatGPT értesítéseket beolvassa.
- A widget **Frissítés** gombja ugyanezt a frissítést indítja el; nem csak a régi helyi listát rajzolja újra.
- A Widgetek listában három külön elem jelenik meg: **ChatGPT értesítések – Kicsi**, **– Közepes** és **– Részletes**. Mindhárom vízszintesen és függőlegesen átméretezhető.
- Telepítés után a régi widgetet töröld a kezdőképernyőről, és az új listából add hozzá a kívánt változatot.
- A főképernyő fejlécében **v0.1.3** legyen látható. Ha más szám látszik, nem ez az APK fut a telefonon.
