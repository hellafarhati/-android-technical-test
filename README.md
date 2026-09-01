# Albums — test technique Android leboncoin

Application Android affichant les ~5 000 photos de
`https://static.leboncoin.fr/img/shared/technical-test.json`, regroupées par album,
avec recherche, favoris persistés, écran de détail et fonctionnement hors ligne.

## Sommaire de la documentation

| Document | Contenu |
| --- | --- |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Découpage en modules, couches, flux de données, justification de chaque librairie |
| [`docs/BUGS-ET-CORRECTIFS.md`](docs/BUGS-ET-CORRECTIFS.md) | Les 20 problèmes trouvés dans le projet initial, avant / après |
| [`docs/TESTS.md`](docs/TESTS.md) | Stratégie de test, ce qui est couvert et ce qui ne l'est pas |
| [`docs/EVOLUTIONS.md`](docs/EVOLUTIONS.md) | Planification des évolutions (bonus de l'énoncé) |

## Démarrer

Prérequis : **JDK 17** et Android Studio récent (AGP 8.10, Gradle 8.11.1, Kotlin 2.2).

```bash
./gradlew assembleDebug      # construire l'APK de debug
./gradlew installDebug       # installer sur un appareil connecté
./gradlew test               # tous les tests unitaires
./gradlew lint               # analyse statique
```

## Fonctionnalités

- Liste des photos **groupées par album**, avec en-têtes collants (`stickyHeader`).
- **Recherche** par titre (debounce 250 ms) et filtre **Favoris**.
- **Favoris persistés localement**, conservés après une resynchronisation réseau.
- **Écran de détail** recevant réellement l'identifiant de la photo (navigation type-safe).
- **Hors ligne** : la base Room est la source de vérité, l'application affiche son cache
  même sans réseau et après redémarrage.
- **États explicites** : chargement, erreur avec bouton *Réessayer*, vide, contenu.
  Une erreur réseau alors que du cache existe se limite à un *snackbar* non bloquant.
- Rotation, mode sombre, RTL, `contentDescription` et `stateDescription` pour les
  lecteurs d'écran, textes en français et en anglais.

## Stack

| Domaine | Choix | Pourquoi |
| --- | --- | --- |
| UI | Jetpack Compose + Spark (design system leboncoin) | Composants maison, cohérence visuelle |
| Présentation | MVVM, `StateFlow`, `SavedStateHandle` | État unique, survit rotation et process death |
| DI | Hilt (KSP) | Standard Android, portées gérées, testable |
| Réseau | Retrofit 3 + OkHttp + kotlinx.serialization | Contrat typé, parsing sans réflexion |
| Persistance | Room | Requêtes vérifiées à la compilation, `Flow` réactif |
| Images | Coil 3 | Compose-first, partage du client OkHttp, cache disque |
| Navigation | Navigation Compose type-safe | Arguments vérifiés à la compilation |
| Tests | JUnit4, coroutines-test, Turbine, *fakes* écrits à la main | Déterministe, sans mocks fragiles |

## Architecture en un coup d'œil

```
:app  ──────────────►  :feature:albums   ─┐
      └─────────────►  :feature:albumdetails
                              │
                              ▼
                          :data   (réseau, Room, repository, use cases)
                              │
                              ▼
                          :core   (modèles, dispatchers, design system)
```

5 modules Gradle : `:core` regroupe tout ce qui est partagé et stable (modèles, dispatchers,
composants Compose), `:data` toute la logique d'accès aux données (réseau, Room,
repository, use cases), `:feature:albums`/`:feature:albumdetails` sont les deux écrans,
`:app` ne fait que le câblage (Hilt, navigation). Détails, raisons du découpage et
alternative envisagée dans [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).
