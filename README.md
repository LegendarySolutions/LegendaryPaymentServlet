# Legendary Payment Servlet

## Projekt szkoleniowy do trenowania technik pracy z odziedziczonym kodem

"Legendary Payment Servlet" rozwijany przez lata w firmie projekt do integracji wewnętrznych systemów z bramką płatności. Kod, który w podobnej postaci można prawdpopodobnie spotkać w bardzo wielu istniejących firmach. Celem warsztatu jest przećwiczenie pracy z odziedziczonym kodem w oparciu o algorytmem postępowania opisany w książce Micheal'a Feathersa ["Working Effectively with Legacy Code"](http://www.informit.com/store/working-effectively-with-legacy-code-9780131177055).

Zadaniem uczestnika jest zaimplementowanie kilku prostych ficzerów starając się postępować zgodnie z algorytmem:
 1. Zidentyfikuj miejsce zmiany,
 2. Znajdź najodpowiedniejsze miejsca do przetestowania,
 3. Rozbij zależności, jeżeli to konieczne,
 4. Napisz testy (na dotychczasową i nową funkcjonalność),
 5. Dokonaj zmiany i zrefaktoryzuj.


## Wymagania

Projekt wymaga zainstalowanego lokalnie [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/) najlepiej w **wersji 8**.


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

Import do IntelliJ Idea 13+: `File -> Import Project.` i wskazać plik `build.gradle`.

**Uwaga**. W przypadku korzystania z Idei w wariancie bezpłatnym (Community Edition) zalecana jest [wersja 14](http://confluence.jetbrains.com/display/IDEADEV/IDEA+14+EAP)
posiadająca wbudowane narzędzie do badania pokrycia kodu (w Idei 13 była ta funkcja dostępna była tylko w wersji Ultimate).

#### Eclipse

Import do Eclipse:

    ./gradlew eclipse

(na platformie Windows można 2 razy kliknąć `gradlew-eclipse.bat`)

i potem `File -> Import -> Existing Project Into Workspace`

**Uwaga**. Ze względu na wykorzystanie Java 8 zalecaną wersją Eclipsa jest minimum [Luna (4.4)](https://www.eclipse.org/downloads/).

## Konfiguracja IDE

Zalecana konfiguracja IDE (Idea i Eclipse) została opisana w pliku [IDE.md](IDE.md)

## O projekcie

Strona projektu: https://github.com/LegendarySolutions/LegendaryPaymentServlet

Autorzy:
 - [Michał Piotrkowski](https://twitter.com/mpidev)
 - [Marcin Zajączkowski](https://twitter.com/SolidSoftBlog)
