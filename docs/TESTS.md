# Stratégie de test

```bash
./gradlew test                              # 30 tests unitaires, tous modules
./gradlew :data:test                        # un module en particulier
```

## Répartition

| Module | Tests | Ce qui est verrouillé |
| --- | ---: | --- |
| `:data` (use cases) | 6 | Filtrage, recherche, regroupement par album, réactivité aux critères |
| `:data` (repository) | 8 | Arbitrage cache/réseau, traduction des erreurs, survie des favoris |
| `:data` (réseau) | 3 | Contrat JSON : clés inconnues, valeurs nulles, champs manquants |
| `:feature:albums` | 9 | États UI, recherche, favoris, **rotation et process death** |
| `:feature:albumdetails` | 4 | Argument de navigation, photo absente, bascule du favori |
| **Total** | **30** | |

## Principes

**Tester le comportement, pas l'implémentation.** Les tests décrivent ce que
l'utilisateur constate : « une panne réseau conserve le contenu en cache », « les favoris
survivent à une resynchronisation », « la recherche survit à une rotation ». Ils ne
vérifient pas qu'une méthode précise a été appelée — ce qui les rendrait fragiles au
moindre refactoring.

**Des fakes plutôt que des mocks.** `FakeAlbumsRepository`, `FakeAlbumsStore` (qui rejoue
le comportement observable de Room : jointure, tri, ré-émission) et
`FakeAlbumsRemoteDataSource` sont écrits à la main dans les sources de test de chaque
module. `FakeAlbumsRepository` est dupliqué entre `:feature:albums` et
`:feature:albumdetails` plutôt que mis dans un module de test partagé : avec seulement deux
consommateurs, la duplication (une trentaine de lignes) coûte moins cher à maintenir qu'un
module supplémentaire. C'est un choix assumé au global :

- ils sont typés, donc un changement de signature casse la compilation, pas un test au
  runtime ;
- ils encodent un vrai comportement, ce qui rend possible des scénarios en plusieurs
  étapes (mettre en favori, puis resynchroniser, puis vérifier) impossibles à écrire avec
  un empilement de `every { ... } returns ...` ;
- ils poussent à garder les interfaces petites : un fake pénible à écrire signale une
  interface trop large ;
- une dépendance de moins dans le build.

**Des tests déterministes.** Aucun `Thread.sleep`, aucune attente active. Les dispatchers
sont injectés (`DispatcherProvider`), `MainDispatcherRule` remplace `Dispatchers.Main`, et
tous les tests partagent l'ordonnanceur virtuel de `runTest` — le `debounce` de 250 ms
s'écoule instantanément via `advanceUntilIdle()`.

**Turbine** pour les `Flow` : chaque émission doit être consommée explicitement, ce qui
révèle les émissions parasites (par exemple un état intermédiaire vide qui provoquerait un
clignotement).

## Le test qui compte le plus

```kotlin
@Test
fun `state and search survive the loss of the collector (configuration change)`() { … }
```

Il simule exactement le scénario éliminatoire de l'énoncé : l'écran est détruit, le
collecteur disparaît, l'écran est recréé. Le test vérifie que la recherche est conservée,
que la liste est toujours là, **et qu'aucun appel réseau supplémentaire n'a été déclenché**.
C'est précisément ce que le projet initial échouait à faire.

Un second test (`restores the query saved before a process death`) construit le ViewModel
avec un `SavedStateHandle` pré-rempli : c'est le scénario « application tuée en arrière-plan
puis restaurée », que la seule survie du ViewModel ne couvre pas.

## Ce qui n'est pas couvert, et pourquoi

| Non testé | Raison | Comment le couvrir |
| --- | --- | --- |
| DAO Room (SQL réel) | Nécessite un appareil ou Robolectric | Tests instrumentés avec une base en mémoire (`room-testing`) |
| Composables | Nécessite `createComposeRule` et un runtime Android | `AlbumsScreen` est déjà sans état et découplé du ViewModel : le test se résume à injecter un `AlbumsUiState` |
| Couche Retrofit réelle | Un test avec `MockWebServer` validerait surtout Retrofit lui-même | Le contrat JSON, seul vrai risque de régression, est testé directement |
| Navigation de bout en bout | Test instrumenté | `SmokeInstrumentedTest` fournit le point de départ |

Ces manques sont assumés et priorisés dans [`EVOLUTIONS.md`](EVOLUTIONS.md) — un test qui
demande un émulateur coûte cher en CI, et l'essentiel de la logique risquée est déjà
couverte en JVM pure.
