# Legendary Payment Servlet

## Projekt szkoleniowy do trenowania technik pracy z odziedziczonym kodem

**TODO: Krótkie wprowadzenie**


## Wymagania

Projekt wymaga zainstalowanego lokalnie [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/) najlepiej w wersji **8**.


## Budowa projektu

Projekt jest budowany z wykorzystaniem narzędzia [Gradle](http://www.gradle.org/), jednak dzięki zastosowaniu wrappera
nie jest konieczna jego wcześniejsza instalacja.

### Linia poleceń

Proste zbudowanie i wykonanie testów:

Linux/Unix:

    ./gradlew check

Windows:

    gradlew.bat check


### Import do IDE

#### IntelliJ Idea

Import do IntelliJ Idea 13+: `File -> Import Project.`

Import do IntelliJ Idea 12:

    ./gradlew idea

(`gradle.bat` na platformie Windows)

i potem otworzenie pliku `*.ipr` przez `File->Open`

**Uwaga**. W przypadku korzystania z Idei w wariancie bezpłatnym (Community Edition) zalecana jest [wersja 14](http://confluence.jetbrains.com/display/IDEADEV/IDEA+14+EAP)
posiadająca wbudowane narzędzie do badania pokrycia kodu (w Idei 13 była ta funkcja dostępna była tylko w wersji Ultimate).

#### Eclipse

Import do Eclipse:

    ./gradlew eclipse

(na platformie Windows można 2 razy kliknąć `gradlew-eclipse.bat`)

i potem `File -> Import -> Existing Project Into Workspace`

**Uwaga**. Ze względu na wykorzystanie Java 8 zalecaną wersją Eclipsa jest minimum [Luna (4.4)](https://www.eclipse.org/downloads/).


## O projekcie

Strona projektu: https://github.com/szpak/LegendaryPaymentServlet

Autorzy:
 - [Michał Piotrkowski](https://twitter.com/mpidev)
 - [Marcin Zajączkowski](https://twitter.com/SolidSoftBlog)
