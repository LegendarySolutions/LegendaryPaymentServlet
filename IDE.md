# Konfiguracja IDE

## IntelliJ Idea

### Przydatne pluginy

#### Zalecane w czasie warsztatów

 - Key Promoter - http://plugins.jetbrains.com/plugin/4455 - podpowiadanie skrótów klawiaturowych do czynności wykonywanych myszą

#### Opcjonalne

 - Builder Generator - https://github.com/mjedynak/Builder-Generator - generowanie fluent builderów dla klasy

Pluginy można zainstalować z menadżera pluginów IDE:

```CTRL-SHIFT-S -> Plugins -> Browse Repositories```

### Konfiguracja

### Lepszy szablon metody testowej

```CTRL-SHIFT-A -> File and Code Templates Window -> Code```

`JUnit4 Test Method` zmieniamy na coś podobnego do:

    @org.junit.Test
    public void should${NAME}() {
      //given
      ${BODY}
      //when
      //then
    }

Dodatkowo warto z metody `JUnit4 SetUp Method` usunąć `throws Exception`.

Szablon można wstawić używając ```CTRL-INSERT``` w klasie testowej.

### Wybrane skróty

 - ```CTRL-SHIFT-A``` - możliwość wyszukania po nazwie akcji (komendy) i opcji (w ustawieniach)
 - ```ALT-ENTER``` - przekształcenia na aktualnie wskazywanym przez kursor fragmencie kodu (np. utworzenie nowej metody o wybranych parametrach)
 - ```SHIFT-CTRL-T``` - przeskok do między testem i klasą testowaną (również utworzenie nowej klasy testowej)
 - ```CTRL-E``` - przeskok między ostatnio używanymi klasami
 - ```ALT-CTRL-SHIFT-T``` - menu wyświetlające dostępne opcje refaktoringu (warto zapamiętać skrót dla często powtarzanych operacji)

Więcej informacji: http://www.jetbrains.com/idea/docs/IntelliJIDEA_ReferenceCard.pdf - warto sobie wydrukować i zerkać


## Eclipse

### Przydatne pluginy

#### Zalecane w czasie warsztatów

 - More Unit - http://moreunit.sourceforge.net/ - między innymi: przeskakiwanie między testem a kodem klasy (CTRL-J) i szybkie uruchamianie bieżącego testu (CTRL-R)
 - Mouse Feed - https://github.com/heeckhau/mousefeed - podpowiadanie skrótów klawiaturowych do czynności wykonywanych myszą
 - EclEmma - http://www.eclemma.org/ - pokazanie pokrycia kodu produkcyjnego przez testy automatyczne

#### Opcjonalne

 - Code Recommenders - http://www.eclipse.org/recommenders/ - sprytniejsze uzupełnianie/podpowiadanie
 - Fluent Builder Generator - https://code.google.com/p/fluent-builders-generator-eclipse-plugin/ - generowanie fluent builderów dla klasy
 - Pitclipse - https://github.com/philglover/pitclipse - integracja z narzędziem PIT do testowania mutacyjnego
 - Infinitest - http://infinitest.github.io/ - ciągłe uruchamianie testów IDE

### Konfiguracja

#### Lepsze podpowiadanie statycznych importów:

```Window -> Preferences -> Java -> Editor -> Content Assist -> Favorites```

Warto dodać:

    org.assertj.core.api.Assertions

    org.mockito.BDDMockito
    org.mockito.Mockito
    org.mockito.Matchers

(Eclipse automatycznie doda `.*` na końcu)

Więcej informacji: http://stackoverflow.com/questions/288861/eclipse-optimize-imports-to-include-static-imports

#### Lepszy szablon metody testowej

```Window -> Preferences -> Java -> Editor -> Templates```

Istniejący szablon test zmieniamy na coś podobnego do:

    @Test
    public void should${someMeaningfulName}() {
        //given
        ${cursor}
        //when
        //then

    }${:import(org.junit.Test)}

Więcej informacja: http://www.macluq.com/2013/01/07/eclipse-template-to-insert-test-methods-should-given-when-then/

#### Bardziej rozsądna maksymalna długość linii w edytorze

Ograniczenie 80 znaków miało swoje uzasadnienie historyczne, jednak obecnie większość edytuje kod Java w IDE w trybie graficznym na monitorach/wyświetlaczach mieszczących znacznie więcej linii. Sensowna wydaje się zmiana ustawień z domyślnych 80 na 120 lub (wedle preferencji) nawet na 150 znaków, aby w przypadku użycia automatycznego formatowania nie przeżyć rozczarowania.

```
Java -> Code Style -> Formatter -> New... -> "Long lines" -> OK ->
        Line Wrapping -> Maximum line width -> 120 -> OK
```

### Wybrane skróty

 - ```CTRL-SHIFT-M``` - zamień konstrukcję ```Assertions.assertThat(...)``` na statyczny import
 - ```CTRL-J``` - przeskocz między testem a kodem klasy (More Unit)
 - ```CTRL-R``` - szybkie uruchamianie bieżącego testu (More Unit)

